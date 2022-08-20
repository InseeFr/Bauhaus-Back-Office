package fr.insee.rmes.model.concepts;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)

public class CollectionForExportODSFinal {

    public String id;
    public String prefLabelLg1;
    public String prefLabelLg2;
    public String creator;
    private String contributor;
    private String disseminationStatus;
    private String additionalMaterial;
    private String created;
    private String modified;
    private String valid;
    private String conceptVersion;
    private String isValidated;
    public String descriptionLg1;
    public String descriptionLg2;

    List<Conceptfield> membersLg1;
    List<Conceptfield> membersLg2;


    public CollectionForExportODSFinal(String id, String prefLabelLg1, String prefLabelLg2, String creator,
                                       String contributor,String disseminationStatus,String additionalMaterial,
                                       String created,String modified,String valid,String conceptVersion,String isValidated,
                                       String descriptionLg1,String descriptionLg2, List<Conceptfield>  membersLg1, List<Conceptfield> membersLg2) {
            this.id=id;
            this.prefLabelLg1=prefLabelLg1;
            this.prefLabelLg2=prefLabelLg2;
            this.creator=creator;
            this.contributor=contributor;
            this.disseminationStatus=disseminationStatus;
            this.additionalMaterial=additionalMaterial;
            this.created=created;
            this.modified=modified;
            this.valid=valid;
            this.conceptVersion=conceptVersion;
            this.isValidated=isValidated;
            this.descriptionLg1=descriptionLg1;
            this.descriptionLg2=descriptionLg2;
            this.membersLg1=membersLg1;
            this.membersLg2=membersLg2;
    }

    public List<Conceptfield> getMembersLg1() {
        return membersLg1;
    }

    public void setMembersLg1(List<Conceptfield> membersLg1) {
        this.membersLg1 = membersLg1;
    }

    public List<Conceptfield> getMembersLg2() {
        return membersLg2;
    }

    public void setMembersLg2(List<Conceptfield> membersLg2) {
        this.membersLg2 = membersLg2;
    }

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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }



    public String getDescriptionLg1() {
        return descriptionLg1;
    }

    public void setDescriptionLg1(String descriptionLg1) {
        this.descriptionLg1 = descriptionLg1;
    }
}
