package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaXml.GROUP_NS;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaXml.REUSABLE_NS;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaXml.STUDY_UNIT_NS;
import static fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaXml.XML_NS;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4GroupResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangStrings;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Lecture d'un Group et de ses StudyUnits via l'endpoint {@code ddiset} de Colectica, puis parsing
 * direct du DDI 3.3 vers {@link Ddi4GroupResponse} (sans passer par le convertisseur générique).
 */
class ColecticaGroupSetReader {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaGroupSetReader.class);

    private static final String AGENCY = "Agency";
    private static final String VERSION = "Version";

    private final ColecticaClient colecticaClient;
    private final String defaultLang;

    ColecticaGroupSetReader(ColecticaClient colecticaClient, String defaultLang) {
        this.colecticaClient = colecticaClient;
        this.defaultLang = defaultLang;
    }

    Ddi4GroupResponse getGroup(String agencyId, String id) {
        logger.info("Fetching DDI4 Group from Colectica API for agencyId: {}, id: {}", agencyId, id);
        try {
            // Lecture des octets bruts décodés explicitement en UTF-8 : Colectica omet le charset, et
            // le StringHttpMessageConverter de Spring retomberait sur ISO-8859-1, produisant du
            // mojibake sur les caractères accentués.
            logger.info("Fetching full DDI set for Group {}/{}", agencyId, id);
            byte[] ddisetBytes = colecticaClient.getDdiSet(agencyId, id);

            if (ddisetBytes == null || ddisetBytes.length == 0) {
                logger.error("Received empty response from Colectica API for ddiset {}/{}", agencyId, id);
                return null;
            }

            String ddisetXml = new String(ddisetBytes, StandardCharsets.UTF_8);
            logger.info("Received response from ddiset endpoint for Group. Length: {}", ddisetXml.length());

            return parseGroupXml(ColecticaXml.stripLeadingGarbage(ddisetXml));
        } catch (Exception e) {
            logger.error("Error processing Colectica API response for Group agencyId: {}, id: {}", agencyId, id, e);
            throw new RuntimeException("Failed to process DDI Group response", e);
        }
    }

    private Ddi4GroupResponse parseGroupXml(String xml) throws Exception {
        Document doc = ColecticaXml.parse(xml);

        List<Ddi4Group> groups = new ArrayList<>();
        List<Ddi4StudyUnit> studyUnits = new ArrayList<>();
        List<Reference> topLevelReferences = new ArrayList<>();

        NodeList groupNodes = doc.getElementsByTagNameNS(GROUP_NS, "Group");
        logger.info("Found {} Group elements", groupNodes.getLength());
        for (int i = 0; i < groupNodes.getLength(); i++) {
            Ddi4Group group = parseGroupElement((Element) groupNodes.item(i));
            groups.add(group);
            topLevelReferences.add(Reference.of(group.agency(), group.id(), group.version(), "Group"));
        }

        NodeList studyUnitNodes = doc.getElementsByTagNameNS(STUDY_UNIT_NS, "StudyUnit");
        logger.info("Found {} StudyUnit elements", studyUnitNodes.getLength());
        for (int i = 0; i < studyUnitNodes.getLength(); i++) {
            studyUnits.add(parseStudyUnitElement((Element) studyUnitNodes.item(i)));
        }

        return new Ddi4GroupResponse(Ddi4Response.SCHEMA, topLevelReferences, groups, studyUnits);
    }

    private Ddi4Group parseGroupElement(Element groupElement) {
        String versionDate = groupElement.getAttribute("versionDate");
        List<String> seriesIris = ColecticaXml.textContents(groupElement, REUSABLE_NS, "UserID");
        String typeOfGroup = ColecticaXml.textContent(groupElement, GROUP_NS, "TypeOfGroup");

        return new Ddi4Group(
                Ddi4Group.TYPE,
                versionDate.isEmpty() ? null : CogsDate.ofDateTime(versionDate),
                ColecticaXml.textContent(groupElement, REUSABLE_NS, "URN"),
                ColecticaXml.textContent(groupElement, REUSABLE_NS, AGENCY),
                ColecticaXml.textContent(groupElement, REUSABLE_NS, "ID"),
                ColecticaXml.textContent(groupElement, REUSABLE_NS, VERSION),
                ColecticaXml.textContent(groupElement, REUSABLE_NS, "VersionResponsibility"),
                parseCitation(groupElement),
                parseStudyUnitReferences(groupElement),
                seriesIris.isEmpty() ? null : seriesIris,
                (typeOfGroup == null || typeOfGroup.isEmpty()) ? null : typeOfGroup);
    }

    private Ddi4StudyUnit parseStudyUnitElement(Element studyUnitElement) {
        String versionDate = studyUnitElement.getAttribute("versionDate");
        String operationIri = ColecticaXml.textContent(studyUnitElement, REUSABLE_NS, "UserID");

        return new Ddi4StudyUnit(
                Ddi4StudyUnit.TYPE,
                (versionDate == null || versionDate.isEmpty()) ? null : CogsDate.ofDateTime(versionDate),
                ColecticaXml.textContent(studyUnitElement, REUSABLE_NS, "URN"),
                ColecticaXml.textContent(studyUnitElement, REUSABLE_NS, AGENCY),
                ColecticaXml.textContent(studyUnitElement, REUSABLE_NS, "ID"),
                ColecticaXml.textContent(studyUnitElement, REUSABLE_NS, VERSION),
                parseCitation(studyUnitElement),
                (operationIri == null || operationIri.isEmpty()) ? null : operationIri,
                null);
    }

    private Citation parseCitation(Element parentElement) {
        NodeList citationNodes = parentElement.getElementsByTagNameNS(REUSABLE_NS, "Citation");
        if (citationNodes.getLength() == 0) {
            return null;
        }
        NodeList titleNodes = ((Element) citationNodes.item(0)).getElementsByTagNameNS(REUSABLE_NS, "Title");
        if (titleNodes.getLength() == 0) {
            return null;
        }
        NodeList stringNodes = ((Element) titleNodes.item(0)).getElementsByTagNameNS(REUSABLE_NS, "String");
        if (stringNodes.getLength() == 0) {
            return null;
        }

        Element stringElement = (Element) stringNodes.item(0);
        String lang = stringElement.getAttributeNS(XML_NS, "lang");
        return new Citation(LangStrings.of(lang.isEmpty() ? defaultLang : lang, stringElement.getTextContent()));
    }

    private List<Reference> parseStudyUnitReferences(Element groupElement) {
        List<Reference> references = new ArrayList<>();
        NodeList refNodes = groupElement.getElementsByTagNameNS(REUSABLE_NS, "StudyUnitReference");
        for (int i = 0; i < refNodes.getLength(); i++) {
            Element refElement = (Element) refNodes.item(i);
            references.add(Reference.of(
                    ColecticaXml.textContent(refElement, REUSABLE_NS, AGENCY),
                    ColecticaXml.textContent(refElement, REUSABLE_NS, "ID"),
                    ColecticaXml.textContent(refElement, REUSABLE_NS, VERSION),
                    ColecticaXml.textContent(refElement, REUSABLE_NS, "TypeOfObject")));
        }
        return references;
    }
}
