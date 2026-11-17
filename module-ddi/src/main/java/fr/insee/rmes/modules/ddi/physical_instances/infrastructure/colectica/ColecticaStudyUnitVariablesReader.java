package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static fr.insee.rmes.domain.logging.LogSanitizer.forLog;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.DATA_RELATIONSHIP;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.LOGICAL_PRODUCT;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.PHYSICAL_INSTANCE;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.VARIABLE;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.VARIABLE_GROUP;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.VARIABLE_SCHEME;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.RelationshipDirection;
import fr.insee.rmes.colectica.client.dto.ColecticaItem;
import fr.insee.rmes.colectica.client.dto.ColecticaItemResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeListVariableUsage;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Variables d'une StudyUnit, pour leur réutilisation d'une PhysicalInstance à l'autre (#1387).
 *
 * <p>Deux lectures, toutes deux en descente {@code bysubject} :
 *
 * <ul>
 *   <li>le vivier : StudyUnit → LogicalProduct → VariableScheme → Variable, y compris les variables
 *       rangées dans des VariableGroup, imbriqués ou non, sous le VariableScheme ;
 *   <li>les usages : StudyUnit → PhysicalInstance → DataRelationship → Variable.
 * </ul>
 */
class ColecticaStudyUnitVariablesReader {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaStudyUnitVariablesReader.class);

    private final ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;
    private final ColecticaClient colecticaClient;
    private final DDI3toDDI4ConverterService ddi3ToDdi4Converter;
    private final ColecticaLabels labels;

    ColecticaStudyUnitVariablesReader(
            ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
            ColecticaClient colecticaClient,
            DDI3toDDI4ConverterService ddi3ToDdi4Converter,
            ColecticaLabels labels) {
        this.instanceConfiguration = instanceConfiguration;
        this.colecticaClient = colecticaClient;
        this.ddi3ToDdi4Converter = ddi3ToDdi4Converter;
        this.labels = labels;
    }

    Ddi4Response getStudyUnitVariables(String agencyId, String studyUnitId) {
        if (logger.isInfoEnabled()) {
            logger.info(
                    "Fetching variables of the variable scheme of study unit {}/{}",
                    forLog(agencyId),
                    forLog(studyUnitId));
        }
        List<ItemReference> variableSchemes = ColecticaRelationships.descend(
                colecticaClient,
                new ItemReference(agencyId, studyUnitId),
                List.of(itemType(LOGICAL_PRODUCT), itemType(VARIABLE_SCHEME)));

        List<ColecticaItem> variables = new ArrayList<>();
        for (ItemReference variableScheme : variableSchemes) {
            variables.addAll(variablesUnder(variableScheme));
        }
        variables = ColecticaItems.latestVersions(variables);
        if (variables.isEmpty()) {
            return variablesOnly(List.of());
        }

        ColecticaItemResponse[] fragments = colecticaClient.getDescriptions(ColecticaItems.identifiersOf(variables));
        List<Ddi3Response.Ddi3Item> ddi3Items = fragments == null ? List.of() : ColecticaItems.toDdi3Items(fragments);
        Ddi4Response converted =
                ddi3ToDdi4Converter.convertDdi3ToDdi4(new Ddi3Response(null, ddi3Items), Ddi4Response.SCHEMA);
        return variablesOnly(converted == null || converted.variable() == null ? List.of() : converted.variable());
    }

    List<CodeListVariableUsage> getStudyUnitVariableUsages(String agencyId, String studyUnitId) {
        if (logger.isInfoEnabled()) {
            logger.info(
                    "Fetching variable usages of the physical instances of study unit {}/{}",
                    forLog(agencyId),
                    forLog(studyUnitId));
        }
        List<ColecticaItem> physicalInstances =
                ColecticaItems.latestVersions(children(new ItemReference(agencyId, studyUnitId), PHYSICAL_INSTANCE));
        // Une descente PI → DataRelationship → Variable par PI, indépendantes les unes des autres :
        // lancées en parallèle, la réponse coûte la PI la plus lente plutôt que la somme (deux appels
        // bysubject ou plus par PI, ~6 s en série contre ~1 s mesurés sur 16 PI de la démo).
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<List<CodeListVariableUsage>>> perPhysicalInstance = physicalInstances.stream()
                    .map(physicalInstance -> executor.submit(() -> usagesOf(agencyId, studyUnitId, physicalInstance)))
                    .toList();
            List<CodeListVariableUsage> usages = new ArrayList<>();
            for (Future<List<CodeListVariableUsage>> future : perPhysicalInstance) {
                usages.addAll(future.get());
            }
            return usages.stream().distinct().toList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted while fetching variable usages of study unit " + agencyId + "/" + studyUnitId, e);
        } catch (ExecutionException e) {
            // Remonte l'exception d'origine (404 Colectica, etc.) telle que le code séquentiel la levait.
            if (e.getCause() instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new IllegalStateException(
                    "Failed to fetch variable usages of study unit " + agencyId + "/" + studyUnitId, e.getCause());
        }
    }

    /** Les variables de chaque DataRelationship de {@code physicalInstance}, une ligne par variable. */
    private List<CodeListVariableUsage> usagesOf(String agencyId, String studyUnitId, ColecticaItem physicalInstance) {
        List<CodeListVariableUsage> usages = new ArrayList<>();
        List<ItemReference> dataRelationships = ColecticaRelationships.childrenOfType(
                colecticaClient, ColecticaItems.itemRef(physicalInstance), itemType(DATA_RELATIONSHIP));
        for (ItemReference dataRelationship : dataRelationships) {
            for (ColecticaItem variable : ColecticaItems.latestVersions(children(dataRelationship, VARIABLE))) {
                usages.add(new CodeListVariableUsage(
                        agencyId,
                        studyUnitId,
                        null,
                        physicalInstance.agencyId(),
                        physicalInstance.identifier(),
                        labels.of(physicalInstance),
                        variable.agencyId(),
                        variable.identifier(),
                        labels.of(variable)));
            }
        }
        return usages;
    }

    /**
     * Variables de {@code variableScheme} : celles qu'il référence directement et celles de ses
     * VariableGroup, à toute profondeur. Chaque groupe n'est visité qu'une fois, ce qui protège d'un
     * cycle de références entre groupes.
     */
    private List<ColecticaItem> variablesUnder(ItemReference variableScheme) {
        List<ColecticaItem> variables = new ArrayList<>();
        Set<ItemReference> visited = new HashSet<>();
        Deque<ItemReference> toVisit = new ArrayDeque<>(List.of(variableScheme));
        while (!toVisit.isEmpty()) {
            ItemReference container = toVisit.pop();
            if (!visited.add(container)) {
                continue;
            }
            variables.addAll(children(container, VARIABLE));
            toVisit.addAll(ColecticaRelationships.childrenOfType(colecticaClient, container, itemType(VARIABLE_GROUP)));
        }
        return variables;
    }

    /** Items de {@code typeKey} directement référencés par {@code subject}, avec leur libellé. */
    private List<ColecticaItem> children(ItemReference subject, String typeKey) {
        return colecticaClient.findRelatedItems(RelationshipDirection.BY_SUBJECT, subject, List.of(itemType(typeKey)));
    }

    private static Ddi4Response variablesOnly(List<Ddi4Variable> variables) {
        return new Ddi4Response(Ddi4Response.SCHEMA, null, null, null, variables, null, null, null);
    }

    private String itemType(String typeKey) {
        return instanceConfiguration.itemTypes().get(typeKey);
    }
}
