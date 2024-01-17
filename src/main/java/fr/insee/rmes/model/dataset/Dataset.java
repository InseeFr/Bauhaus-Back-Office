package fr.insee.rmes.model.dataset;

import java.util.List;

public class Dataset {
    private String id;
    private String labelLg1;
    private String labelLg2;
    private String descriptionLg1;
    private String descriptionLg2;
    private String abstractLg1;
    private String abstractLg2;
    private String cautionLg1;
    private String cautionLg2;
    private String disseminationStatus;
    private String idSerie;
    private List<String> themes;
    private String validationState;
    private String updated;

    private List<String> creators;

    private CatalogRecord catalogRecord;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getAbstractLg1() {
        return abstractLg1;
    }

    public void setAbstractLg1(String abstractLg1) {
        this.abstractLg1 = abstractLg1;
    }

    public String getAbstractLg2() {
        return abstractLg2;
    }

    public void setAbstractLg2(String abstractLg2) {
        this.abstractLg2 = abstractLg2;
    }

    public String getCautionLg1() {
        return cautionLg1;
    }

    public void setCautionLg1(String cautionLg1) {
        this.cautionLg1 = cautionLg1;
    }

    public String getCautionLg2() {
        return cautionLg2;
    }

    public void setCautionLg2(String cautionLg2) {
        this.cautionLg2 = cautionLg2;
    }

    public String getDisseminationStatus() {
        return disseminationStatus;
    }

    public void setDisseminationStatus(String disseminationStatus) {
        this.disseminationStatus = disseminationStatus;
    }

    public String getIdSerie() {
        return idSerie;
    }

    public void setIdSerie(String idSerie) {
        this.idSerie = idSerie;
    }

    public List<String> getThemes() {
        return themes;
    }

    public void setThemes(List<String> themes) {
        this.themes = themes;
    }

    public String getValidationState() {
        return validationState;
    }

    public void setValidationState(String validationState) {
        this.validationState = validationState;
    }

    public CatalogRecord getCatalogRecord() {
        return catalogRecord;
    }

    public void setCatalogRecord(CatalogRecord catalogRecord) {
        this.catalogRecord = catalogRecord;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public List<String> getCreators() {
        return creators;
    }

    public void setCreators(List<String> creators) {
        this.creators = creators;
    }
}
