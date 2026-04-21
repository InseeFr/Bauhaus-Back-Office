package fr.insee.rmes.modules.ddi.physical_instances.domain.services.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.insee.ddi.lifecycle33.instance.FragmentDocument;
import fr.insee.ddi.lifecycle33.studyunit.StudyUnitType;
import org.apache.xmlbeans.XmlException;

public class StudyUnitDDIItemConverter extends AbstractDDIItemConverter {

    @Override
    public boolean supports(String xmlRootElementLocalName) {
        return "StudyUnit".equals(xmlRootElementLocalName);
    }

    @Override
    public JsonNode convert(String xmlFragment) {
        try {
            StudyUnitType studyUnit = FragmentDocument.Factory.parse(xmlFragment).getFragment().getStudyUnit();
            if (studyUnit == null) throw new IllegalArgumentException("No StudyUnit element found in fragment");

            ObjectNode result = MAPPER.createObjectNode();
            addVersionableFields(result, studyUnit);
            addUserIds(result, studyUnit);
            addCitation(result, studyUnit.getCitation());

            ArrayNode physicalInstanceRefs = buildReferences(studyUnit.getPhysicalInstanceReferenceArray());
            if (!physicalInstanceRefs.isEmpty()) result.set("PhysicalInstanceReference", physicalInstanceRefs);

            return result;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (XmlException e) {
            throw new RuntimeException("Failed to parse StudyUnit XML fragment", e);
        }
    }
}
