package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import static fr.insee.rmes.PropertiesKeys.DOCUMENTS_BASE_URI;
import static fr.insee.rmes.PropertiesKeys.LINKS_BASE_URI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.DocumentsStorageProperties;
import fr.insee.rmes.bauhaus_services.operations.OperationsParentRepository;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.modules.commons.configuration.StorageProperties;
import fr.insee.rmes.modules.commons.domain.model.Document;
import fr.insee.rmes.modules.commons.domain.port.serverside.FilesOperations;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationDocumentsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.IdGenerator;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;

/**
 * Lecture et suppression d'un document ou d'un lien.
 *
 * <p>Deux règles structurent la suppression : un document encore référencé par un SIMS n'est
 * pas supprimable, et seul un document — jamais un lien — possède un fichier à effacer du
 * stockage. Les tests verrouillent aussi le 404 typé attendu par le front quand l'identifiant
 * est inconnu.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentsUtilsReadDeleteTest {

    private static final String ID = "1000";
    private static final String DOCUMENT_IRI = "http://bauhaus/documents/document/" + ID;
    private static final String LINK_IRI = "http://bauhaus/documents/page/" + ID;
    private static final String STORAGE = "/mnt/documents-gestion";

    @Mock
    RepositoryGestion repoGestion;

    @Mock
    IdGenerator idGenerator;

    @Mock
    RepositoryPublication repositoryPublication;

    @Mock
    PublicationUtils publicationUtils;

    @Mock
    OperationsParentRepository operationsParentRepository;

    @Mock
    FilesOperations filesOperations;

    @Mock
    StorageProperties storageProperties;

    @Mock
    OperationDocumentsQueries operationDocumentsQueries;

    @Mock
    DocumentsStorageProperties documentsStorage;

    private DocumentsUtils documentsUtils;

    @BeforeEach
    void setUp() throws RmesException {
        RdfUtils.setGraphs(GraphsPropertiesStub.stub());
        RdfUtils.setBauhausUriBuilder(
                new BauhausUriBuilder("http://id.insee.fr/", "http://bauhaus/", name -> switch (name) {
                    case DOCUMENTS_BASE_URI -> Optional.of("documents/document");
                    case LINKS_BASE_URI -> Optional.of("documents/page");
                    default -> Optional.empty();
                }));

        documentsUtils = new DocumentsUtils(
                repoGestion,
                idGenerator,
                repositoryPublication,
                new BauhausLanguagesProperties("fr", "en"),
                publicationUtils,
                operationsParentRepository,
                filesOperations,
                storageProperties,
                operationDocumentsQueries,
                documentsStorage);

        when(storageProperties.directoryGestion()).thenReturn(STORAGE);
        when(repoGestion.getResponseAsArray(any())).thenReturn(new JSONArray());
        when(repoGestion.executeUpdate(any())).thenReturn(HttpStatus.OK);
    }

    @Test
    @DisplayName("Un document connu est restitué avec la date formatée et la liste de ses SIMS")
    void shouldReturnADocumentWithItsSims() throws RmesException {
        givenDocumentInDatabase(
                false,
                new JSONObject()
                        .put(Constants.URI, DOCUMENT_IRI)
                        .put(Constants.URL, "file:///mnt/documents-gestion/note.pdf")
                        .put(Constants.UPDATED_DATE, "2024-03-15T00:00:00.000"));
        when(operationDocumentsQueries.getSimsByDocument(ID, false)).thenReturn("sims-query");
        when(repoGestion.getResponseAsArray("sims-query"))
                .thenReturn(new JSONArray().put(new JSONObject().put(Constants.ID, "1234")));
        when(operationsParentRepository.getDocumentationOwnersByIdSims("1234")).thenReturn("[\"DG75-L001\"]");

        JSONObject document = documentsUtils.getDocument(ID, false);

        assertThat(document.getString(Constants.UPDATED_DATE)).isEqualTo("2024-03-15");
        JSONArray sims = document.getJSONArray("sims");
        assertThat(sims.length()).isOne();
        assertThat(sims.getJSONObject(0).getJSONArray(Constants.CREATORS).getString(0))
                .isEqualTo("DG75-L001");
    }

    @Test
    @DisplayName("Un identifiant de document inconnu remonte un 404 typé DOCUMENT_UNKNOWN_ID")
    void shouldRejectAnUnknownDocumentId() throws RmesException {
        givenDocumentInDatabase(false, new JSONObject());

        assertThatThrownBy(() -> documentsUtils.getDocument(ID, false))
                .isInstanceOf(RmesNotFoundException.class)
                .satisfies(thrown -> {
                    assertThat(detailsOf(thrown)).contains(String.valueOf(ErrorCodes.DOCUMENT_UNKNOWN_ID));
                    assertThat(detailsOf(thrown)).contains("Document");
                });
    }

    @Test
    @DisplayName("Un identifiant de lien inconnu remonte un 404 qui parle bien de lien")
    void shouldRejectAnUnknownLinkId() throws RmesException {
        givenDocumentInDatabase(true, new JSONObject());

        assertThatThrownBy(() -> documentsUtils.getDocument(ID, true))
                .isInstanceOf(RmesNotFoundException.class)
                .satisfies(thrown -> assertThat(detailsOf(thrown)).contains("Link"));
    }

    @Test
    @DisplayName("Supprimer un document efface son fichier puis ses triplets")
    void shouldDeleteTheFileAndTheTriplesOfADocument() throws RmesException {
        givenDocumentInDatabase(
                false,
                new JSONObject()
                        .put(Constants.URI, DOCUMENT_IRI)
                        .put(Constants.URL, "file:///mnt/documents-gestion/note.pdf"));

        HttpStatus status = documentsUtils.deleteDocument(ID, false);

        assertThat(status).isEqualTo(HttpStatus.OK);
        ArgumentCaptor<Document> deleted = ArgumentCaptor.forClass(Document.class);
        verify(filesOperations).delete(deleted.capture());
        assertThat(deleted.getValue().name()).isEqualTo("note.pdf");
        verify(repoGestion).executeUpdate(any());
    }

    @Test
    @DisplayName("Supprimer un lien n'essaie jamais d'effacer un fichier")
    void shouldNotTouchTheStorageWhenDeletingALink() throws RmesException {
        givenDocumentInDatabase(
                true, new JSONObject().put(Constants.URI, LINK_IRI).put(Constants.URL, "https://www.insee.fr/page"));

        documentsUtils.deleteDocument(ID, true);

        verify(filesOperations, never()).delete(any());
        verify(repoGestion).executeUpdate(any());
    }

    @Test
    @DisplayName("Un document encore rattaché à un SIMS n'est pas supprimable")
    void shouldRefuseToDeleteADocumentStillReferencedBySims() throws RmesException {
        givenDocumentInDatabase(
                false,
                new JSONObject()
                        .put(Constants.URI, DOCUMENT_IRI)
                        .put(Constants.URL, "file:///mnt/documents-gestion/note.pdf"));
        when(operationDocumentsQueries.getLinksToDocumentQuery(ID)).thenReturn("links-query");
        when(repoGestion.getResponseAsArray("links-query"))
                .thenReturn(new JSONArray()
                        .put(new JSONObject().put(Constants.TEXT, "http://bauhaus/qualite/attribut/1234/S.3.1/texte")));

        assertThatThrownBy(() -> documentsUtils.deleteDocument(ID, false))
                .isInstanceOf(RmesBadRequestException.class)
                .satisfies(thrown ->
                        assertThat(detailsOf(thrown)).contains(String.valueOf(ErrorCodes.DOCUMENT_DELETION_LINKED)));

        verify(filesOperations, never()).delete(any());
        verify(repoGestion, never()).executeUpdate(any());
    }

    private void givenDocumentInDatabase(boolean isLink, JSONObject document) throws RmesException {
        String query = "document-query-" + isLink;
        when(operationDocumentsQueries.getDocumentQuery(ID, isLink)).thenReturn(query);
        when(repoGestion.getResponseAsObject(query)).thenReturn(document);
    }

    private static String detailsOf(Throwable thrown) {
        return ((RmesException) thrown).getDetails();
    }
}
