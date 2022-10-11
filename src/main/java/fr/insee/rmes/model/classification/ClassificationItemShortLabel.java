package fr.insee.rmes.model.classification;

import io.swagger.v3.oas.annotations.media.Schema;

public class ClassificationItemShortLabel {

    @Schema(description = "length")
    public String length;

    @Schema(description = "shortLabelLg1")
    public String shortLabelLg1;

    @Schema(description = "shortLabelLg2")
    public String shortLabelLg2;

    @Schema(description = "shortLabelUri")
    public String shortLabelUri;

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getShortLabelLg1() {
        return shortLabelLg1;
    }

    public void setShortLabelLg1(String shortLabelLg1) {
        this.shortLabelLg1 = shortLabelLg1;
    }

    public String getShortLabelLg2() {
        return shortLabelLg2;
    }

    public void setShortLabelLg2(String shortLabelLg2) {
        this.shortLabelLg2 = shortLabelLg2;
    }

    public String getShortLabelUri() {
        return shortLabelUri;
    }

    public void setShortLabelUri(String shortLabelUri) {
        this.shortLabelUri = shortLabelUri;
    }
}
