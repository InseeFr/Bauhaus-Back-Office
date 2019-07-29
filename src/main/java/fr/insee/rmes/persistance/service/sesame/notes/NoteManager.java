package fr.insee.rmes.persistance.service.sesame.notes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.URI;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.sesame.concepts.concepts.Concept;
import fr.insee.rmes.persistance.service.sesame.notes.concepts.ConceptsDatedNoteTypes;
import fr.insee.rmes.persistance.service.sesame.notes.concepts.ConceptsVersionnedNoteTypes;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;

public class NoteManager {

	public List<List<URI>> setNotes(Concept concept, Model model) throws RmesException {

		NotesUtils noteUtils = new NotesUtils();
		// TODO : see extreme cases to close notes

		List<VersionableNote> versionableNotes = concept.getVersionableNotes();
		List<DatableNote> datableNotes = concept.getDatableNotes();
		
		String conceptId = concept.getId();
		String conceptVersion = noteUtils.getConceptVersion(concept);

		List<URI> notesToDelete = new ArrayList<URI>();
		List<URI> notesToUpdate = new ArrayList<URI>();
		
		Set<String> versionableNoteTypesInConcept = new HashSet<String>();

		for (VersionableNote versionableNote : versionableNotes) {
			versionableNoteTypesInConcept.add(versionableNote.getNoteType());
			for (ConceptsVersionnedNoteTypes c : ConceptsVersionnedNoteTypes.values()) {
				if (c.toString().equals(versionableNote.getNoteType())) {
					versionableNote.setPath(c.pathComponent());
					versionableNote.setPredicat(c.owlProperty());
					versionableNote.setLang(c.lang());
					versionableNote.setConceptVersion(conceptVersion);
					versionableNote.setVersion(noteUtils.getVersion(concept, versionableNote, "1"));
					if (concept.getCreation()) {}
					else if (concept.getVersioning()) {
						// Close previous note
						noteUtils.closeRdfVersionableNote(conceptId, versionableNote, model);
					} else {
						// Delete note in the current conceptVersion
						notesToDelete.add(SesameUtils.versionableNoteIRI(conceptId, versionableNote));
					}
					break;
				}
			}
			if (!versionableNote.getContent().isEmpty() && !versionableNote.getContent().equals("<div xmlns=\"http://www.w3.org/1999/xhtml\"></div>"))
				noteUtils.createRdfVersionableNote(conceptId, versionableNote, model);
		}
		
		Set<String> versionableNoteTypes = new HashSet<String>();
		for (ConceptsVersionnedNoteTypes c : ConceptsVersionnedNoteTypes.values()) {
			versionableNoteTypes.add(c.toString());
		}
		versionableNoteTypes.removeAll(versionableNoteTypesInConcept);
		
		for (String noteType : versionableNoteTypes) {
			ConceptsVersionnedNoteTypes versionnedNoteType = ConceptsVersionnedNoteTypes.getByName(noteType);
			VersionableNote versionableNote = new VersionableNote();
			versionableNote.setPath(versionnedNoteType.pathComponent());
			versionableNote.setConceptVersion(conceptVersion);
			versionableNote.setLang(versionnedNoteType.lang());
			versionableNote.setPredicat(versionnedNoteType.owlProperty());
			versionableNote.setVersion(noteUtils.getLastVersion(concept, versionableNote, "0"));
			// Update concept version of unchanged versionable notes
			if (concept.getVersioning()) {
			String previousConceptVersion = String.valueOf(Integer.parseInt(conceptVersion) - 1);
			versionableNote.setConceptVersion(previousConceptVersion);
			noteUtils.updateNoteConceptVersion(conceptId, versionableNote, model);
			}
			// Keep link with unchanged notes of this concept version
			else if (!concept.getCreation()) {
				noteUtils.keepNote(conceptId, versionableNote, model);
			}
		}
		
		for (DatableNote datableNote : datableNotes) {
			for (ConceptsDatedNoteTypes c : ConceptsDatedNoteTypes.values()) {
				if (c.toString().equals(datableNote.getNoteType())) {
					datableNote.setPath(c.pathComponent());
					datableNote.setPredicat(c.owlProperty());
					datableNote.setLang(c.lang());
					datableNote.setConceptVersion(noteUtils.getConceptVersion(concept));
				}
			}
			noteUtils.deleteDatableNote(conceptId, datableNote, notesToDelete);
			noteUtils.createRdfDatableNote(conceptId, datableNote, model);
		}
		
		// Keep historical notes
		noteUtils.keepHistoricalNotes(conceptId, conceptVersion, model);

		List<List<URI>> notesToDeleteAndUpdate = new ArrayList<List<URI>>();
		notesToDeleteAndUpdate.add(notesToDelete);
		notesToDeleteAndUpdate.add(notesToUpdate);

		return notesToDeleteAndUpdate;

	}
}
