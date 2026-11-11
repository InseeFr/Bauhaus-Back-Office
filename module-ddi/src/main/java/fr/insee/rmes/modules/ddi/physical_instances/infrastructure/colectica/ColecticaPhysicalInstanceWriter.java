package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static fr.insee.rmes.domain.logging.LogSanitizer.forLog;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.DATA_RELATIONSHIP;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.PHYSICAL_INSTANCE;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.STUDY_UNIT;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaXml.REUSABLE_NS;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaXml.STUDY_UNIT_NS;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.dto.ColecticaCreateItemRequest;
import fr.insee.rmes.colectica.client.dto.ColecticaItemResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.DdiItemNotFoundException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions.InvalidReuseVariablesException;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.BasedOnObject;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CreatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4DataRelationship;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.DuplicatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangString;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangStrings;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LogicalRecord;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceIds;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceParents;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.ReuseVariablesRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.RolloverRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.RolloverVariable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UpdatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.VariablesInRecord;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Créations et mises à jour d'une PhysicalInstance et de ses objets DDI. */
class ColecticaPhysicalInstanceWriter {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaPhysicalInstanceWriter.class);

    /** Une copie démarre à la version 1, comme toute création. */
    private static final String COPY_VERSION = "1";

    // Libellés par défaut de la copie, alignés sur ceux posés par le front à la création.
    private static final String DATA_RELATIONSHIP_LABEL_PREFIX = "Structure : ";
    private static final String LOGICAL_RECORD_LABEL_PREFIX = "Enregistrement logique : ";

    private final ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;
    private final ColecticaClient colecticaClient;
    private final DDI4toDDI3ConverterService ddi4ToDdi3Converter;
    private final ColecticaPhysicalInstanceReader reader;
    private final ColecticaSchemeFiler schemeFiler;
    private final ColecticaLabels labels;

    ColecticaPhysicalInstanceWriter(
            ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration,
            ColecticaClient colecticaClient,
            DDI4toDDI3ConverterService ddi4ToDdi3Converter,
            ColecticaPhysicalInstanceReader reader,
            ColecticaSchemeFiler schemeFiler,
            ColecticaLabels labels) {
        this.instanceConfiguration = instanceConfiguration;
        this.colecticaClient = colecticaClient;
        this.ddi4ToDdi3Converter = ddi4ToDdi3Converter;
        this.reader = reader;
        this.schemeFiler = schemeFiler;
        this.labels = labels;
    }

    Ddi4Response createPhysicalInstance(CreatePhysicalInstanceRequest request) {
        return createPhysicalInstance(
                request,
                new PhysicalInstanceIds(
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString()));
    }

    Ddi4Response createPhysicalInstance(CreatePhysicalInstanceRequest request, PhysicalInstanceIds ids) {
        String physicalInstanceId = ids.physicalInstance();
        String dataRelationshipId = ids.dataRelationship();
        String logicalRecordId = ids.logicalRecord();
        String agencyId = instanceConfiguration.defaultAgencyId();
        int version = 1;
        String versionDate = ColecticaDates.nowIso();

        String physicalInstanceXml = buildPhysicalInstanceXml(
                agencyId,
                physicalInstanceId,
                version,
                request.physicalInstanceLabel(),
                dataRelationshipId,
                versionDate);

        String dataRelationshipXml = buildDataRelationshipXml(
                agencyId,
                dataRelationshipId,
                version,
                request.dataRelationshipLabel(),
                logicalRecordId,
                request.logicalRecordLabel() != null ? request.logicalRecordLabel() : request.physicalInstanceLabel(),
                versionDate);

        List<ColecticaItemResponse> itemsToCreate = new ArrayList<>(List.of(
                newItem(PHYSICAL_INSTANCE, agencyId, version, physicalInstanceId, physicalInstanceXml, versionDate),
                newItem(DATA_RELATIONSHIP, agencyId, version, dataRelationshipId, dataRelationshipXml, versionDate)));

        if (request.studyUnitId() != null && request.studyUnitAgency() != null) {
            itemsToCreate.add(addPhysicalInstanceReferenceToStudyUnit(
                    request.studyUnitAgency(), request.studyUnitId(), agencyId, physicalInstanceId));
        }

        colecticaClient.createOrUpdateItems(new ColecticaCreateItemRequest(itemsToCreate));

        return reader.getPhysicalInstance(agencyId, physicalInstanceId);
    }

    private ColecticaItemResponse newItem(
            String typeKey, String agencyId, int version, String id, String xml, String versionDate) {
        return new ColecticaItemResponse(
                instanceConfiguration.itemTypes().get(typeKey),
                agencyId,
                version,
                id,
                xml,
                versionDate,
                instanceConfiguration.versionResponsibility(),
                false,
                false,
                false,
                instanceConfiguration.itemFormat());
    }

    /**
     * Ne réécrit que la PhysicalInstance, sa DataRelationship et, le cas échéant, la StudyUnit de
     * rattachement : variables, listes de codes et catégories restent intactes dans Colectica, et
     * aucun LogicalProduct ni scheme n'est donc requis.
     *
     * @return la PhysicalInstance telle qu'enregistrée (libellé et {@code versionDate} à jour)
     */
    Ddi4PhysicalInstance updatePhysicalInstance(String agencyId, String id, UpdatePhysicalInstanceRequest request) {
        Ddi4Response currentInstance = reader.getPhysicalInstance(agencyId, id);

        if (currentInstance == null
                || currentInstance.physicalInstance() == null
                || currentInstance.physicalInstance().isEmpty()) {
            throw DdiItemNotFoundException.physicalInstance(agencyId, id);
        }

        Ddi4PhysicalInstance currentPI = currentInstance.physicalInstance().getFirst();
        Ddi4DataRelationship currentDR = currentInstance.dataRelationship() != null
                        && !currentInstance.dataRelationship().isEmpty()
                ? currentInstance.dataRelationship().getFirst()
                : null;

        String versionDate = ColecticaDates.nowIso();

        LangString currentTitle = currentPI.citation().title().get(0);
        String newPhysicalInstanceLabel =
                request.physicalInstanceLabel() != null ? request.physicalInstanceLabel() : currentTitle.value();

        Ddi4PhysicalInstance updatedPI = new Ddi4PhysicalInstance(
                Ddi4PhysicalInstance.TYPE,
                CogsDate.ofDateTime(versionDate),
                currentPI.urn(),
                currentPI.agency(),
                currentPI.id(),
                currentPI.version(),
                currentPI.basedOnObject(),
                new Citation(LangStrings.of(currentTitle.language(), newPhysicalInstanceLabel)),
                currentPI.dataRelationshipReference());

        Ddi4Response updatedResponse = new Ddi4Response(
                currentInstance.schema(),
                currentInstance.topLevelReference(),
                List.of(updatedPI),
                currentDR == null ? null : List.of(updatedDataRelationship(currentDR, request, versionDate)),
                null,
                null,
                null,
                null);

        List<ColecticaItemResponse> colecticaItems = toColecticaItems(updatedResponse);
        if (request.attachesToStudyUnit()) {
            colecticaItems.add(addPhysicalInstanceReferenceToStudyUnit(
                    request.studyUnitAgency(), request.studyUnitId(), agencyId, id));
        }

        logger.info("Sending physical instance update to Colectica with {} items", colecticaItems.size());
        colecticaClient.createOrUpdateItems(new ColecticaCreateItemRequest(colecticaItems));
        return updatedPI;
    }

    /**
     * Copie une PhysicalInstance : la PI, sa DataRelationship, son LogicalRecord et ses variables
     * reçoivent de nouveaux identifiants et pointent sur leur original via {@code BasedOnObject} ;
     * les listes de codes et catégories restent référencées telles quelles, sans être réécrites.
     *
     * <p>La copie, la StudyUnit de rattachement et le VariableScheme qui range les nouvelles variables
     * partent dans un seul enregistrement. Les parents étant portés par la requête, un scheme manquant
     * sous l'Étude lève une {@code MissingSchemeException} avant tout envoi.
     */
    Reference duplicatePhysicalInstance(String agencyId, String id, DuplicatePhysicalInstanceRequest request) {
        Ddi4Response source = reader.getPhysicalInstance(agencyId, id);
        if (source == null
                || source.physicalInstance() == null
                || source.physicalInstance().isEmpty()) {
            throw new RuntimeException("Physical instance not found: " + agencyId + "/" + id);
        }

        CogsDate versionDate = CogsDate.ofDateTime(ColecticaDates.nowIso());
        String label = request.physicalInstanceLabel();

        Map<String, String> variableIds = new LinkedHashMap<>();
        List<Ddi4Variable> copiedVariables = orEmpty(source.variable()).stream()
                .map(variable -> {
                    String copyId = UUID.randomUUID().toString();
                    variableIds.put(variable.id(), copyId);
                    return new Ddi4Variable(
                            Ddi4Variable.TYPE,
                            versionDate,
                            Reference.synthesizeUrn(agencyId, copyId, COPY_VERSION),
                            agencyId,
                            copyId,
                            COPY_VERSION,
                            basedOn(variable.agency(), variable.id(), variable.version(), Ddi4Variable.TYPE),
                            variable.variableName(),
                            variable.label(),
                            variable.description(),
                            variable.variableRepresentation(),
                            variable.isGeographic(),
                            variable.versionResponsibility());
                })
                .toList();
        List<Reference> copiedVariableReferences = variableIds.values().stream()
                .map(copyId -> Reference.of(agencyId, copyId, COPY_VERSION, Ddi4Variable.TYPE))
                .toList();

        List<Ddi4DataRelationship> copiedDataRelationships = orEmpty(source.dataRelationship()).stream()
                .limit(1)
                .map(dataRelationship -> copiedDataRelationship(
                        dataRelationship,
                        agencyId,
                        label,
                        request.dataRelationshipLabel(),
                        request.logicalRecordLabel(),
                        versionDate,
                        copiedVariableReferences))
                .toList();

        Ddi4PhysicalInstance sourcePI = source.physicalInstance().getFirst();
        String copyId = UUID.randomUUID().toString();
        Ddi4PhysicalInstance copiedPI = new Ddi4PhysicalInstance(
                Ddi4PhysicalInstance.TYPE,
                versionDate,
                Reference.synthesizeUrn(agencyId, copyId, COPY_VERSION),
                agencyId,
                copyId,
                COPY_VERSION,
                basedOn(sourcePI.agency(), sourcePI.id(), sourcePI.version(), Ddi4PhysicalInstance.TYPE),
                new Citation(labels.withFallback(
                        sourcePI.citation() == null ? null : sourcePI.citation().title(), label)),
                copiedDataRelationships.stream()
                        .map(dr -> Reference.of(agencyId, dr.id(), COPY_VERSION, Ddi4DataRelationship.TYPE))
                        .toList());

        Ddi4Response copy = new Ddi4Response(
                Ddi4Response.SCHEMA,
                List.of(Reference.of(agencyId, copyId, COPY_VERSION, Ddi4PhysicalInstance.TYPE)),
                List.of(copiedPI),
                copiedDataRelationships.isEmpty() ? null : copiedDataRelationships,
                copiedVariables.isEmpty() ? null : copiedVariables,
                null,
                null,
                null);

        List<ColecticaItemResponse> studyUnit = List.of(addPhysicalInstanceReferenceToStudyUnit(
                request.studyUnitAgency(), request.studyUnitId(), agencyId, copyId));
        PhysicalInstanceParents parents = new PhysicalInstanceParents(
                request.studyUnitAgency(), request.studyUnitId(), request.groupAgency(), request.groupId());
        updateFullPhysicalInstance(agencyId, copyId, copy, studyUnit, parents);

        return Reference.of(agencyId, copyId, COPY_VERSION, Ddi4PhysicalInstance.TYPE);
    }

    /**
     * Crée une PhysicalInstance dans l'Étude de la source, qui réutilise les variables choisies sans
     * les copier (#1674) : la PI, sa DataRelationship et son LogicalRecord sont neufs, la PI pointe sur
     * la source via {@code BasedOnObject}, et le LogicalRecord référence les variables telles que
     * stockées. Ni les variables ni le VariableScheme de l'Étude, qui les range déjà, ne sont réécrits.
     *
     * @param parents les parents de la source : la nouvelle PI rejoint sa StudyUnit
     * @throws InvalidReuseVariablesException si le groupe de la requête n'est pas celui de la source,
     *     ou si une variable demandée n'est pas utilisée par la source
     */
    Reference reuseVariables(
            String agencyId, String id, ReuseVariablesRequest request, PhysicalInstanceParents parents) {
        if (!parents.groupAgency().equals(request.groupAgency())
                || !parents.groupId().equals(request.groupId())) {
            throw InvalidReuseVariablesException.groupMismatch(request.groupAgency(), request.groupId());
        }
        Ddi4Response source = reader.getPhysicalInstance(agencyId, id);
        if (source == null
                || source.physicalInstance() == null
                || source.physicalInstance().isEmpty()) {
            throw DdiItemNotFoundException.physicalInstance(agencyId, id);
        }

        Set<String> selectedIds = Set.copyOf(request.variableIds());
        List<Ddi4Variable> sourceVariables = orEmpty(source.variable());
        Set<String> sourceIds = sourceVariables.stream().map(Ddi4Variable::id).collect(Collectors.toSet());
        List<String> unknownIds = request.variableIds().stream()
                .filter(variableId -> !sourceIds.contains(variableId))
                .toList();
        if (!unknownIds.isEmpty()) {
            throw InvalidReuseVariablesException.notInSource(unknownIds);
        }
        // Ordre de la source, versions stockées : la nouvelle PI partage les mêmes variables.
        List<Reference> reusedVariableReferences = sourceVariables.stream()
                .filter(variable -> selectedIds.contains(variable.id()))
                .map(variable -> Reference.of(
                        variable.agency(),
                        variable.id(),
                        variable.version() != null ? variable.version() : COPY_VERSION,
                        Ddi4Variable.TYPE))
                .toList();

        CogsDate versionDate = CogsDate.ofDateTime(ColecticaDates.nowIso());
        String label = request.physicalInstanceLabel();
        List<Ddi4DataRelationship> newDataRelationships = orEmpty(source.dataRelationship()).stream()
                .limit(1)
                .map(dataRelationship -> copiedDataRelationship(
                        dataRelationship,
                        agencyId,
                        label,
                        request.dataRelationshipLabel(),
                        request.logicalRecordLabel(),
                        versionDate,
                        reusedVariableReferences))
                .toList();

        Ddi4PhysicalInstance sourcePI = source.physicalInstance().getFirst();
        String newId = UUID.randomUUID().toString();
        Ddi4PhysicalInstance newPI = new Ddi4PhysicalInstance(
                Ddi4PhysicalInstance.TYPE,
                versionDate,
                Reference.synthesizeUrn(agencyId, newId, COPY_VERSION),
                agencyId,
                newId,
                COPY_VERSION,
                basedOn(sourcePI.agency(), sourcePI.id(), sourcePI.version(), Ddi4PhysicalInstance.TYPE),
                new Citation(labels.withFallback(
                        sourcePI.citation() == null ? null : sourcePI.citation().title(), label)),
                newDataRelationships.stream()
                        .map(dr -> Reference.of(agencyId, dr.id(), COPY_VERSION, Ddi4DataRelationship.TYPE))
                        .toList());

        Ddi4Response newInstance = new Ddi4Response(
                Ddi4Response.SCHEMA,
                List.of(Reference.of(agencyId, newId, COPY_VERSION, Ddi4PhysicalInstance.TYPE)),
                List.of(newPI),
                newDataRelationships.isEmpty() ? null : newDataRelationships,
                null,
                null,
                null,
                null);

        List<ColecticaItemResponse> studyUnit = List.of(addPhysicalInstanceReferenceToStudyUnit(
                parents.studyUnitAgency(), parents.studyUnitId(), agencyId, newId));
        updateFullPhysicalInstance(agencyId, newId, newInstance, studyUnit, parents);

        return Reference.of(agencyId, newId, COPY_VERSION, Ddi4PhysicalInstance.TYPE);
    }

    /**
     * Nouvelle période d'une PhysicalInstance (#1672), dans une autre étude du groupe : la PI, sa
     * DataRelationship et son LogicalRecord sont neufs et pointent sur la source via
     * {@code BasedOnObject}. Chaque variable choisie est copiée avec un {@code BasedOn} vers son
     * original, sauf celles de {@code reusedBySourceId}, déjà dérivées de la source dans l'étude
     * cible : le LogicalRecord les référence telles quelles, sans les réécrire. Listes de codes et
     * catégories restent référencées par les copies sans être réécrites.
     *
     * <p>La PI, l'étude cible et son VariableScheme, qui range les copies, partent dans un seul
     * enregistrement ; un scheme manquant lève une {@code MissingSchemeException} avant tout envoi.
     *
     * @param source la PI source, lue et validée par l'appelant
     */
    Reference rollover(Ddi4Response source, RolloverRequest request, Map<String, Reference> reusedBySourceId) {
        Ddi4PhysicalInstance sourcePI = source.physicalInstance().getFirst();
        // Comme la duplication : les nouveaux objets sont créés sous l'agence de la source.
        String agencyId = sourcePI.agency();
        CogsDate versionDate = CogsDate.ofDateTime(ColecticaDates.nowIso());
        String label = request.physicalInstanceLabel();
        Set<String> selectedIds =
                request.variables().stream().map(RolloverVariable::sourceId).collect(Collectors.toSet());

        // Ordre de la source : chaque variable choisie est soit remplacée par sa dérivée de l'étude
        // cible, soit copiée.
        List<Ddi4Variable> copiedVariables = new ArrayList<>();
        List<Reference> variableReferences = new ArrayList<>();
        for (Ddi4Variable variable : orEmpty(source.variable())) {
            if (!selectedIds.contains(variable.id())) {
                continue;
            }
            Reference reused = reusedBySourceId.get(variable.id());
            if (reused != null) {
                variableReferences.add(reused);
                continue;
            }
            Ddi4Variable copy = copiedVariable(variable, agencyId, versionDate);
            copiedVariables.add(copy);
            variableReferences.add(Reference.of(agencyId, copy.id(), COPY_VERSION, Ddi4Variable.TYPE));
        }

        List<Ddi4DataRelationship> newDataRelationships = orEmpty(source.dataRelationship()).stream()
                .limit(1)
                .map(dataRelationship -> copiedDataRelationship(
                        dataRelationship,
                        agencyId,
                        label,
                        request.dataRelationshipLabel(),
                        request.logicalRecordLabel(),
                        versionDate,
                        variableReferences))
                .toList();

        String newId = UUID.randomUUID().toString();
        Ddi4PhysicalInstance newPI = new Ddi4PhysicalInstance(
                Ddi4PhysicalInstance.TYPE,
                versionDate,
                Reference.synthesizeUrn(agencyId, newId, COPY_VERSION),
                agencyId,
                newId,
                COPY_VERSION,
                basedOn(sourcePI.agency(), sourcePI.id(), sourcePI.version(), Ddi4PhysicalInstance.TYPE),
                new Citation(labels.withFallback(
                        sourcePI.citation() == null ? null : sourcePI.citation().title(), label)),
                newDataRelationships.stream()
                        .map(dr -> Reference.of(agencyId, dr.id(), COPY_VERSION, Ddi4DataRelationship.TYPE))
                        .toList());

        Ddi4Response newInstance = new Ddi4Response(
                Ddi4Response.SCHEMA,
                List.of(Reference.of(agencyId, newId, COPY_VERSION, Ddi4PhysicalInstance.TYPE)),
                List.of(newPI),
                newDataRelationships.isEmpty() ? null : newDataRelationships,
                copiedVariables.isEmpty() ? null : copiedVariables,
                null,
                null,
                null);

        List<ColecticaItemResponse> studyUnit = List.of(addPhysicalInstanceReferenceToStudyUnit(
                request.studyUnitAgency(), request.studyUnitId(), agencyId, newId));
        PhysicalInstanceParents parents = new PhysicalInstanceParents(
                request.studyUnitAgency(), request.studyUnitId(), request.groupAgency(), request.groupId());
        updateFullPhysicalInstance(agencyId, newId, newInstance, studyUnit, parents);

        return Reference.of(agencyId, newId, COPY_VERSION, Ddi4PhysicalInstance.TYPE);
    }

    /** Copie d'une variable : nouvel identifiant en version 1, {@code BasedOn} vers l'originale. */
    private static Ddi4Variable copiedVariable(Ddi4Variable variable, String agencyId, CogsDate versionDate) {
        String copyId = UUID.randomUUID().toString();
        return new Ddi4Variable(
                Ddi4Variable.TYPE,
                versionDate,
                Reference.synthesizeUrn(agencyId, copyId, COPY_VERSION),
                agencyId,
                copyId,
                COPY_VERSION,
                basedOn(variable.agency(), variable.id(), variable.version(), Ddi4Variable.TYPE),
                variable.variableName(),
                variable.label(),
                variable.description(),
                variable.variableRepresentation(),
                variable.isGeographic(),
                variable.versionResponsibility());
    }

    private Ddi4DataRelationship copiedDataRelationship(
            Ddi4DataRelationship source,
            String agencyId,
            String label,
            String requestedDataRelationshipLabel,
            String requestedLogicalRecordLabel,
            CogsDate versionDate,
            List<Reference> copiedVariableReferences) {
        String dataRelationshipLabel = requestedDataRelationshipLabel != null
                ? requestedDataRelationshipLabel
                : DATA_RELATIONSHIP_LABEL_PREFIX + label;
        String logicalRecordLabel =
                requestedLogicalRecordLabel != null ? requestedLogicalRecordLabel : LOGICAL_RECORD_LABEL_PREFIX + label;

        List<LogicalRecord> copiedLogicalRecords = orEmpty(source.logicalRecord()).stream()
                .limit(1)
                .map(logicalRecord -> {
                    String copyId = UUID.randomUUID().toString();
                    return new LogicalRecord(
                            LogicalRecord.TYPE,
                            Reference.synthesizeUrn(agencyId, copyId, COPY_VERSION),
                            agencyId,
                            copyId,
                            COPY_VERSION,
                            labels.withFallback(logicalRecord.label(), logicalRecordLabel),
                            new VariablesInRecord(copiedVariableReferences));
                })
                .toList();

        String copyId = UUID.randomUUID().toString();
        return new Ddi4DataRelationship(
                Ddi4DataRelationship.TYPE,
                versionDate,
                Reference.synthesizeUrn(agencyId, copyId, COPY_VERSION),
                agencyId,
                copyId,
                COPY_VERSION,
                basedOn(source.agency(), source.id(), source.version(), Ddi4DataRelationship.TYPE),
                labels.withFallback(source.label(), dataRelationshipLabel),
                copiedLogicalRecords.isEmpty() ? null : copiedLogicalRecords);
    }

    private static BasedOnObject basedOn(String agency, String id, String version, String type) {
        return BasedOnObject.of(List.of(Reference.of(agency, id, version != null ? version : COPY_VERSION, type)));
    }

    private static <T> List<T> orEmpty(List<T> items) {
        return items == null ? List.of() : items;
    }

    private Ddi4DataRelationship updatedDataRelationship(
            Ddi4DataRelationship currentDR, UpdatePhysicalInstanceRequest request, String versionDate) {
        List<LogicalRecord> updatedLRs = currentDR.logicalRecord();
        if (updatedLRs != null && request.logicalRecordLabel() != null) {
            updatedLRs = updatedLRs.stream()
                    .map(lr -> new LogicalRecord(
                            LogicalRecord.TYPE,
                            lr.urn(),
                            lr.agency(),
                            lr.id(),
                            lr.version(),
                            labels.withFallback(lr.label(), request.logicalRecordLabel()),
                            lr.variablesInRecord()))
                    .toList();
        }

        return new Ddi4DataRelationship(
                Ddi4DataRelationship.TYPE,
                CogsDate.ofDateTime(versionDate),
                currentDR.urn(),
                currentDR.agency(),
                currentDR.id(),
                currentDR.version(),
                currentDR.basedOnObject(),
                labels.withFallback(currentDR.label(), request.dataRelationshipLabel()),
                updatedLRs);
    }

    void updateFullPhysicalInstance(String agencyId, String id, Ddi4Response ddi4Response) {
        updateFullPhysicalInstance(agencyId, id, ddi4Response, List.of(), null);
    }

    /**
     * Comme {@link #updateFullPhysicalInstance(String, String, Ddi4Response)}, mais permet à l'appelant
     * de joindre des items Colectica déjà construits dans le même enregistrement atomique — utilisé par
     * la duplication pour rattacher la copie à une StudyUnit.
     *
     * @param knownParents les parents quand l'appelant les connaît déjà ; {@code null} pour les
     *                     résoudre via les relations Colectica
     */
    private void updateFullPhysicalInstance(
            String agencyId,
            String id,
            Ddi4Response ddi4Response,
            List<ColecticaItemResponse> additionalItems,
            PhysicalInstanceParents knownParents) {
        if (logger.isInfoEnabled()) {
            logger.info(
                    "Updating full physical instance {}/{} with all DDI objects in Colectica",
                    forLog(agencyId),
                    forLog(id));
        }

        List<ColecticaItemResponse> colecticaItems = toColecticaItems(ddi4Response);

        // Range les listes de codes et catégories non mutualisées sous les schemes du groupe, et les
        // variables sous le VariableScheme de la study unit. Un scheme manquant lève une
        // MissingSchemeException avant tout envoi. Les parents sont résolus une fois et partagés.
        schemeFiler.appendSchemeUpdates(agencyId, id, ddi4Response, colecticaItems, knownParents);

        // Items supplémentaires (p.ex. la StudyUnit réenregistrée avec une nouvelle référence de PI)
        colecticaItems.addAll(additionalItems);

        logger.info("Sending full update request to Colectica with {} items", colecticaItems.size());
        colecticaClient.createOrUpdateItems(new ColecticaCreateItemRequest(colecticaItems));

        if (logger.isInfoEnabled()) {
            logger.info(
                    "Successfully updated full physical instance with id: {} ({} items saved)",
                    forLog(id),
                    colecticaItems.size());
        }
    }

    private List<ColecticaItemResponse> toColecticaItems(Ddi4Response ddi4Response) {
        Ddi3Response ddi3Response = ddi4ToDdi3Converter.convertDdi4ToDdi3(ddi4Response);

        if (ddi3Response == null
                || ddi3Response.items() == null
                || ddi3Response.items().isEmpty()) {
            throw new RuntimeException("No items to save in DDI4 response");
        }

        return ddi3Response.items().stream()
                .map(ColecticaItems::toColecticaItem)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Ajoute une {@code r:PhysicalInstanceReference} au XML de la StudyUnit et renvoie l'item Colectica
     * correspondant, à enregistrer dans le même batch que la PhysicalInstance.
     */
    private ColecticaItemResponse addPhysicalInstanceReferenceToStudyUnit(
            String studyUnitAgency, String studyUnitId, String physicalInstanceAgency, String physicalInstanceId) {
        ColecticaItemResponse studyUnitItem;
        try {
            studyUnitItem = colecticaClient.getItem(studyUnitAgency, studyUnitId, null);
        } catch (HttpClientErrorException.NotFound _) {
            studyUnitItem = null;
        }
        if (studyUnitItem == null) {
            throw DdiItemNotFoundException.studyUnit(studyUnitAgency, studyUnitId);
        }
        try {
            Document doc = ColecticaXml.parse(studyUnitItem.item());

            NodeList studyUnitNodes = doc.getElementsByTagNameNS(STUDY_UNIT_NS, "StudyUnit");
            if (studyUnitNodes.getLength() == 0) {
                throw new RuntimeException("No StudyUnit element found in XML for id=" + studyUnitId);
            }

            Element piRef = doc.createElementNS(REUSABLE_NS, "r:PhysicalInstanceReference");
            appendTextElement(doc, piRef, "r:Agency", physicalInstanceAgency);
            appendTextElement(doc, piRef, "r:ID", physicalInstanceId);
            appendTextElement(doc, piRef, "r:Version", "1");
            appendTextElement(doc, piRef, "r:TypeOfObject", PHYSICAL_INSTANCE);
            studyUnitNodes.item(0).appendChild(piRef);

            return new ColecticaItemResponse(
                    instanceConfiguration.itemTypes().get(STUDY_UNIT),
                    studyUnitAgency,
                    studyUnitItem.version(),
                    studyUnitId,
                    ColecticaXml.toXmlString(doc.getDocumentElement()),
                    studyUnitItem.versionDate(),
                    studyUnitItem.versionResponsibility(),
                    studyUnitItem.isPublished(),
                    studyUnitItem.isDeprecated(),
                    studyUnitItem.isProvisional(),
                    instanceConfiguration.itemFormat());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to add PhysicalInstanceReference to StudyUnit id=" + studyUnitId, e);
        }
    }

    private static void appendTextElement(Document doc, Element parent, String qualifiedName, String text) {
        Element element = doc.createElementNS(REUSABLE_NS, qualifiedName);
        element.setTextContent(text);
        parent.appendChild(element);
    }

    /**
     * {@code r:VersionResponsibility} issu de {@code colectica.yml}, à insérer juste après
     * {@code r:Version} — l'ordre des éléments de {@code VersionableType} est imposé par le schéma.
     * Chaîne vide quand la propriété n'est pas renseignée.
     */
    private String versionResponsibilityXml() {
        String versionResponsibility = instanceConfiguration.versionResponsibility();
        if (versionResponsibility == null || versionResponsibility.isBlank()) {
            return "";
        }
        return "<r:VersionResponsibility>" + ColecticaXml.escape(versionResponsibility) + "</r:VersionResponsibility>";
    }

    private String buildPhysicalInstanceXml(
            String agencyId, String id, int version, String label, String dataRelationshipId, String versionDate) {
        return String.format(
                """
            <Fragment xmlns:r="ddi:reusable:3_3" xmlns="ddi:instance:3_3">
              <PhysicalInstance isUniversallyUnique="true" versionDate="%s" xmlns="ddi:physicalinstance:3_3">
                <r:URN>urn:ddi:%s:%s:%d</r:URN>
                <r:Agency>%s</r:Agency>
                <r:ID>%s</r:ID>
                <r:Version>%d</r:Version>
                %s
                <r:Citation>
                  <r:Title>
                    <r:String xml:lang="%s">%s</r:String>
                  </r:Title>
                </r:Citation>
                %s
              </PhysicalInstance>
            </Fragment>""",
                ColecticaXml.escape(versionDate),
                ColecticaXml.escape(agencyId),
                ColecticaXml.escape(id),
                version,
                ColecticaXml.escape(agencyId),
                ColecticaXml.escape(id),
                version,
                versionResponsibilityXml(),
                labels.defaultLang(),
                ColecticaXml.escape(label),
                dataRelationshipReferenceXml(agencyId, dataRelationshipId, version));
    }

    private String dataRelationshipReferenceXml(String agencyId, String dataRelationshipId, int version) {
        return String.format("""
            <r:DataRelationshipReference>
              <r:Agency>%s</r:Agency>
              <r:ID>%s</r:ID>
              <r:Version>%d</r:Version>
              <r:TypeOfObject>DataRelationship</r:TypeOfObject>
            </r:DataRelationshipReference>""", ColecticaXml.escape(agencyId), ColecticaXml.escape(dataRelationshipId), version);
    }

    private String buildDataRelationshipXml(
            String agencyId,
            String dataRelationshipId,
            int version,
            String dataRelationshipLabel,
            String logicalRecordId,
            String logicalRecordLabel,
            String versionDate) {
        return String.format(
                """
            <Fragment xmlns:r="ddi:reusable:3_3" xmlns="ddi:instance:3_3">
              <DataRelationship isUniversallyUnique="true" versionDate="%s" xmlns="ddi:logicalproduct:3_3">
                <r:URN>urn:ddi:%s:%s:%d</r:URN>
                <r:Agency>%s</r:Agency>
                <r:ID>%s</r:ID>
                <r:Version>%d</r:Version>
                %s
                <r:Label>
                  <r:Content xml:lang="%s">%s</r:Content>
                </r:Label>
                <LogicalRecord isUniversallyUnique="true">
                  <r:URN>urn:ddi:%s:%s:%d</r:URN>
                  <r:Agency>%s</r:Agency>
                  <r:ID>%s</r:ID>
                  <r:Version>%d</r:Version>
                  <r:Label>
                    <r:Content xml:lang="%s">%s</r:Content>
                  </r:Label>
                </LogicalRecord>
              </DataRelationship>
            </Fragment>""",
                ColecticaXml.escape(versionDate),
                ColecticaXml.escape(agencyId),
                ColecticaXml.escape(dataRelationshipId),
                version,
                ColecticaXml.escape(agencyId),
                ColecticaXml.escape(dataRelationshipId),
                version,
                versionResponsibilityXml(),
                labels.defaultLang(),
                ColecticaXml.escape(dataRelationshipLabel),
                ColecticaXml.escape(agencyId),
                ColecticaXml.escape(logicalRecordId),
                version,
                ColecticaXml.escape(agencyId),
                ColecticaXml.escape(logicalRecordId),
                version,
                labels.defaultLang(),
                ColecticaXml.escape(logicalRecordLabel));
    }
}
