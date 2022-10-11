package fr.insee.rmes.model.classification;

import io.swagger.v3.oas.annotations.media.Schema;

public class Classification {
    @Schema(description = "Id")
    public String id;

    @Schema(description = "Label lg1", required = true)
    public String prefLabelLg1;

    @Schema(description = "Label lg2")
    public String prefLabelLg2;

    @Schema(description = "Alternative Label Lg1")
    public String altLabelLg1;

    @Schema(description = "Alternative Label Lg2")
    public String altLabelLg2;

    @Schema(description = "Description Lg1")
    public String descriptionLg1;

    @Schema(description = "Description Lg2")
    public String descriptionLg2;

    @Schema(description = "Change Note Lg1")
    public String changeNoteLg1;

    @Schema(description = "Change Note Lg2")
    public String changeNoteLg2;

    @Schema(description = "Scope Note Lg1")
    public String scopeNoteLg1;

    @Schema(description = "Scope Note Lg2")
    public String scopeNoteLg2;

    @Schema(description = "Scope Note Uri Lg1")
    public String scopeNoteUriLg1;

    @Schema(description = "Scope Note Uri Lg2")
    public String scopeNoteUriLg2;

    @Schema(description = "Change Note Uri Lg1")
    public String changeNoteUriLg1;

    @Schema(description = "Change Note Uri Lg2")
    public String changeNoteUriLg2;


    @Schema(description = "Series ID")
    public String idSeries;

    @Schema(description = "")
    public String idBefore;

    @Schema(description = "")
    public String idAfter;

    @Schema(description = "")
    public String idVariant;

    @Schema(description = "Dissemination status", required = true)
    public String disseminationStatus;

    @Schema(description = "Additional Material", required = true)
    public String additionalMaterial;

    @Schema(description = "Legal Material", required = true)
    public String legalMaterial;

    @Schema(description = "Homepage", required = true)
    public String homepage;

    @Schema(description = "Creator", required = true)
    public String creator;

    @Schema(description = "Contributor", required = true)
    public String contributor;

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

    public String getIdSeries() {
        return idSeries;
    }

    public void setIdSeries(String idSeries) {
        this.idSeries = idSeries;
    }

    public String getIdBefore() {
        return idBefore;
    }

    public void setIdBefore(String idBefore) {
        this.idBefore = idBefore;
    }

    public String getIdAfter() {
        return idAfter;
    }

    public void setIdAfter(String idAfter) {
        this.idAfter = idAfter;
    }

    public String getIdVariant() {
        return idVariant;
    }

    public void setIdVariant(String idVariant) {
        this.idVariant = idVariant;
    }

    public String getDisseminationStatus() {
        return disseminationStatus;
    }

    public void setDisseminationStatus(String disseminationStatus) {
        this.disseminationStatus = disseminationStatus;
    }

    public String getAdditionalMaterial() {
        return additionalMaterial;
    }

    public void setAdditionalMaterial(String additionalMaterial) {
        this.additionalMaterial = additionalMaterial;
    }

    public String getLegalMaterial() {
        return legalMaterial;
    }

    public void setLegalMaterial(String legalMaterial) {
        this.legalMaterial = legalMaterial;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
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

    public String getChangeNoteLg1() {
        return changeNoteLg1;
    }

    public void setChangeNoteLg1(String changeNoteLg1) {
        this.changeNoteLg1 = changeNoteLg1;
    }

    public String getChangeNoteLg2() {
        return changeNoteLg2;
    }

    public void setChangeNoteLg2(String changeNoteLg2) {
        this.changeNoteLg2 = changeNoteLg2;
    }

    public String getScopeNoteLg1() {
        return scopeNoteLg1;
    }

    public void setScopeNoteLg1(String scopeNoteLg1) {
        this.scopeNoteLg1 = scopeNoteLg1;
    }

    public String getScopeNoteLg2() {
        return scopeNoteLg2;
    }

    public void setScopeNoteLg2(String scopeNoteLg2) {
        this.scopeNoteLg2 = scopeNoteLg2;
    }

    public String getScopeNoteUriLg1() {
        return scopeNoteUriLg1;
    }

    public void setScopeNoteUriLg1(String scopeNoteUriLg1) {
        this.scopeNoteUriLg1 = scopeNoteUriLg1;
    }

    public String getScopeNoteUriLg2() {
        return scopeNoteUriLg2;
    }

    public void setScopeNoteUriLg2(String scopeNoteUriLg2) {
        this.scopeNoteUriLg2 = scopeNoteUriLg2;
    }

    public String getChangeNoteUriLg1() {
        return changeNoteUriLg1;
    }

    public void setChangeNoteUriLg1(String changeNoteUriLg1) {
        this.changeNoteUriLg1 = changeNoteUriLg1;
    }

    public String getChangeNoteUriLg2() {
        return changeNoteUriLg2;
    }

    public void setChangeNoteUriLg2(String changeNoteUriLg2) {
        this.changeNoteUriLg2 = changeNoteUriLg2;
    }
}
