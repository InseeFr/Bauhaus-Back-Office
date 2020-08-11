package fr.insee.rmes.bauhaus_services.notes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.concepts.Concept;
import fr.insee.rmes.model.notes.DatableNote;
import fr.insee.rmes.model.notes.VersionableNote;
import fr.insee.rmes.model.notes.concepts.ConceptsDatedNoteTypes;
import fr.insee.rmes.model.notes.concepts.ConceptsVersionnedNoteTypes;

@Component
public class NoteManager {
	
	@Autowired
	NotesUtils noteUtils;

	public List<List<IRI>> setNotes(Concept concept, Model model) throws RmesException {
		// TODO : see extreme cases to close notes

		List<VersionableNote> versionableNotes = concept.getVersionableNotes();
		List<DatableNote> datableNotes = concept.getDatableNotes();
		
		String conceptId = concept.getId();
		String conceptVersion = noteUtils.getConceptVersion(concept);

		List<IRI> notesToDelete = new ArrayList<>();
		List<IRI> notesToUpdate = new ArrayList<>();
		
		Set<String> versionableNoteTypesInConcept = new HashSet<>();

		setVersionableNotes(concept, model, versionableNotes, conceptId, conceptVersion, notesToDelete,
				versionableNoteTypesInConcept);
		
		Set<String> versionableNoteTypes = new HashSet<>();
		for (ConceptsVersionnedNoteTypes c : ConceptsVersionnedNoteTypes.values()) {
			versionableNoteTypes.add(c.toString());
		}
		versionableNoteTypes.removeAll(versionableNoteTypesInConcept);
		setVersionableNoteTypes(concept, model, conceptId, conceptVersion, versionableNoteTypes);
		
		setDatableNotes(concept, model, datableNotes, conceptId, notesToDelete);
		
		// Keep historical notes
		noteUtils.keepHistoricalNotes(conceptId, conceptVersion, model);

		List<List<IRI>> notesToDeleteAndUpdate = new ArrayList<>();
		notesToDeleteAndUpdate.add(notesToDelete);
		notesToDeleteAndUpdate.add(notesToUpdate);

		return notesToDeleteAndUpdate;

	}

	private void setVersionableNotes(Concept concept, Model model, List<VersionableNote> versionableNotes, String conceptId, String conceptVersion, List<IRI> notesToDelete,
			Set<String> versionableNoteTypesInConcept) throws RmesException {
		for (VersionableNote versionableNote : versionableNotes) {
			versionableNoteTypesInConcept.add(versionableNote.getNoteType());
			for (ConceptsVersionnedNoteTypes c : ConceptsVersionnedNoteTypes.values()) {
				if (c.toString().equals(versionableNote.getNoteType())) {
					versionableNote.setPath(c.pathComponent());
					versionableNote.setPredicat(c.owlProperty());
					versionableNote.setLang(c.lang());
					versionableNote.setConceptVersion(conceptVersion);
					versionableNote.setVersion(noteUtils.getVersion(concept, versionableNote, "1"));
					if (Boolean.TRUE.equals(concept.getVersioning())) {
						// Close previous note
						noteUtils.closeRdfVersionableNote(conceptId, versionableNote, model);
					} else if (Boolean.FALSE.equals(concept.getCreation())){
						// Delete note in the current conceptVersion
						notesToDelete.add(RdfUtils.versionableNoteIRI(conceptId, versionableNote));
					}
					break;
				}
			}
			if (!versionableNote.getContent().isEmpty() && !versionableNote.getContent().equals("<div xmlns=\"http://www.w3.org/1999/xhtml\"></div>")) {
				noteUtils.createRdfVersionableNote(conceptId, versionableNote, model);
			}
		}
	}

	private void setDatableNotes(Concept concept, Model model, List<DatableNote> datableNotes,
			String conceptId, List<IRI> notesToDelete) throws RmesException {
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
	}

	private void setVersionableNoteTypes(Concept concept, Model model, String conceptId,
			String conceptVersion, Set<String> versionableNoteTypes) throws RmesException {
		for (String noteType : versionableNoteTypes) {
			ConceptsVersionnedNoteTypes versionnedNoteType = ConceptsVersionnedNoteTypes.getByName(noteType);
			VersionableNote versionableNote = new VersionableNote();
			versionableNote.setPath(versionnedNoteType.pathComponent());
			versionableNote.setConceptVersion(conceptVersion);
			versionableNote.setLang(versionnedNoteType.lang());
			versionableNote.setPredicat(versionnedNoteType.owlProperty());
			versionableNote.setVersion(noteUtils.getLastVersion(concept, versionableNote, "0"));
			// Update concept version of unchanged versionable notes
			if (Boolean.TRUE.equals(concept.getVersioning())) {
				String previousConceptVersion = String.valueOf(Integer.parseInt(conceptVersion) - 1);
				versionableNote.setConceptVersion(previousConceptVersion);
				noteUtils.updateNoteConceptVersion(conceptId, versionableNote, model);
			}
			// Keep link with unchanged notes of this concept version
			else if (Boolean.FALSE.equals(concept.getCreation())) {
				noteUtils.keepNote(conceptId, versionableNote, model);
			}
		}
	}
}
