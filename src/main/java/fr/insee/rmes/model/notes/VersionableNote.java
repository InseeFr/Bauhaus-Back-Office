package fr.insee.rmes.model.notes;

import org.eclipse.rdf4j.model.IRI;

import java.time.LocalDateTime;

public class  VersionableNote {


	private String noteType;
	private String lang;
	private String content;
	private String validFrom;
	private String version;
	private String conceptVersion;
	private String path;
	private IRI predicat;
	
	public VersionableNote() {
		validFrom = LocalDateTime.now().toString();
	}
	
	
	public String getNoteType() {
		return noteType;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public void setConceptVersion(String conceptVersion) {
		this.conceptVersion = conceptVersion;
	}
	public String getContent() {
		return content;
	}
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}
	public String getConceptVersion() {
		return conceptVersion;
	}
	public String getVersion() {
		return version;
	}
	public String getValidFrom() {
		return validFrom;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public IRI getPredicat() {
		return predicat;
	}
	public void setPredicat(IRI predicat) {
		this.predicat = predicat;
	}
	
}
