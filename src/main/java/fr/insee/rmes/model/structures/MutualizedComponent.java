package fr.insee.rmes.model.structures;

import fr.insee.rmes.exceptions.RmesException;

public class MutualizedComponent {

    private String identifiant;
    private String id;
    private String labelLg1;
    private String labelLg2;


    private String descriptionLg1;
    private String descriptionLg2;

    private String type;
    private String concept;
    private String codeList;
    private String range;
    private Structure[] structures;

    private String created;
    private String updated;

    private String creator;
    private String contributor;
    private String disseminationStatus;


    public MutualizedComponent() throws RmesException {
        //nothing to do
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getCodeList() {
        return codeList;
    }

    public void setCodeList(String codeList) {
        this.codeList = codeList;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public Structure[] getStructures() {
        return structures;
    }

    public void setStructures(Structure[] structures) {
        this.structures = structures;
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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public String getDisseminationStatus() {
        return disseminationStatus;
    }

    public void setDisseminationStatus(String disseminationStatus) {
        this.disseminationStatus = disseminationStatus;
    }
}
