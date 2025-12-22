package fr.insee.rmes.modules.commons.domain.model;

public enum DisseminationStatus {
    PRIVATE("Privé","http://id.insee.fr/codes/base/statutDiffusion/Prive"),
    PUBLIC_GENERIC("Public générique","http://id.insee.fr/codes/base/statutDiffusion/PublicGenerique"),
    PUBLIC_SPECIFIC("Public spécifique","http://id.insee.fr/codes/base/statutDiffusion/PublicSpecifique");
 
    private final String label;
    private final String url;
 
    DisseminationStatus(String label, String url) {
        this.label = label;
        this.url = url;
    }

	public String getLabel() {
		return label;
	}

	public String getUrl() {
		return url;
	}

	public static String getEnumLabel(String url) {
		for (DisseminationStatus ds : DisseminationStatus.values()){
            if(url.equals(ds.url)) return ds.label;
        }
        return null;
	}
}