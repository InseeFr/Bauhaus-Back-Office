package fr.insee.rmes.modules.datasets.distributions.model;

import static fr.insee.rmes.modules.commons.webservice.ValidationMessages.INVALID_URL;
import static fr.insee.rmes.modules.commons.webservice.ValidationMessages.REQUIRED;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

/**
 * Distribution, telle que lue et telle qu'écrite : c'est aussi le corps des requêtes de création et
 * de modification. Les contraintes reprennent celles du formulaire et du service (qui vérifie encore
 * la présence des champs obligatoires, sans Bean Validation).
 */
public class Distribution {
    String id;

    @NotBlank(message = REQUIRED)
    String idDataset;

    @NotBlank(message = REQUIRED)
    String labelLg1;

    @NotBlank(message = REQUIRED)
    String labelLg2;

    String descriptionLg1;
    String descriptionLg2;
    String language;
    String created;
    String updated;
    String format;
    String mediaType;
    String compressFormat;
    String byteSize;

    @URL(message = INVALID_URL)
    String accessUrl;

    @URL(message = INVALID_URL)
    String url;

    String validationState;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getCompressFormat() {
        return compressFormat;
    }

    public void setCompressFormat(String compressFormat) {
        this.compressFormat = compressFormat;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }

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
