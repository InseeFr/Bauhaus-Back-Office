package fr.insee.rmes.persistance.service.sesame.notes;

import java.time.LocalDateTime;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.modele.concepts.Concept;
import fr.insee.rmes.modele.notes.DatableNote;
import fr.insee.rmes.modele.notes.VersionableNote;
import fr.insee.rmes.persistance.ontologies.EVOC;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.ontologies.PAV;
import fr.insee.rmes.persistance.ontologies.XKOS;
import fr.insee.rmes.persistance.service.sesame.utils.SesameService;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;
import fr.insee.rmes.persistance.sparql_queries.notes.NotesQueries;

public class NotesUtils  extends SesameService {
	
	private static final String ONE = "1";
	

	public void createRdfVersionableNote(String conceptId, VersionableNote versionableNote, Model model) {
		URI note = SesameUtils.versionableNoteIRI(conceptId, versionableNote);
		model.add(SesameUtils.conceptIRI(conceptId), versionableNote.getPredicat(), note, SesameUtils.conceptGraph());
		model.add(note, RDF.TYPE, XKOS.EXPLANATORY_NOTE, SesameUtils.conceptGraph());
		model.add(note, DCTERMS.LANGUAGE, SesameUtils.setLiteralLanguage(versionableNote.getLang()), SesameUtils.conceptGraph());
		model.add(note, PAV.VERSION, SesameUtils.setLiteralInt(versionableNote.getVersion()), SesameUtils.conceptGraph());
		model.add(note, INSEE.CONCEPT_VERSION, SesameUtils.setLiteralInt(versionableNote.getConceptVersion()), SesameUtils.conceptGraph());
		model.add(note, INSEE.VALIDFROM, SesameUtils.setLiteralDateTime(versionableNote.getValidFrom()), SesameUtils.conceptGraph());	
		model.add(note, EVOC.NOTE_LITERAL, SesameUtils.setLiteralXML(versionableNote.getContent()), SesameUtils.conceptGraph());
	}
	
	public void closeRdfVersionableNote(String conceptId, VersionableNote versionableNote, Model model)  throws RmesException{
		URI noteURIPreviousVersion = SesameUtils.previousVersionableNoteIRI(conceptId, versionableNote);
		Boolean isNoteExist = repoGestion.getResponseAsBoolean(NotesQueries.isExist(noteURIPreviousVersion));
		if (isNoteExist) {
			Boolean isNoteClosed = repoGestion.getResponseAsBoolean(NotesQueries.isClosed(noteURIPreviousVersion));
			if (!isNoteClosed) {
				model.add(noteURIPreviousVersion, INSEE.VALIDUNTIL, SesameUtils.setLiteralDateTime(LocalDateTime.now().toString()), SesameUtils.conceptGraph());
			}
		}
	}
	
	public void keepNote(String conceptId, VersionableNote versionableNote, Model model)  throws RmesException{
		URI conceptURI = SesameUtils.conceptIRI(conceptId);
		URI noteURI = SesameUtils.versionableNoteIRI(conceptId, versionableNote);
		Boolean isNoteExist = repoGestion.getResponseAsBoolean(NotesQueries.isExist(noteURI));
		if (isNoteExist) {
			model.add(conceptURI, versionableNote.getPredicat(), noteURI, SesameUtils.conceptGraph());
		}
	}
	
	public void keepHistoricalNotes(String conceptId, String conceptVersion, Model model)  throws RmesException{
		JSONArray notes = repoGestion.getResponseAsArray(
				NotesQueries.getHistoricalNotes(conceptId, conceptVersion));
		for (int i = 0; i < notes.length(); i++) {
			JSONObject note = (JSONObject) notes.get(i);
			URI predicat = SesameUtils.toURI(note.getString("predicat"));
			URI noteURI = SesameUtils.toURI(note.getString("note"));
			model.add(SesameUtils.conceptIRI(conceptId), predicat, noteURI, SesameUtils.conceptGraph());
		}
	}
	
	public void updateNoteConceptVersion(String conceptId, VersionableNote versionableNote, Model model)  throws RmesException{
		URI noteURI = SesameUtils.versionableNoteIRI(conceptId, versionableNote);
		Boolean isNoteExist = repoGestion.getResponseAsBoolean(NotesQueries.isExist(noteURI));
		if (isNoteExist) {
			Boolean isNoteClosed = repoGestion.getResponseAsBoolean(NotesQueries.isClosed(noteURI));
			if (!isNoteClosed) {
				String newConceptVersion = String.valueOf(Integer.parseInt(versionableNote.getConceptVersion()) + 1);
				model.add(noteURI, INSEE.CONCEPT_VERSION, SesameUtils.setLiteralInt(newConceptVersion), SesameUtils.conceptGraph());
			}
		}
	}

	public void createRdfDatableNote(String conceptId, DatableNote datableNote, Model model) {
		URI note = SesameUtils.datableNoteIRI(conceptId, datableNote);
		model.add(SesameUtils.conceptIRI(conceptId), datableNote.getPredicat(), note, SesameUtils.conceptGraph());
		model.add(note, RDF.TYPE, XKOS.EXPLANATORY_NOTE, SesameUtils.conceptGraph());
		model.add(note, DCTERMS.LANGUAGE, SesameUtils.setLiteralLanguage(datableNote.getLang()), SesameUtils.conceptGraph());
		model.add(note, INSEE.CONCEPT_VERSION, SesameUtils.setLiteralInt(datableNote.getConceptVersion()), SesameUtils.conceptGraph());
		model.add(note, DCTERMS.ISSUED, SesameUtils.setLiteralDateTime(datableNote.getIssued()), SesameUtils.conceptGraph());	
		model.add(note, EVOC.NOTE_LITERAL, SesameUtils.setLiteralXML(datableNote.getContent()), SesameUtils.conceptGraph());
	}
	
	public void deleteDatableNote(String conceptId, DatableNote datableNote, List<URI> notesToDelete)  throws RmesException{
		JSONObject noteToDelete = repoGestion.getResponseAsObject(NotesQueries.getChangeNoteToDelete(conceptId, datableNote));
		if (noteToDelete.length() != 0) {
			notesToDelete.add(SesameUtils.toURI(noteToDelete.getString("changeNoteURI")));
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
