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

    @Schema()
    public String definitionLg1Uri;

    @Schema()
    public String definitionLg1;

    @Schema()
    public String definitionLg2Uri;

    @Schema()
    public String definitionLg2;

    @Schema()
    public String scopeNoteLg1Uri;

    @Schema()
    public String scopeNoteLg1;

    @Schema()
    public String scopeNoteLg2Uri;

    @Schema()
    public String scopeNoteLg2;

    @Schema()
    public String coreContentNoteLg1Uri;

    @Schema()
    public String coreContentNoteLg1;

    @Schema()
    public String coreContentNoteLg2Uri;

    @Schema()
    public String coreContentNoteLg2;

    @Schema()
    public String additionalContentNoteLg1Uri;

    @Schema()
    public String additionalContentNoteLg1;

    @Schema()
    public String additionalContentNoteLg2Uri;

    @Schema()
    public String additionalContentNoteLg2;

    @Schema()
    public String exclusionNoteLg1Uri;

    @Schema()
    public String exclusionNoteLg1;

    @Schema()
    public String exclusionNoteLg2Uri;

    @Schema()
    public String exclusionNoteLg2;

    @Schema()
    public String changeNoteLg1Uri;

    @Schema()
    public String changeNoteLg1;

    @Schema()
    public String changeNoteLg2Uri;

    @Schema()
    public String changeNoteLg2;

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

    public String getDefinitionLg1Uri() {
        return definitionLg1Uri;
    }

    public void setDefinitionLg1Uri(String definitionLg1Uri) {
        this.definitionLg1Uri = definitionLg1Uri;
    }

    public String getDefinitionLg1() {
        return definitionLg1;
    }

    public void setDefinitionLg1(String definitionLg1) {
        this.definitionLg1 = definitionLg1;
    }

    public String getDefinitionLg2Uri() {
        return definitionLg2Uri;
    }

    public void setDefinitionLg2Uri(String definitionLg2Uri) {
        this.definitionLg2Uri = definitionLg2Uri;
    }

    public String getDefinitionLg2() {
        return definitionLg2;
    }

    public void setDefinitionLg2(String definitionLg2) {
        this.definitionLg2 = definitionLg2;
    }

    public String getScopeNoteLg1Uri() {
        return scopeNoteLg1Uri;
    }

    public void setScopeNoteLg1Uri(String scopeNoteLg1Uri) {
        this.scopeNoteLg1Uri = scopeNoteLg1Uri;
    }

    public String getScopeNoteLg1() {
        return scopeNoteLg1;
    }

    public void setScopeNoteLg1(String scopeNoteLg1) {
        this.scopeNoteLg1 = scopeNoteLg1;
    }

    public String getScopeNoteLg2Uri() {
        return scopeNoteLg2Uri;
    }

    public void setScopeNoteLg2Uri(String scopeNoteLg2Uri) {
        this.scopeNoteLg2Uri = scopeNoteLg2Uri;
    }

    public String getScopeNoteLg2() {
        return scopeNoteLg2;
    }

    public void setScopeNoteLg2(String scopeNoteLg2) {
        this.scopeNoteLg2 = scopeNoteLg2;
    }

    public String getCoreContentNoteLg1Uri() {
        return coreContentNoteLg1Uri;
    }

    public void setCoreContentNoteLg1Uri(String coreContentNoteLg1Uri) {
        this.coreContentNoteLg1Uri = coreContentNoteLg1Uri;
    }

    public String getCoreContentNoteLg1() {
        return coreContentNoteLg1;
    }

    public void setCoreContentNoteLg1(String coreContentNoteLg1) {
        this.coreContentNoteLg1 = coreContentNoteLg1;
    }

    public String getCoreContentNoteLg2Uri() {
        return coreContentNoteLg2Uri;
    }

    public void setCoreContentNoteLg2Uri(String coreContentNoteLg2Uri) {
        this.coreContentNoteLg2Uri = coreContentNoteLg2Uri;
    }

    public String getCoreContentNoteLg2() {
        return coreContentNoteLg2;
    }

    public void setCoreContentNoteLg2(String coreContentNoteLg2) {
        this.coreContentNoteLg2 = coreContentNoteLg2;
    }

    public String getAdditionalContentNoteLg1Uri() {
        return additionalContentNoteLg1Uri;
    }

    public void setAdditionalContentNoteLg1Uri(String additionalContentNoteLg1Uri) {
        this.additionalContentNoteLg1Uri = additionalContentNoteLg1Uri;
    }

    public String getAdditionalContentNoteLg1() {
        return additionalContentNoteLg1;
    }

    public void setAdditionalContentNoteLg1(String additionalContentNoteLg1) {
        this.additionalContentNoteLg1 = additionalContentNoteLg1;
    }

    public String getAdditionalContentNoteLg2Uri() {
        return additionalContentNoteLg2Uri;
    }

    public void setAdditionalContentNoteLg2Uri(String additionalContentNoteLg2Uri) {
        this.additionalContentNoteLg2Uri = additionalContentNoteLg2Uri;
    }

    public String getAdditionalContentNoteLg2() {
        return additionalContentNoteLg2;
    }

    public void setAdditionalContentNoteLg2(String additionalContentNoteLg2) {
        this.additionalContentNoteLg2 = additionalContentNoteLg2;
    }

    public String getExclusionNoteLg1Uri() {
        return exclusionNoteLg1Uri;
    }

    public void setExclusionNoteLg1Uri(String exclusionNoteLg1Uri) {
        this.exclusionNoteLg1Uri = exclusionNoteLg1Uri;
    }

    public String getExclusionNoteLg1() {
        return exclusionNoteLg1;
    }

    public void setExclusionNoteLg1(String exclusionNoteLg1) {
        this.exclusionNoteLg1 = exclusionNoteLg1;
    }

    public String getExclusionNoteLg2Uri() {
        return exclusionNoteLg2Uri;
    }

    public void setExclusionNoteLg2Uri(String exclusionNoteLg2Uri) {
        this.exclusionNoteLg2Uri = exclusionNoteLg2Uri;
    }

    public String getExclusionNoteLg2() {
        return exclusionNoteLg2;
    }

    public void setExclusionNoteLg2(String exclusionNoteLg2) {
        this.exclusionNoteLg2 = exclusionNoteLg2;
    }

    public String getChangeNoteLg1Uri() {
        return changeNoteLg1Uri;
    }

    public void setChangeNoteLg1Uri(String changeNoteLg1Uri) {
        this.changeNoteLg1Uri = changeNoteLg1Uri;
    }

    public String getChangeNoteLg1() {
        return changeNoteLg1;
    }

    public void setChangeNoteLg1(String changeNoteLg1) {
        this.changeNoteLg1 = changeNoteLg1;
    }

    public String getChangeNoteLg2Uri() {
        return changeNoteLg2Uri;
    }

    public void setChangeNoteLg2Uri(String changeNoteLg2Uri) {
        this.changeNoteLg2Uri = changeNoteLg2Uri;
    }

    public String getChangeNoteLg2() {
        return changeNoteLg2;
    }

    public void setChangeNoteLg2(String changeNoteLg2) {
        this.changeNoteLg2 = changeNoteLg2;
    }
}
