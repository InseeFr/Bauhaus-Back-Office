package fr.insee.rmes.modules.commons.configuration.swagger.model.operations.documentation;

public class DocumentId {
    private String id;

    public DocumentId(String id) {
        this.id = id;
    }

    public String getDocumentId() {
        return id;
    }

    public String getString() {
        if (id != null && !id.isEmpty()) {
            return id;
        }
        else{
            return null;                //without this it might cause some trouble to test with new DocumentID(null)
        }
    }

}
