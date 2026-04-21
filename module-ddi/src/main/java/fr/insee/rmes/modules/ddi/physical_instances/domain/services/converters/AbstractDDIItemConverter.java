package fr.insee.rmes.modules.ddi.physical_instances.domain.services.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.insee.ddi.lifecycle33.reusable.*;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIItemConverter;

public abstract class AbstractDDIItemConverter implements DDIItemConverter {

    protected static final ObjectMapper MAPPER = new ObjectMapper();

    protected void addVersionableFields(ObjectNode target, AbstractVersionableType item) {
        if (item.sizeOfURNArray() > 0) target.put("URN", item.getURNArray(0).getStringValue());
        if (item.sizeOfAgencyArray() > 0) target.put("Agency", item.getAgencyArray(0));
        if (item.sizeOfIDArray() > 0) target.put("ID", item.getIDArray(0).getStringValue());
        if (item.sizeOfVersionArray() > 0) target.put("Version", item.getVersionArray(0));

        if (item.isSetVersionDate()) {
            target.set("VersionDate", MAPPER.createObjectNode().put("DateTime", item.xgetVersionDate().getStringValue()));
        }
    }

    protected void addUserIds(ObjectNode target, AbstractIdentifiableType item) {
        if (item.sizeOfUserIDArray() == 0) return;

        ArrayNode userIds = MAPPER.createArrayNode();
        for (UserIDType userId : item.getUserIDList()) {
            ObjectNode node = MAPPER.createObjectNode();
            String value = userId.getStringValue();
            if (value != null && !value.isBlank()) node.put("StringValue", value);
            String type = userId.getTypeOfUserID();
            if (type != null && !type.isBlank()) node.set("TypeOfUserID", MAPPER.createObjectNode().put("StringValue", type));
            userIds.add(node);
        }
        if (!userIds.isEmpty()) target.set("UserID", userIds);
    }

    protected void addCitation(ObjectNode target, CitationType citation) {
        if (citation == null || !citation.isSetTitle()) return;

        ArrayNode stringsArray = MAPPER.createArrayNode();
        for (StringType s : citation.getTitle().getStringList()) {
            ObjectNode multilingualValue = MAPPER.createObjectNode();
            String lang = s.getLang();
            String value = s.getStringValue();
            if (lang != null && !lang.isBlank()) multilingualValue.put("LanguageTag", lang);
            if (value != null && !value.isBlank()) multilingualValue.put("Value", value);
            stringsArray.add(MAPPER.createObjectNode().set("MultilingualStringValue", multilingualValue));
        }

        target.set("Citation", MAPPER.createObjectNode()
                .set("Title", MAPPER.createObjectNode().set("String", stringsArray)));
    }

    protected ArrayNode buildReferences(ReferenceType[] refs) {
        ArrayNode result = MAPPER.createArrayNode();
        for (ReferenceType ref : refs) {
            ObjectNode node = buildReference(ref);
            if (node != null) result.add(node);
        }
        return result;
    }

    protected ObjectNode buildReference(ReferenceType ref) {
        String urn = ref.sizeOfURNArray() > 0 ? ref.getURNArray(0).getStringValue() : null;
        if (urn == null) {
            String agency = ref.sizeOfAgencyArray() > 0 ? ref.getAgencyArray(0) : null;
            String id = ref.sizeOfIDArray() > 0 ? ref.getIDArray(0).getStringValue() : null;
            String version = ref.sizeOfVersionArray() > 0 ? ref.getVersionArray(0) : null;
            if (agency == null || id == null || version == null) return null;
            urn = "urn:ddi:" + agency + ":" + id + ":" + version;
        }

        TypeOfObjectType.Enum typeEnum = ref.getTypeOfObject();
        String type = typeEnum != null ? typeEnum.toString() : null;
        if (type == null) return null;

        ObjectNode node = MAPPER.createObjectNode();
        node.put("$type", type);
        node.putArray("value").add(urn);
        return node;
    }
}
