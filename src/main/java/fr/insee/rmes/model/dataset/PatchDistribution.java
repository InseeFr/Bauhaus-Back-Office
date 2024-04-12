package fr.insee.rmes.model.dataset;

public class PatchDistribution {
    private String updated;
    private String taille;
    private String url;



    public String getUpdated() {
        return updated;
    }
    public String getTaille() {
        return taille;
    }

    public String getUrl() {
        return url;
    }

    public void setTaille(String taille) {
            this.taille = taille;
    }

    public PatchDistribution(String updated, String taille, String url) {
        this.updated = updated;
        this.taille = taille;
        this.url = url;
    }

    public PatchDistribution() {
    }
}
