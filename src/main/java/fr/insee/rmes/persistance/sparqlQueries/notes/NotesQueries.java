package fr.insee.rmes.persistance.sparqlQueries.notes;

import org.openrdf.model.URI;

import fr.insee.rmes.modele.notes.DatableNote;

public class NotesQueries {

	public static String getLastVersionnableNoteVersion(String conceptId, URI predicat) {
		return "select ?version where { \n"
				+ "?concept <" + predicat + "> ?note . \n"
				+ "FILTER(REGEX(STR(?concept),'/concepts/definition/" + conceptId + "')) . \n"
				+ "?note pav:version ?version } \n"
				+ "ORDER BY DESC(?version) \n"
				+ "LIMIT 1";
	}
	
	public static String getConceptVersion(String conceptId) {
		return "select ?conceptVersion where { \n"
				+ "?concept ?predicat ?note . \n"
				+ "FILTER(REGEX(STR(?concept),'/concepts/definition/" + conceptId + "')) . \n"
				+ "?note insee:conceptVersion ?conceptVersion } \n"
				+ "ORDER BY DESC(?conceptVersion) \n"
				+ "LIMIT 1";
	}
	
	public static String getChangeNoteToDelete(String conceptId, DatableNote datableNote) {
		return "select ?changeNoteURI where { \n"
				+ "?concept skos:changeNote ?changeNoteURI . \n"
				+ "FILTER(REGEX(STR(?concept),'/concepts/definition/" + conceptId + "')) . \n"
				+ "?changeNoteURI dcterms:language '" + datableNote.getLang() + "'^^xsd:language . \n"
				+ "?changeNoteURI insee:conceptVersion '" + datableNote.getConceptVersion() + "'^^xsd:int}";
	}
		
	public static String getHistoricalNotes(String conceptId, String maxVersion) {
		return "SELECT ?note ?predicat \n"
				+ "WHERE { GRAPH <http://rdf.insee.fr/graphes/concepts/definitions> { \n"
				+ "?concept skos:notation '" + conceptId + "' . \n"
				+ "?concept ?predicat ?note . \n"
				+ "?note insee:conceptVersion ?conceptVersion \n"
				+ "FILTER(?conceptVersion < " + maxVersion + ") \n"
				+ "}}";
	}
	
	public static String isExist(URI note) {
		return "ASK { \n"
				+ "<" + note + "> ?b ?c \n"
				+ "}";
	}
	
	public static String isClosed(URI note) {
		return "ASK { \n"
				+ "<" + note + "> insee:validUntil ?c \n"
				+ "}";
	}
}
