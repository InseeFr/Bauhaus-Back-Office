package fr.insee.rmes.modules.ddi.physical_instances.domain.services.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.insee.ddi.lifecycle33.group.GroupType;
import fr.insee.ddi.lifecycle33.instance.FragmentDocument;
import org.apache.xmlbeans.XmlException;

public class GroupDDIItemConverter extends AbstractDDIItemConverter {

    @Override
    public boolean supports(String xmlRootElementLocalName) {
        return "Group".equals(xmlRootElementLocalName);
    }

    @Override
    public JsonNode convert(String xmlFragment) {
        try {
            GroupType group = FragmentDocument.Factory.parse(xmlFragment).getFragment().getGroup();
            if (group == null) throw new IllegalArgumentException("No Group element found in fragment");

            ObjectNode result = MAPPER.createObjectNode();
            addVersionableFields(result, group);
            addUserIds(result, group);

            if (group.isSetTypeOfGroup()) {
                result.set("TypeOfGroup", MAPPER.createObjectNode().put("StringValue", group.getTypeOfGroup().getStringValue()));
            }

            addCitation(result, group.getCitation());

            ArrayNode studyUnitRefs = buildReferences(group.getStudyUnitReferenceArray());
            if (!studyUnitRefs.isEmpty()) result.set("StudyUnitReference", studyUnitRefs);

            return result;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (XmlException e) {
            throw new RuntimeException("Failed to parse Group XML fragment", e);
        }
    }
}
