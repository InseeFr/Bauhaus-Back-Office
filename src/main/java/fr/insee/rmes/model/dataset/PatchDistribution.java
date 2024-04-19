package fr.insee.rmes.model.dataset;

public class PatchDistribution {
    private String updated;
    private String byteSize;
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

    public PatchDistribution(String updated, String byteSize, String url) {
        this.updated = updated;
        this.byteSize = byteSize;
        this.url = url;
    }

    public PatchDistribution() {
    }
}
