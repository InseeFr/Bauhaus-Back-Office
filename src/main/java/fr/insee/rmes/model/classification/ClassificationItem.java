package fr.insee.rmes.model.classification;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class ClassificationItem {
    @Schema(description = "Id")
    public String id;

    @Schema(description = "Label lg1", required = true)
    public String prefLabelLg1;

    @Schema(description = "Label lg2")
    public String prefLabelLg2;

    @Schema(description = "idBroader")
    public String idBroader;

    @Schema(description = "altLabelLg1")
    public String altLabelLg1;

    @Schema(description = "altLabelLg2")
    public String altLabelLg2;

    @Schema(description = "altLabels")
    public List<ClassificationItemShortLabel>  altLabels;

    @Schema(description = "narrowers")
    public List<String>  narrowers;

    @Schema(description = "broader URI")
    public String broaderURI;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrefLabelLg1() {
        return prefLabelLg1;
    }

    public void setPrefLabelLg1(String prefLabelLg1) {
        this.prefLabelLg1 = prefLabelLg1;
    }

    public String getPrefLabelLg2() {
        return prefLabelLg2;
    }

    public void setPrefLabelLg2(String prefLabelLg2) {
        this.prefLabelLg2 = prefLabelLg2;
    }

    public String getIdBroader() {
        return idBroader;
    }

    public void setIdBroader(String idBroader) {
        this.idBroader = idBroader;
    }

    public String getBroaderURI() {
        return broaderURI;
    }

    public void setBroaderURI(String broaderURI) {
        this.broaderURI = broaderURI;
    }

    public String getAltLabelLg1() {
        return altLabelLg1;
    }

    public void setAltLabelLg1(String altLabelLg1) {
        this.altLabelLg1 = altLabelLg1;
    }

    public String getAltLabelLg2() {
        return altLabelLg2;
    }

    public void setAltLabelLg2(String altLabelLg2) {
        this.altLabelLg2 = altLabelLg2;
    }

    public List<ClassificationItemShortLabel> getAltLabels() {
        return altLabels;
    }

    public void setAltLabels(List<ClassificationItemShortLabel> altLabels) {
        this.altLabels = altLabels;
    }

    public List<String> getNarrowers() {
        return narrowers;
    }

    public void setNarrowers(List<String> narrowers) {
        this.narrowers = narrowers;
    }
}
