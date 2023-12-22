package fr.insee.rmes.model.dataset;

public class CatalogRecord {
    private String creator;
    private String contributor;
    private String created;
    private String updated;

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
