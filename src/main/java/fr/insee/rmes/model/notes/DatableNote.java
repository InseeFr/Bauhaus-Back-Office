package fr.insee.rmes.model.notes;

import java.time.LocalDateTime;

import org.eclipse.rdf4j.model.IRI;

public class DatableNote {
	
	private String noteType;
	private String lang;
	private String content;
	private String issued;
	private String conceptVersion;
	private String path;
	private IRI predicat;
	
	public DatableNote() {
		issued = LocalDateTime.now().toString();
		conceptVersion = "1";
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
	public void setConceptVersion(String conceptVersion) {
		this.conceptVersion = conceptVersion;
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
	public String getNoteType() {
		return noteType;
	}
	public String getContent() {
		return content;
	}
	public String getIssued() {
		return issued;
	}

}
