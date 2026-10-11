package fr.insee.rmes.modules.datasets.datasets.model;

import static fr.insee.rmes.modules.commons.webservice.ValidationMessages.REQUIRED;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CatalogRecord {
    @NotBlank(message = REQUIRED)
    private String creator;

    @NotEmpty(message = REQUIRED)
    private List<String> contributor;

    private String created;
    private String updated;

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public List<String> getContributor() {
        return contributor;
    }

    public void setContributor(List<String> contributor) {
        this.contributor = contributor;
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

    public void setUpdated(String udpated) {
        this.updated = udpated;
    }
}
