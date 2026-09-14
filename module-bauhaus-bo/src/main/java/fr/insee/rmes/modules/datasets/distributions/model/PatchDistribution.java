package fr.insee.rmes.modules.datasets.distributions.model;

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

    public PatchDistribution() {}

    public static PatchDistribution of(String updated, String byteSize, String url) {
        PatchDistribution patch = new PatchDistribution();
        patch.updated = updated;
        patch.byteSize = byteSize;
        patch.url = url;
        return patch;
    }
}
