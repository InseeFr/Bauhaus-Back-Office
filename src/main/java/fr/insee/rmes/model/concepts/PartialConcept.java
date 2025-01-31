package fr.insee.rmes.model.concepts;

public class PartialConcept {
    String id;
    String label;
    String altLabel;

    public PartialConcept(){}
    public PartialConcept(String id, String label, String altLabel) {
        this.id = id;
        this.label = label;
        this.altLabel = altLabel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAltLabel() {
        return altLabel;
    }

    public void setAltLabel(String altLabel) {
        this.altLabel = altLabel;
    }
}
