package fr.insee.rmes.modules.ddi.physical_instances.domain.services.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.insee.ddi.lifecycle33.instance.FragmentDocument;
import fr.insee.ddi.lifecycle33.physicalinstance.PhysicalInstanceType;
import org.apache.xmlbeans.XmlException;

public class PhysicalInstanceDDIItemConverter extends AbstractDDIItemConverter {

    @Override
    public boolean supports(String xmlRootElementLocalName) {
        return "PhysicalInstance".equals(xmlRootElementLocalName);
    }

    @Override
    public JsonNode convert(String xmlFragment) {
        try {
            PhysicalInstanceType pi = FragmentDocument.Factory.parse(xmlFragment).getFragment().getPhysicalInstance();
            if (pi == null) throw new IllegalArgumentException("No PhysicalInstance element found in fragment");

            ObjectNode result = MAPPER.createObjectNode();
            addVersionableFields(result, pi);
            addCitation(result, pi.getCitation());

            ArrayNode dataRelationshipRefs = buildReferences(pi.getDataRelationshipReferenceArray());
            if (!dataRelationshipRefs.isEmpty()) result.set("DataRelationshipReference", dataRelationshipRefs);

            return result;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (XmlException e) {
            throw new RuntimeException("Failed to parse PhysicalInstance XML fragment", e);
        }
    }
}
