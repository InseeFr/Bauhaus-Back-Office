package fr.insee.rmes.bauhaus_services.notes;

import java.time.LocalDateTime;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.json.JSONArray;
import org.json.JSONObject;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.concepts.Concept;
import fr.insee.rmes.model.notes.DatableNote;
import fr.insee.rmes.model.notes.VersionableNote;
import fr.insee.rmes.persistance.ontologies.EVOC;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.ontologies.PAV;
import fr.insee.rmes.persistance.ontologies.XKOS;
import fr.insee.rmes.persistance.sparql_queries.notes.NotesQueries;

public class NotesUtils  extends RdfService {
	
	private static final String ONE = "1";
	

	public void createRdfVersionableNote(String conceptId, VersionableNote versionableNote, Model model) {
		IRI note = RdfUtils.versionableNoteIRI(conceptId, versionableNote);
		model.add(RdfUtils.conceptIRI(conceptId), versionableNote.getPredicat(), note, RdfUtils.conceptGraph());
		model.add(note, RDF.TYPE, XKOS.EXPLANATORY_NOTE, RdfUtils.conceptGraph());
		model.add(note, DCTERMS.LANGUAGE, RdfUtils.setLiteralLanguage(versionableNote.getLang()), RdfUtils.conceptGraph());
		model.add(note, PAV.VERSION, RdfUtils.setLiteralInt(versionableNote.getVersion()), RdfUtils.conceptGraph());
		model.add(note, INSEE.CONCEPT_VERSION, RdfUtils.setLiteralInt(versionableNote.getConceptVersion()), RdfUtils.conceptGraph());
		model.add(note, INSEE.VALIDFROM, RdfUtils.setLiteralDateTime(versionableNote.getValidFrom()), RdfUtils.conceptGraph());	
		model.add(note, EVOC.NOTE_LITERAL, RdfUtils.setLiteralXML(versionableNote.getContent()), RdfUtils.conceptGraph());
	}
	
	public void closeRdfVersionableNote(String conceptId, VersionableNote versionableNote, Model model)  throws RmesException{
		IRI noteURIPreviousVersion = RdfUtils.previousVersionableNoteIRI(conceptId, versionableNote);
		Boolean isNoteExist = repoGestion.getResponseAsBoolean(NotesQueries.isExist(noteURIPreviousVersion));
		if (isNoteExist) {
			Boolean isNoteClosed = repoGestion.getResponseAsBoolean(NotesQueries.isClosed(noteURIPreviousVersion));
			if (!isNoteClosed) {
				model.add(noteURIPreviousVersion, INSEE.VALIDUNTIL, RdfUtils.setLiteralDateTime(LocalDateTime.now().toString()), RdfUtils.conceptGraph());
			}
		}
	}
	
	public void keepNote(String conceptId, VersionableNote versionableNote, Model model)  throws RmesException{
		IRI conceptURI = RdfUtils.conceptIRI(conceptId);
		IRI noteURI = RdfUtils.versionableNoteIRI(conceptId, versionableNote);
		Boolean isNoteExist = repoGestion.getResponseAsBoolean(NotesQueries.isExist(noteURI));
		if (isNoteExist) {
			model.add(conceptURI, versionableNote.getPredicat(), noteURI, RdfUtils.conceptGraph());
		}
	}
	
	public void keepHistoricalNotes(String conceptId, String conceptVersion, Model model)  throws RmesException{
		JSONArray notes = repoGestion.getResponseAsArray(
				NotesQueries.getHistoricalNotes(conceptId, conceptVersion));
		for (int i = 0; i < notes.length(); i++) {
			JSONObject note = (JSONObject) notes.get(i);
			IRI predicat = RdfUtils.toURI(note.getString("predicat"));
			IRI noteURI = RdfUtils.toURI(note.getString("note"));
			model.add(RdfUtils.conceptIRI(conceptId), predicat, noteURI, RdfUtils.conceptGraph());
		}
	}
	
	public void updateNoteConceptVersion(String conceptId, VersionableNote versionableNote, Model model)  throws RmesException{
		IRI noteURI = RdfUtils.versionableNoteIRI(conceptId, versionableNote);
		Boolean isNoteExist = repoGestion.getResponseAsBoolean(NotesQueries.isExist(noteURI));
		if (isNoteExist) {
			Boolean isNoteClosed = repoGestion.getResponseAsBoolean(NotesQueries.isClosed(noteURI));
			if (!isNoteClosed) {
				String newConceptVersion = String.valueOf(Integer.parseInt(versionableNote.getConceptVersion()) + 1);
				model.add(noteURI, INSEE.CONCEPT_VERSION, RdfUtils.setLiteralInt(newConceptVersion), RdfUtils.conceptGraph());
			}
		}
	}

	public void createRdfDatableNote(String conceptId, DatableNote datableNote, Model model) {
		IRI note = RdfUtils.datableNoteIRI(conceptId, datableNote);
		model.add(RdfUtils.conceptIRI(conceptId), datableNote.getPredicat(), note, RdfUtils.conceptGraph());
		model.add(note, RDF.TYPE, XKOS.EXPLANATORY_NOTE, RdfUtils.conceptGraph());
		model.add(note, DCTERMS.LANGUAGE, RdfUtils.setLiteralLanguage(datableNote.getLang()), RdfUtils.conceptGraph());
		model.add(note, INSEE.CONCEPT_VERSION, RdfUtils.setLiteralInt(datableNote.getConceptVersion()), RdfUtils.conceptGraph());
		model.add(note, DCTERMS.ISSUED, RdfUtils.setLiteralDateTime(datableNote.getIssued()), RdfUtils.conceptGraph());	
		model.add(note, EVOC.NOTE_LITERAL, RdfUtils.setLiteralXML(datableNote.getContent()), RdfUtils.conceptGraph());
	}
	
	public void deleteDatableNote(String conceptId, DatableNote datableNote, List<IRI> notesToDelete)  throws RmesException{
		JSONObject noteToDelete = repoGestion.getResponseAsObject(NotesQueries.getChangeNoteToDelete(conceptId, datableNote));
		if (noteToDelete.length() != 0) {
			notesToDelete.add(RdfUtils.toURI(noteToDelete.getString("changeNoteURI")));
		}
	}
	
	public String getVersion(Concept concept, VersionableNote note, String defaultVersion)  throws RmesException {
		if (concept.getCreation()) {
			return ONE;
		} else {
			String version = getLastVersion(concept,note,defaultVersion);
			if (!concept.getVersioning()) {
				return version;
			}
			return String.valueOf(Integer.parseInt(version)+1) ;
		}
	}
	
	public String getLastVersion(Concept concept, VersionableNote note, String defaultVersion)  throws RmesException{
		if (concept.getCreation()) {
			return ONE;
		} else {
			JSONObject jsonVersion = repoGestion.getResponseAsObject(
					NotesQueries.getLastVersionnableNoteVersion(concept.getId(), note.getPredicat()));
			if (jsonVersion.length() == 0) {
				return defaultVersion;
			}
			String version = jsonVersion.getString("version");
			return version ;
		}
	}
	
	public String getConceptVersion(Concept concept)  throws RmesException{
		String conceptVersion = ONE;
		JSONObject jsonConceptVersion = repoGestion.getResponseAsObject(NotesQueries.getConceptVersion(concept.getId()));
		if (jsonConceptVersion.length() == 0) {
			return conceptVersion;
		}
		conceptVersion = jsonConceptVersion.getString("conceptVersion");
		if (concept.getVersioning()) {
			conceptVersion = String.valueOf(Integer.parseInt(conceptVersion)+1) ;
		}
		return conceptVersion;
		
	}
}
