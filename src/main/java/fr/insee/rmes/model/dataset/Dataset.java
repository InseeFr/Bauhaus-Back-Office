package fr.insee.rmes.model.dataset;

import java.util.List;

public class Dataset {

    // Informations Générales

    private String labelLg1;
    private String labelLg2;
    private String subTitleLg1;
    private String subTitleLg2;
    private String accrualPeriodicity;
    private String accessRights;
    private String confidentialityStatus;
    private List<String> creators;
    private String publisher;
    private String landingPageLg1;
    private String landingPageLg2;

    private String updated;

    private String issued;

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

    public String getSubTitleLg1() {
        return subTitleLg1;
    }

    public void setSubTitleLg1(String subTitleLg1) {
        this.subTitleLg1 = subTitleLg1;
    }

    public String getSubTitleLg2() {
        return subTitleLg2;
    }

    public void setSubTitleLg2(String subTitleLg2) {
        this.subTitleLg2 = subTitleLg2;
    }

    public String getAccrualPeriodicity() {
        return accrualPeriodicity;
    }

    public void setAccrualPeriodicity(String accrualPeriodicity) {
        this.accrualPeriodicity = accrualPeriodicity;
    }

    public String getAccessRights() {
        return accessRights;
    }

    public void setAccessRights(String accessRights) {
        this.accessRights = accessRights;
    }

    public String getConfidentialityStatus() {
        return confidentialityStatus;
    }

    public void setConfidentialityStatus(String confidentialityStatus) {
        this.confidentialityStatus = confidentialityStatus;
    }

    public List<String> getCreators() {
        return creators;
    }

    public void setCreators(List<String> creators) {
        this.creators = creators;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getLandingPageLg1() {
        return landingPageLg1;
    }


    public void setLandingPageLg1(String landingPageLg1) {
        this.landingPageLg1 = landingPageLg1;
    }

    public String getLandingPageLg2() {
        return landingPageLg2;
    }

    public void setLandingPageLg2(String landingPageLg2) {
        this.landingPageLg2 = landingPageLg2;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getIssued() {
        return issued;
    }

    public void setIssued(String issued) {
        this.issued = issued;
    }

    // Internal Managment
    private String processStep;

    private String archiveUnit;

    public String getProcessStep() {
        return processStep;
    }

    public void setProcessStep(String processStep) {
        this.processStep = processStep;
    }

    public String getArchiveUnit() {
        return archiveUnit;
    }

    public void setArchiveUnit(String archiveUnit) {
        this.archiveUnit = archiveUnit;
    }

    // Statistics Informations
    private String type;

    private List<String> statisticalUnit;

    private String dataStructure;

    private String temporalCoverageStartDate;
    private String temporalCoverageEndDate;
    private String temporalCoverageDataType;

    private Integer observationNumber;

    private Integer timeSeriesNumber;

    private String spacialCoverage;

    private String temporalResolution;

    private List<String> spacialResolutions;
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getStatisticalUnit() {
        return statisticalUnit;
    }

    public void setStatisticalUnit(List<String> statisticalUnit) {
        this.statisticalUnit = statisticalUnit;
    }

    public String getDataStructure() {
        return dataStructure;
    }

    public void setDataStructure(String dataStructure) {
        this.dataStructure = dataStructure;
    }

    public String getTemporalCoverageStartDate() {
        return temporalCoverageStartDate;
    }

    public void setTemporalCoverageStartDate(String temporalCoverageStartDate) {
        this.temporalCoverageStartDate = temporalCoverageStartDate;
    }

    public String getTemporalCoverageEndDate() {
        return temporalCoverageEndDate;
    }

    public void setTemporalCoverageEndDate(String temporalCoverageEndDate) {
        this.temporalCoverageEndDate = temporalCoverageEndDate;
    }

    public String getTemporalCoverageDataType() {
        return temporalCoverageDataType;
    }

    public void setTemporalCoverageDataType(String temporalCoverageDataType) {
        this.temporalCoverageDataType = temporalCoverageDataType;
    }

    public Integer getObservationNumber() {
        return observationNumber;
    }

    public void setObservationNumber(Integer observationNumber) {
        this.observationNumber = observationNumber;
    }

    public Integer getTimeSeriesNumber() {
        return timeSeriesNumber;
    }

    public void setTimeSeriesNumber(Integer timeSeriesNumber) {
        this.timeSeriesNumber = timeSeriesNumber;
    }

    public String getSpacialCoverage() {
        return spacialCoverage;
    }

    public void setSpacialCoverage(String spacialCoverage) {
        this.spacialCoverage = spacialCoverage;
    }

    public String getTemporalResolution() {
        return temporalResolution;
    }

    public void setTemporalResolution(String temporalResolution) {
        this.temporalResolution = temporalResolution;
    }

    public List<String> getSpacialResolutions() {
        return spacialResolutions;
    }

    public void setSpacialResolutions(List<String> spacialResolutions) {
        this.spacialResolutions = spacialResolutions;
    }

    //
    private String id;
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

    private CatalogRecord catalogRecord;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}