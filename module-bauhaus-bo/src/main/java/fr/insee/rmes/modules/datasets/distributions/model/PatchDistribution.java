package fr.insee.rmes.modules.datasets.distributions.model;

import static fr.insee.rmes.modules.commons.webservice.ValidationMessages.INVALID_URL;

import org.hibernate.validator.constraints.URL;

/**
 * Corps du PATCH d'une distribution. La règle « au moins un des trois champs » reste au service, avec
 * son code d'erreur.
 */
public class PatchDistribution {
    private String updated;
    private String byteSize;

    @URL(message = INVALID_URL)
    private String url;

    public String getUpdated() {
        return updated;
    }

    public String getByteSize() {
        return byteSize;
    }

    public String getUrl() {
        return url;
    }

    public void setByteSize(String byteSize) {
        this.byteSize = byteSize;
    }

    public PatchDistribution() {}

    public static PatchDistribution of(String updated, String byteSize, String url) {
        PatchDistribution patch = new PatchDistribution();
        patch.updated = updated;
        patch.byteSize = byteSize;
        patch.url = url;
        return patch;
    }
}
