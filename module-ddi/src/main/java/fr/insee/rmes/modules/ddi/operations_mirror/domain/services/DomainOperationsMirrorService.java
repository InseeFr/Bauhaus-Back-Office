package fr.insee.rmes.modules.ddi.operations_mirror.domain.services;

import fr.insee.rmes.modules.commons.hexagonal.DomainService;
import fr.insee.rmes.modules.ddi.operations_mirror.domain.model.DdiMirrorIdentity;
import fr.insee.rmes.modules.ddi.operations_mirror.domain.model.DdiShortName;
import fr.insee.rmes.modules.ddi.operations_mirror.domain.port.clientside.OperationsMirrorService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CategoryScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4LogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedRepresentationScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4VariableScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangString;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.GroupService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.StudyUnitService;
import fr.insee.rmes.modules.operation.domain.event.BilingualLabel;
import fr.insee.rmes.modules.operation.domain.event.OperationSaved;
import fr.insee.rmes.modules.operation.domain.event.SeriesSaved;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Traduit les événements du domaine « opérations » en objets DDI.
 * <p>
 * Le dépôt RDF reste la source de vérité : chaque écriture y est rejouée à l'identique côté DDI,
 * sur un objet dont l'identifiant est dérivé de l'IRI de la ressource ({@link DdiMirrorIdentity}),
 * si bien qu'une création et une modification empruntent le même chemin. La version de l'objet DDI
 * n'est jamais incrémentée : on réécrit toujours celle qui est déjà en place.
 */
@DomainService
public class DomainOperationsMirrorService implements OperationsMirrorService {

    private static final Logger logger = LoggerFactory.getLogger(DomainOperationsMirrorService.class);

    private static final String SERIES_TYPE_OF_GROUP = "insee:StatisticalOperationSeries";
    private static final String FIRST_VERSION = "1";

    private static final String LOGICAL_PRODUCT_ROLE = "logicalproduct";
    private static final String VARIABLE_SCHEME_ROLE = "variablescheme";
    private static final String LOGICAL_PRODUCT_NAME_PREFIX = "LP-";
    private static final String VARIABLE_SCHEME_NAME_PREFIX = "VS-";
    private static final String VARIABLE_SCHEME_LABEL_PREFIX = "Ensemble de variables ";
    private static final String CODE_LIST_SCHEME_ROLE = "codelistscheme";
    private static final String CATEGORY_SCHEME_ROLE = "categoryscheme";
    private static final String MANAGED_REPRESENTATION_SCHEME_ROLE = "managedrepresentationscheme";
    private static final String CODE_LIST_SCHEME_LABEL_PREFIX = "Ensemble de listes de codes ";
    private static final String CATEGORY_SCHEME_LABEL_PREFIX = "Ensemble de catégories ";
    private static final String MANAGED_REPRESENTATION_SCHEME_LABEL_PREFIX = "Ensemble de représentations ";

    private final GroupService groupService;
    private final StudyUnitService studyUnitService;
    private final DDIService ddiService;
    private final String agencyId;
    private final List<String> languages;

    public DomainOperationsMirrorService(
            GroupService groupService,
            StudyUnitService studyUnitService,
            DDIService ddiService,
            String agencyId,
            List<String> languages) {
        this.groupService = groupService;
        this.studyUnitService = studyUnitService;
        this.ddiService = ddiService;
        this.agencyId = agencyId;
        this.languages = languages;
    }

    @Override
    public void mirror(SeriesSaved event) {
        String groupId = DdiMirrorIdentity.of(event.iri());
        Optional<Ddi4Group> existing = groupService.find(agencyId, groupId);
        String version = existing.map(Ddi4Group::version).orElse(FIRST_VERSION);
        logger.info("Mirroring series {} as DDI group {} (version {})", event.iri(), groupId, version);

        // Comme pour une opération, le LogicalProduct et ses schemes passent avant le Group qui les
        // référence. Un Group qui a déjà le sien (créé par l'init, ou à un précédent enregistrement)
        // le garde : on n'en fabrique pas un second.
        List<Reference> logicalProducts = existing.map(Ddi4Group::logicalProductReference)
                .filter(references -> !references.isEmpty())
                .orElseGet(() -> List.of(
                        createGroupLogicalProduct(event.iri(), shortLabelOf(event.prefLabel(), event.altLabel()))));

        groupService.createOrUpdate(new Ddi4Group(
                Ddi4Group.TYPE,
                now(),
                DdiMirrorIdentity.urn(agencyId, groupId, version),
                agencyId,
                groupId,
                version,
                null,
                citationOf(event.prefLabel(), event.altLabel()),
                existing.map(Ddi4Group::studyUnitReference).orElse(null),
                seriesIrisOf(existing, event.iri()),
                SERIES_TYPE_OF_GROUP,
                logicalProducts));
    }

    @Override
    public void mirror(OperationSaved event) {
        String studyUnitId = DdiMirrorIdentity.of(event.iri());
        String shortLabel = shortLabelOf(event.prefLabel(), event.altLabel());
        logger.info("Mirroring operation {} as DDI study unit {}", event.iri(), studyUnitId);

        // La VariableScheme avant le LogicalProduct qui la classe, lui-même avant la StudyUnit qui le
        // classe : Colectica fabrique des coquilles vides pour les items référencés qu'il ne connaît
        // pas encore, et ces coquilles ne se laissent pas réécrire à version égale.
        Reference logicalProduct = createStudyUnitLogicalProduct(event.iri(), shortLabel);
        Optional<Ddi4StudyUnit> existing = studyUnitService.find(agencyId, studyUnitId);
        String version = existing.map(Ddi4StudyUnit::version).orElse(FIRST_VERSION);

        studyUnitService.createOrUpdate(new Ddi4StudyUnit(
                Ddi4StudyUnit.TYPE,
                now(),
                DdiMirrorIdentity.urn(agencyId, studyUnitId, version),
                agencyId,
                studyUnitId,
                version,
                new Citation(langStrings(event.prefLabel())),
                event.iri(),
                existing.map(Ddi4StudyUnit::physicalInstanceReferences).orElse(null),
                List.of(logicalProduct)));

        if (event.seriesIri() != null) {
            fileUnderSeriesGroup(event.seriesIri(), studyUnitId, version);
        }
    }

    /**
     * Crée la VariableScheme de l'opération puis le LogicalProduct qui la classe, et rend la
     * référence vers ce LogicalProduct. Tous deux portent un {@code Name} dérivé du nom court de
     * l'opération, distinct de leur libellé.
     */
    private Reference createStudyUnitLogicalProduct(String operationIri, String shortLabel) {
        String variableSchemeId = DdiMirrorIdentity.of(operationIri, VARIABLE_SCHEME_ROLE);
        ddiService.createVariableScheme(new Ddi4VariableScheme(
                Ddi4VariableScheme.TYPE,
                now(),
                DdiMirrorIdentity.urn(agencyId, variableSchemeId, FIRST_VERSION),
                agencyId,
                variableSchemeId,
                FIRST_VERSION,
                labelIn(languages.get(0), VARIABLE_SCHEME_LABEL_PREFIX + shortLabel),
                List.of(),
                DdiShortName.prefixed(VARIABLE_SCHEME_NAME_PREFIX, shortLabel)));

        String logicalProductId = DdiMirrorIdentity.of(operationIri, LOGICAL_PRODUCT_ROLE);
        ddiService.createLogicalProduct(new Ddi4LogicalProduct(
                Ddi4LogicalProduct.TYPE,
                now(),
                DdiMirrorIdentity.urn(agencyId, logicalProductId, FIRST_VERSION),
                agencyId,
                logicalProductId,
                FIRST_VERSION,
                labelIn(languages.get(0), shortLabel),
                null,
                null,
                List.of(Reference.of(agencyId, variableSchemeId, FIRST_VERSION, "VariableScheme")),
                null,
                DdiShortName.prefixed(LOGICAL_PRODUCT_NAME_PREFIX, shortLabel)));

        return Reference.of(agencyId, logicalProductId, FIRST_VERSION, "LogicalProduct");
    }

    /**
     * Classe la StudyUnit sous le Group de sa série, si elle ne l'est pas déjà. Le Group n'est
     * réécrit que lorsque la référence y manque : une simple modification de libellé d'opération ne
     * doit pas faire bouger le Group.
     */
    private void fileUnderSeriesGroup(String seriesIri, String studyUnitId, String studyUnitVersion) {
        String groupId = DdiMirrorIdentity.of(seriesIri);
        Optional<Ddi4Group> group = groupService.find(agencyId, groupId);
        if (group.isEmpty()) {
            logger.warn(
                    "No DDI group {} for series {}: study unit {} stays unfiled until the series is saved again",
                    groupId,
                    seriesIri,
                    studyUnitId);
            return;
        }

        Reference studyUnitReference = Reference.of(agencyId, studyUnitId, studyUnitVersion, "StudyUnit");
        List<Reference> filed = group.get().studyUnitReference() == null
                ? List.of()
                : group.get().studyUnitReference();
        if (filed.stream().anyMatch(reference -> studyUnitId.equals(reference.id()))) {
            return;
        }

        List<Reference> updated = new ArrayList<>(filed);
        updated.add(studyUnitReference);
        groupService.createOrUpdate(new Ddi4Group(
                Ddi4Group.TYPE,
                now(),
                DdiMirrorIdentity.urn(agencyId, groupId, group.get().version()),
                agencyId,
                groupId,
                group.get().version(),
                null,
                group.get().citation(),
                updated,
                group.get().seriesIris(),
                group.get().typeOfGroup(),
                group.get().logicalProductReference()));
    }

    /**
     * Crée les schemes (initialement vides) du Group d'une série — CodeListScheme, CategoryScheme,
     * ManagedRepresentationScheme — puis le LogicalProduct qui les classe, et rend la référence vers
     * ce LogicalProduct. La VariableScheme n'en fait pas partie : elle vit dans le LogicalProduct de
     * chaque StudyUnit.
     */
    private Reference createGroupLogicalProduct(String seriesIri, String shortLabel) {
        String codeListSchemeId = DdiMirrorIdentity.of(seriesIri, CODE_LIST_SCHEME_ROLE);
        ddiService.createCodeListScheme(new Ddi4CodeListScheme(
                Ddi4CodeListScheme.TYPE,
                now(),
                DdiMirrorIdentity.urn(agencyId, codeListSchemeId, FIRST_VERSION),
                agencyId,
                codeListSchemeId,
                FIRST_VERSION,
                labelIn(languages.get(0), CODE_LIST_SCHEME_LABEL_PREFIX + shortLabel),
                List.of()));

        String categorySchemeId = DdiMirrorIdentity.of(seriesIri, CATEGORY_SCHEME_ROLE);
        ddiService.createCategoryScheme(new Ddi4CategoryScheme(
                Ddi4CategoryScheme.TYPE,
                now(),
                DdiMirrorIdentity.urn(agencyId, categorySchemeId, FIRST_VERSION),
                agencyId,
                categorySchemeId,
                FIRST_VERSION,
                labelIn(languages.get(0), CATEGORY_SCHEME_LABEL_PREFIX + shortLabel),
                List.of()));

        String managedRepresentationSchemeId = DdiMirrorIdentity.of(seriesIri, MANAGED_REPRESENTATION_SCHEME_ROLE);
        ddiService.createManagedRepresentationScheme(new Ddi4ManagedRepresentationScheme(
                Ddi4ManagedRepresentationScheme.TYPE,
                now(),
                DdiMirrorIdentity.urn(agencyId, managedRepresentationSchemeId, FIRST_VERSION),
                agencyId,
                managedRepresentationSchemeId,
                FIRST_VERSION,
                labelIn(languages.get(0), MANAGED_REPRESENTATION_SCHEME_LABEL_PREFIX + shortLabel),
                List.of()));

        String logicalProductId = DdiMirrorIdentity.of(seriesIri, LOGICAL_PRODUCT_ROLE);
        ddiService.createLogicalProduct(new Ddi4LogicalProduct(
                Ddi4LogicalProduct.TYPE,
                now(),
                DdiMirrorIdentity.urn(agencyId, logicalProductId, FIRST_VERSION),
                agencyId,
                logicalProductId,
                FIRST_VERSION,
                labelIn(languages.get(0), shortLabel),
                List.of(Reference.of(agencyId, codeListSchemeId, FIRST_VERSION, "CodeListScheme")),
                List.of(Reference.of(agencyId, categorySchemeId, FIRST_VERSION, "CategoryScheme")),
                null,
                List.of(Reference.of(
                        agencyId, managedRepresentationSchemeId, FIRST_VERSION, "ManagedRepresentationScheme")),
                DdiShortName.prefixed(LOGICAL_PRODUCT_NAME_PREFIX, shortLabel)));

        return Reference.of(agencyId, logicalProductId, FIRST_VERSION, "LogicalProduct");
    }

    /**
     * Le nom court de la ressource, sur lequel se construisent les libellés et les noms de son
     * LogicalProduct et de ses schemes. À défaut de nom court, le libellé long en tient lieu : ces
     * objets ont besoin d'un libellé quoi qu'il arrive.
     */
    private static String shortLabelOf(BilingualLabel prefLabel, BilingualLabel altLabel) {
        if (altLabel != null && !altLabel.isEmpty()) {
            return altLabel.lg1() != null ? altLabel.lg1() : altLabel.lg2();
        }
        return prefLabel == null ? "" : prefLabel.lg1();
    }

    private static List<LangString> labelIn(String language, String value) {
        return value == null || value.isBlank() ? null : List.of(new LangString(language, value));
    }

    /**
     * Les IRIs de série que porte le Group, celle de l'événement comprise.
     * <p>
     * Ces {@code r:UserID} sont la clé par laquelle les stamps des utilisateurs sont résolus : un
     * Group qui en référence plusieurs ne doit pas perdre les autres parce qu'une seule série vient
     * d'être enregistrée.
     */
    private static List<String> seriesIrisOf(Optional<Ddi4Group> existing, String seriesIri) {
        List<String> carried = existing.map(Ddi4Group::seriesIris).orElse(null);
        if (carried == null || carried.isEmpty()) {
            return List.of(seriesIri);
        }
        if (carried.contains(seriesIri)) {
            return carried;
        }
        List<String> merged = new ArrayList<>(carried);
        merged.add(seriesIri);
        return merged;
    }

    /**
     * Le libellé long devient le titre, le libellé court le titre alternatif. Une langue sans
     * libellé n'est pas écrite, et une citation sans titre alternatif n'en porte aucun plutôt qu'une
     * liste vide : le JSON DDI4 ne tolère pas de champ nul et l'XML DDI 3.3 pas d'élément vide.
     */
    private Citation citationOf(BilingualLabel prefLabel, BilingualLabel altLabel) {
        return new Citation(langStrings(prefLabel), langStrings(altLabel));
    }

    private List<LangString> langStrings(BilingualLabel label) {
        if (label == null || label.isEmpty()) {
            return null;
        }
        List<LangString> values = new ArrayList<>();
        addIfPresent(values, languages.get(0), label.lg1());
        if (languages.size() > 1) {
            addIfPresent(values, languages.get(1), label.lg2());
        }
        return values;
    }

    private static void addIfPresent(List<LangString> values, String language, String value) {
        if (value != null && !value.isBlank()) {
            values.add(new LangString(language, value));
        }
    }

    private static CogsDate now() {
        return CogsDate.ofDateTime(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }
}
