package fr.insee.rmes.model.dataset;

public class Distribution {
    String id;
    String idDataset;
    String labelLg1;
    String labelLg2;
    String descriptionLg1;
    String descriptionLg2;
    String created;
    String updated;
    String format;
    String byteSize;
    String url;
    String validationState;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdDataset() {
        return idDataset;
    }

    public void setIdDataset(String idDataset) {
        this.idDataset = idDataset;
    }

    public String getLabelLg1() {
        return labelLg1;
    }

    public void setLabelLg1(String labelLg1) {
        this.labelLg1 = labelLg1;
    }

    public String getLabelLg2() {
        return labelLg2;
    }

    public void setLabelLg2(String labelLg2) {
        this.labelLg2 = labelLg2;
    }

    public String getDescriptionLg1() {
        return descriptionLg1;
    }

    public void setDescriptionLg1(String descriptionLg1) {
        this.descriptionLg1 = descriptionLg1;
    }

    public String getDescriptionLg2() {
        return descriptionLg2;
    }

    public void setDescriptionLg2(String descriptionLg2) {
        this.descriptionLg2 = descriptionLg2;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getByteSize() {
        return byteSize;
    }

    public void setByteSize(String byteSize) {
        this.byteSize = byteSize;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getValidationState() {
        return validationState;
    }

    public void setValidationState(String validationState) {
        this.validationState = validationState;
    }
}