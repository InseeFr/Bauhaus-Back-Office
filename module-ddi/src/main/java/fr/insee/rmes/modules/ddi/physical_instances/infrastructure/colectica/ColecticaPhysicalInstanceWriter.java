package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.DATA_RELATIONSHIP;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.PHYSICAL_INSTANCE;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaItemTypes.STUDY_UNIT;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaXml.REUSABLE_NS;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaXml.STUDY_UNIT_NS;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.dto.ColecticaCreateItemRequest;
import fr.insee.rmes.colectica.client.dto.ColecticaItemResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CreatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4DataRelationship;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangString;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangStrings;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LogicalRecord;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceParents;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UpdatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Créations et mises à jour d'une PhysicalInstance et de ses objets DDI. */
class ColecticaPhysicalInstanceWriter {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaPhysicalInstanceWriter.class);

    private static final String BAUHAUS_API = "bauhaus-api";

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
        String physicalInstanceId = UUID.randomUUID().toString();
        String dataRelationshipId = UUID.randomUUID().toString();
        String logicalRecordId = UUID.randomUUID().toString();
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
                BAUHAUS_API,
                false,
                false,
                false,
                instanceConfiguration.itemFormat());
    }

    void updatePhysicalInstance(String agencyId, String id, UpdatePhysicalInstanceRequest request) {
        // On recharge l'instance courante pour repartir de tous ses objets, variables comprises.
        Ddi4Response currentInstance = reader.getPhysicalInstance(agencyId, id);

        if (currentInstance == null
                || currentInstance.physicalInstance() == null
                || currentInstance.physicalInstance().isEmpty()) {
            throw new RuntimeException("Physical instance not found: " + agencyId + "/" + id);
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

        Ddi4DataRelationship updatedDR =
                currentDR == null ? null : updatedDataRelationship(currentDR, request, versionDate);

        // Ddi4Response reconstruite en préservant variables, listes de codes et catégories.
        Ddi4Response updatedResponse = new Ddi4Response(
                currentInstance.schema(),
                currentInstance.topLevelReference(),
                List.of(updatedPI),
                updatedDR != null ? List.of(updatedDR) : currentInstance.dataRelationship(),
                currentInstance.variable(),
                currentInstance.codeList(),
                currentInstance.category(),
                currentInstance.managedMissingValuesRepresentation());

        // Quand une StudyUnit est fournie (flux de duplication, cf. #1555), on rattache la
        // PhysicalInstance dans le même enregistrement, pour que GET .../parents sache ensuite
        // résoudre sa Study et son Group.
        List<ColecticaItemResponse> additionalItems = new ArrayList<>();
        PhysicalInstanceParents requestParents = null;
        if (request.studyUnitId() != null && request.studyUnitAgency() != null) {
            additionalItems.add(addPhysicalInstanceReferenceToStudyUnit(
                    request.studyUnitAgency(), request.studyUnitId(), agencyId, id));
            // Le rattachement part dans ce même batch : la relation StudyUnit n'est donc pas encore
            // interrogeable dans Colectica, la requête est la seule source de vérité pour les parents.
            if (request.groupId() != null && request.groupAgency() != null) {
                requestParents = new PhysicalInstanceParents(
                        request.studyUnitAgency(), request.studyUnitId(),
                        request.groupAgency(), request.groupId());
            }
        }

        updateFullPhysicalInstance(agencyId, id, updatedResponse, additionalItems, requestParents);
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
     * le flux PATCH pour rattacher la PhysicalInstance à une StudyUnit (cf. #1555).
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
        logger.info("Updating full physical instance {}/{} with all DDI objects in Colectica", agencyId, id);

        Ddi3Response ddi3Response = ddi4ToDdi3Converter.convertDdi4ToDdi3(ddi4Response);

        if (ddi3Response == null
                || ddi3Response.items() == null
                || ddi3Response.items().isEmpty()) {
            throw new RuntimeException("No items to save in DDI4 response");
        }

        List<ColecticaItemResponse> colecticaItems = ddi3Response.items().stream()
                .map(ColecticaItems::toColecticaItem)
                .collect(Collectors.toCollection(ArrayList::new));

        // Range les listes de codes et catégories non mutualisées sous les schemes du groupe, et les
        // variables sous le VariableScheme de la study unit (en auto-provisionnant les schemes
        // manquants). Les parents sont résolus une fois et partagés.
        schemeFiler.appendSchemeUpdates(agencyId, id, ddi4Response, colecticaItems, additionalItems, knownParents);

        // Items supplémentaires (p.ex. la StudyUnit réenregistrée avec une nouvelle référence de PI)
        colecticaItems.addAll(additionalItems);

        logger.info("Sending full update request to Colectica with {} items", colecticaItems.size());
        colecticaClient.createOrUpdateItems(new ColecticaCreateItemRequest(colecticaItems));

        logger.info(
                "Successfully updated full physical instance with id: {} ({} items saved)", id, colecticaItems.size());
    }

    /**
     * Ajoute une {@code r:PhysicalInstanceReference} au XML de la StudyUnit et renvoie l'item Colectica
     * correspondant, à enregistrer dans le même batch que la PhysicalInstance.
     */
    private ColecticaItemResponse addPhysicalInstanceReferenceToStudyUnit(
            String studyUnitAgency, String studyUnitId, String physicalInstanceAgency, String physicalInstanceId) {
        ColecticaItemResponse studyUnitItem = colecticaClient.getItem(studyUnitAgency, studyUnitId, null);
        if (studyUnitItem == null) {
            throw new RuntimeException("StudyUnit not found: agency=" + studyUnitAgency + " id=" + studyUnitId);
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
