package fr.insee.rmes.bauhaus_services.notes;

import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausIriFactory;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfTriples;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.EVOC;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.graphdb.ontologies.PAV;
import fr.insee.rmes.graphdb.ontologies.XKOS;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.model.concepts.Concept;
import fr.insee.rmes.modules.concepts.concept.domain.model.notes.DatableNote;
import fr.insee.rmes.modules.concepts.concept.domain.model.notes.VersionableNote;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptNotesQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.IdGenerator;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;
import java.time.LocalDateTime;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

@Repository
public class NotesRepository extends RdfService {

    private final ConceptNotesQueries conceptNotesQueries;
    private final BauhausIriFactory iriFactory;

    public NotesRepository(
            RepositoryGestion repoGestion,
            IdGenerator idGenerator,
            RepositoryPublication repositoryPublication,
            PublicationUtils publicationUtils,
            ConceptNotesQueries conceptNotesQueries,
            BauhausIriFactory iriFactory) {
        super(repoGestion, idGenerator, repositoryPublication, publicationUtils);
        this.conceptNotesQueries = conceptNotesQueries;
        this.iriFactory = iriFactory;
    }

    private static final String ONE = "1";
    private static final String ZERO = "0";

    public void createRdfVersionableNote(String conceptId, VersionableNote versionableNote, Model model) {
        IRI note = iriFactory.versionableNote(conceptId, versionableNote);
        model.add(iriFactory.concept(conceptId), versionableNote.getPredicat(), note, iriFactory.conceptGraph());
        model.add(note, RDF.TYPE, XKOS.EXPLANATORY_NOTE, iriFactory.conceptGraph());
        model.add(note, DCTERMS.LANGUAGE, RdfTriples.language(versionableNote.getLang()), iriFactory.conceptGraph());
        model.add(note, PAV.VERSION, RdfTriples.integer(versionableNote.getVersion()), iriFactory.conceptGraph());
        model.add(
                note,
                INSEE.CONCEPT_VERSION,
                RdfTriples.integer(versionableNote.getConceptVersion()),
                iriFactory.conceptGraph());
        model.add(
                note, INSEE.VALIDFROM, RdfTriples.dateTime(versionableNote.getValidFrom()), iriFactory.conceptGraph());
        model.add(
                note,
                EVOC.NOTE_LITERAL,
                RdfTriples.xmlLiteral(XhtmlToMarkdownUtils.markdownToXhtml(versionableNote.getContent())),
                iriFactory.conceptGraph());
        model.add(note, RDF.VALUE, RdfTriples.string(versionableNote.getContent()), iriFactory.conceptGraph());
    }

    public void closeRdfVersionableNote(String conceptId, VersionableNote versionableNote, Model model)
            throws RmesException {
        IRI noteURIPreviousVersion = iriFactory.previousVersionableNote(conceptId, versionableNote);
        Boolean isNoteExist = repoGestion.getResponseAsBoolean(conceptNotesQueries.isExist(noteURIPreviousVersion));
        if (Boolean.TRUE.equals(isNoteExist)) {
            Boolean isNoteClosed =
                    repoGestion.getResponseAsBoolean(conceptNotesQueries.isClosed(noteURIPreviousVersion));
            if (Boolean.FALSE.equals(isNoteClosed)) {
                model.add(
                        noteURIPreviousVersion,
                        INSEE.VALIDUNTIL,
                        RdfTriples.dateTime(LocalDateTime.now().toString()),
                        iriFactory.conceptGraph());
            }
        }
    }

    public void keepNote(String conceptId, VersionableNote versionableNote, Model model) throws RmesException {
        IRI conceptURI = iriFactory.concept(conceptId);
        IRI noteURI = iriFactory.versionableNote(conceptId, versionableNote);
        Boolean isNoteExist = repoGestion.getResponseAsBoolean(conceptNotesQueries.isExist(noteURI));
        if (Boolean.TRUE.equals(isNoteExist)) {
            model.add(conceptURI, versionableNote.getPredicat(), noteURI, iriFactory.conceptGraph());
        }
    }

    public void keepHistoricalNotes(String conceptId, String conceptVersion, Model model) throws RmesException {
        JSONArray notes =
                repoGestion.getResponseAsArray(conceptNotesQueries.getHistoricalNotes(conceptId, conceptVersion));
        JSONUtils.stream(notes).forEach(note -> {
            IRI predicat = RdfTriples.iri(note.getString("predicat"));
            IRI noteURI = RdfTriples.iri(note.getString("note"));
            model.add(iriFactory.concept(conceptId), predicat, noteURI, iriFactory.conceptGraph());
        });
    }

    public void updateNoteConceptVersion(String conceptId, VersionableNote versionableNote, Model model)
            throws RmesException {
        IRI noteURI = iriFactory.versionableNote(conceptId, versionableNote);
        Boolean isNoteExist = repoGestion.getResponseAsBoolean(conceptNotesQueries.isExist(noteURI));
        if (Boolean.TRUE.equals(isNoteExist)) {
            Boolean isNoteClosed = repoGestion.getResponseAsBoolean(conceptNotesQueries.isClosed(noteURI));
            if (Boolean.FALSE.equals(isNoteClosed)) {
                String newConceptVersion = String.valueOf(Integer.parseInt(versionableNote.getConceptVersion()) + 1);
                model.add(
                        noteURI,
                        INSEE.CONCEPT_VERSION,
                        RdfTriples.integer(newConceptVersion),
                        iriFactory.conceptGraph());
            }
        }
    }

    public void createRdfDatableNote(String conceptId, DatableNote datableNote, Model model) {
        IRI note = iriFactory.datableNote(conceptId, datableNote);
        model.add(iriFactory.concept(conceptId), datableNote.getPredicat(), note, iriFactory.conceptGraph());
        model.add(note, RDF.TYPE, XKOS.EXPLANATORY_NOTE, iriFactory.conceptGraph());
        model.add(note, DCTERMS.LANGUAGE, RdfTriples.language(datableNote.getLang()), iriFactory.conceptGraph());
        model.add(
                note,
                INSEE.CONCEPT_VERSION,
                RdfTriples.integer(datableNote.getConceptVersion()),
                iriFactory.conceptGraph());
        model.add(note, DCTERMS.ISSUED, RdfTriples.dateTime(datableNote.getIssued()), iriFactory.conceptGraph());
        model.add(note, EVOC.NOTE_LITERAL, RdfTriples.xmlLiteral(datableNote.getContent()), iriFactory.conceptGraph());
        model.add(note, RDF.VALUE, RdfTriples.string(datableNote.getContent()), iriFactory.conceptGraph());
    }

    public void deleteDatableNote(String conceptId, DatableNote datableNote, List<IRI> notesToDelete)
            throws RmesException {
        JSONObject noteToDelete =
                repoGestion.getResponseAsObject(conceptNotesQueries.getChangeNoteToDelete(conceptId, datableNote));
        if (!noteToDelete.isEmpty()) {
            notesToDelete.add(RdfTriples.iri(noteToDelete.getString("changeNoteURI")));
        }
    }

    public String getVersion(Concept concept, VersionableNote note, String defaultVersion) throws RmesException {
        if (Boolean.TRUE.equals(concept.getCreation())) {
            return ONE;
        }
        if (Boolean.FALSE.equals(concept.getVersioning())) {
            return getLastVersion(concept, note, defaultVersion);
        }
        // A note left empty until now has no version to increment : it starts at 1, not at 2.
        return String.valueOf(Integer.parseInt(getLastVersion(concept, note, ZERO)) + 1);
    }

    public String getLastVersion(Concept concept, VersionableNote note, String defaultVersion) throws RmesException {
        if (Boolean.TRUE.equals(concept.getCreation())) {
            return ONE;
        } else {
            JSONObject jsonVersion = repoGestion.getResponseAsObject(
                    conceptNotesQueries.getLastVersionnableNoteVersion(concept.getId(), note.getPredicat()));
            if (jsonVersion.isEmpty()) {
                return defaultVersion;
            }
            return jsonVersion.getString("version");
        }
    }

    public String getConceptVersion(Concept concept) throws RmesException {
        String conceptVersion = ONE;
        JSONObject jsonConceptVersion =
                repoGestion.getResponseAsObject(conceptNotesQueries.getConceptVersion(concept.getId()));
        if (jsonConceptVersion.isEmpty()) {
            return conceptVersion;
        }
        conceptVersion = jsonConceptVersion.getString("conceptVersion");
        if (Boolean.TRUE.equals(concept.getVersioning())) {
            conceptVersion = String.valueOf(Integer.parseInt(conceptVersion) + 1);
        }
        return conceptVersion;
    }
}
