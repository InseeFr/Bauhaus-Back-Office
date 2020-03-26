package fr.insee.rmes.modele.dissemination_status;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum DisseminationStatus {
    PRIVATE("Privé","http://id.insee.fr/codes/base/statutDiffusion/Prive"),
    PUBLIC_GENERIC("Public générique","http://id.insee.fr/codes/base/statutDiffusion/PublicGenerique"),
    PUBLIC_SPECIFIC("Public spécifique","http://id.insee.fr/codes/base/statutDiffusion/PublicSpecifique");
 
    private final String label;
    private final String url;
 
    private DisseminationStatus(String label, String url) {
        this.label = label;
        this.url = url;
    }

	public String getLabel() {
		return label;
	}
	public String getUrl() {
		return url;
	}
	public static String getEnumUrl(String label) {
		for (DisseminationStatus ds : DisseminationStatus.values()){
            if(label.equals(ds.getLabel())) return ds.getUrl();
        }
        return null;
	}
	public static String getEnumLabel(String url) {
		for (DisseminationStatus ds : DisseminationStatus.values()){
            if(url.equals(ds.getUrl())) return ds.getLabel();
        }
        return null;
	}
}