package fr.insee.rmes.model.classification;

import io.swagger.v3.oas.annotations.media.Schema;

public class ClassificationItemShortLabel {

    @Schema(description = "shortLabelLg1")
    public String shortLabelLg1;

    @Schema(description = "shortLabelLg2")
    public String shortLabelLg2;

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
}
