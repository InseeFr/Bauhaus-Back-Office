package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

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
import fr.insee.rmes.exceptions.RmesFileException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.modules.commons.configuration.StorageProperties;
import fr.insee.rmes.modules.commons.domain.model.Document;
import fr.insee.rmes.modules.commons.domain.port.serverside.FilesOperations;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationDocumentsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.IdGenerator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Optional;

import static fr.insee.rmes.PropertiesKeys.DOCUMENTS_BASE_URI;
import static fr.insee.rmes.PropertiesKeys.LINKS_BASE_URI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Gestion du fichier attaché à un document : remplacement, téléchargement, contrôle du nom,
 * et génération de l'identifiant partagé entre documents et liens.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentsUtilsFileTest {

    private static final String ID = "1000";
    private static final String DOCUMENT_IRI = "http://bauhaus/documents/document/" + ID;

    @Mock RepositoryGestion repoGestion;
    @Mock IdGenerator idGenerator;
    @Mock RepositoryPublication repositoryPublication;
    @Mock PublicationUtils publicationUtils;
    @Mock OperationsParentRepository operationsParentRepository;
    @Mock FilesOperations filesOperations;
    @Mock StorageProperties storageProperties;
    @Mock OperationDocumentsQueries operationDocumentsQueries;
    @Mock DocumentsStorageProperties documentsStorage;

    @TempDir
    Path storageFolder;

    private DocumentsUtils documentsUtils;

    @BeforeEach
    void setUp() throws RmesException {
        RdfUtils.setGraphs(GraphsPropertiesStub.stub());
        RdfUtils.setBauhausUriBuilder(new BauhausUriBuilder("http://id.insee.fr/", "http://bauhaus/", name -> switch (name) {
            case DOCUMENTS_BASE_URI -> Optional.of("documents/document");
            case LINKS_BASE_URI -> Optional.of("documents/page");
            default -> Optional.empty();
        }));

        documentsUtils = new DocumentsUtils(repoGestion, idGenerator, repositoryPublication,
                new BauhausLanguagesProperties("fr", "en"), publicationUtils, operationsParentRepository,
                filesOperations, storageProperties, operationDocumentsQueries, documentsStorage);

        when(documentsStorage.storageGestion()).thenReturn(storageFolder.toString());
        when(storageProperties.directoryGestion()).thenReturn(storageFolder.toString());
        when(filesOperations.exists(storageFolder.toString())).thenReturn(true);
        when(repoGestion.getResponseAsArray(any())).thenReturn(new JSONArray());
        when(repoGestion.executeUpdate(any())).thenReturn(HttpStatus.OK);
    }

    @Test
    @DisplayName("Remplacer un fichier par un fichier de même nom conserve l'URL du document")
    void shouldKeepTheSameUrlWhenTheFileNameDoesNotChange() throws RmesException {
        givenDocumentWithFile("note.pdf");

        String newUrl = documentsUtils.changeFile(ID, content("nouveau contenu"), "note.pdf");

        assertThat(newUrl).as("aucune nouvelle URL puisque l'ancienne est réutilisée").isNull();
        verify(filesOperations, never()).delete(any());
        verify(repoGestion, never()).executeUpdate(any());
        verify(filesOperations).write(any(InputStream.class), any(Document.class));
    }

    @Test
    @DisplayName("Remplacer un fichier par un fichier d'un autre nom supprime l'ancien et réécrit l'URL")
    void shouldReplaceTheFileAndTheUrlWhenTheFileNameChanges() throws RmesException {
        givenDocumentWithFile("note.pdf");
        when(operationDocumentsQueries.changeDocumentUrlQuery(any(), any(), any())).thenReturn("change-url-query");

        String newUrl = documentsUtils.changeFile(ID, content("nouveau contenu"), "note-v2.pdf");

        assertThat(newUrl).endsWith("note-v2.pdf");
        ArgumentCaptor<Document> deleted = ArgumentCaptor.forClass(Document.class);
        verify(filesOperations).delete(deleted.capture());
        assertThat(deleted.getValue().name()).isEqualTo("note.pdf");
        verify(repoGestion).executeUpdate("change-url-query");
    }

    @Test
    @DisplayName("On ne peut pas attacher un fichier à un lien")
    void shouldRefuseToAttachAFileToALink() throws RmesException {
        // un lien n'a pas de schema:url exploitable comme fichier : la requête ne renvoie pas d'url
        when(operationDocumentsQueries.getDocumentQuery(ID, false)).thenReturn("document-query");
        when(repoGestion.getResponseAsObject("document-query"))
                .thenReturn(new JSONObject().put(Constants.URI, DOCUMENT_IRI));

        assertThatThrownBy(() -> documentsUtils.changeFile(ID, content("x"), "note.pdf"))
                .isInstanceOf(RmesException.class)
                .satisfies(thrown -> assertThat(((RmesException) thrown).getStatus())
                        .isEqualTo(HttpStatus.NOT_ACCEPTABLE.value()));
    }

    @Test
    @DisplayName("Le téléchargement renvoie le contenu du fichier avec un en-tête de pièce jointe")
    void shouldDownloadTheFileAsAnAttachment() throws Exception {
        givenDocumentWithFile("note.pdf");
        when(filesOperations.read(any(Document.class))).thenReturn(content("contenu du pdf"));

        ResponseEntity<Resource> response = documentsUtils.downloadDocumentFile(ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment; filename=\"note.pdf\"");
        assertThat(response.getBody().getContentAsByteArray()).asString().isEqualTo("contenu du pdf");
    }

    @Test
    @DisplayName("Le téléchargement d'un fichier absent du stockage remonte l'erreur de l'adaptateur")
    void shouldPropagateTheStorageErrorWhenTheFileIsMissing() throws RmesException {
        // Le port FilesOperations ne laisse pas remonter d'IOException : ses deux adaptateurs
        // (système de fichiers et Minio) encapsulent l'absence de fichier dans une
        // RmesFileException, que RmesExceptionHandler traduit en 500. Le catch NoSuchFileException
        // de downloadDocumentFile n'est donc jamais atteint ; ce test fige ce qui se produit
        // réellement plutôt qu'un 404 attendu mais jamais renvoyé.
        givenDocumentWithFile("note.pdf");
        when(filesOperations.read(any(Document.class)))
                .thenThrow(new RmesFileException("note.pdf", "Failed to read file", new NoSuchFileException("note.pdf")));

        assertThatThrownBy(() -> documentsUtils.downloadDocumentFile(ID))
                .isInstanceOf(RmesFileException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    @DisplayName("Un nom de fichier vide est refusé")
    void shouldRejectAnEmptyFileName(String fileName) {
        assertThatThrownBy(() -> documentsUtils.checkFileNameValidity(fileName))
                .isInstanceOf(RmesNotAcceptableException.class)
                .satisfies(thrown -> assertThat(((RmesException) thrown).getDetails())
                        .contains(String.valueOf(ErrorCodes.DOCUMENT_EMPTY_NAME)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "sans-extension", "espace dans le nom.pdf", "accentué.pdf", "../evasion.pdf", "note.pdf.exe" })
    @DisplayName("Un nom de fichier hors alphanumérique, tiret et souligné est refusé")
    void shouldRejectAForbiddenFileName(String fileName) {
        assertThatThrownBy(() -> documentsUtils.checkFileNameValidity(fileName))
                .isInstanceOf(RmesNotAcceptableException.class)
                .satisfies(thrown -> assertThat(((RmesException) thrown).getDetails())
                        .contains(String.valueOf(ErrorCodes.DOCUMENT_FORBIDDEN_CHARACTER_NAME)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "note.pdf", "note_v2.odt", "note-v2.PDF", "NOTE2.pdf" })
    @DisplayName("Un nom de fichier alphanumérique avec extension est accepté")
    void shouldAcceptAValidFileName(String fileName) {
        assertThatCode(() -> documentsUtils.checkFileNameValidity(fileName)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Le prochain identifiant suit le plus grand des identifiants de documents et de liens")
    void shouldGenerateAnIdAfterTheHighestDocumentOrLinkId() throws RmesException {
        when(operationDocumentsQueries.lastDocumentID()).thenReturn("last-document");
        when(operationDocumentsQueries.lastLinkID()).thenReturn("last-link");
        when(repoGestion.getResponseAsObject("last-document")).thenReturn(new JSONObject().put(Constants.ID, "1004"));
        when(repoGestion.getResponseAsObject("last-link")).thenReturn(new JSONObject().put(Constants.ID, "1042"));

        assertThat(documentsUtils.createDocumentID()).isEqualTo("1043");
    }

    @Test
    @DisplayName("Une base sans aucun document ni lien démarre la numérotation à 1000")
    void shouldStartNumberingAtOneThousandOnAnEmptyBase() throws RmesException {
        when(operationDocumentsQueries.lastDocumentID()).thenReturn("last-document");
        when(operationDocumentsQueries.lastLinkID()).thenReturn("last-link");
        when(repoGestion.getResponseAsObject(any())).thenReturn(new JSONObject());

        assertThat(documentsUtils.createDocumentID()).isEqualTo("1000");
    }

    @Test
    @DisplayName("Un identifiant non renseigné dans la base est traité comme absent")
    void shouldTreatUndefinedIdsAsMissing() {
        assertThat(documentsUtils.getIdFromJson(new JSONObject())).isNull();
        assertThat(documentsUtils.getIdFromJson(new JSONObject().put(Constants.ID, Constants.UNDEFINED))).isNull();
        assertThat(documentsUtils.getIdFromJson(new JSONObject().put(Constants.ID, ""))).isNull();
        assertThat(documentsUtils.getIdFromJson(new JSONObject().put(Constants.ID, "42"))).isEqualTo(42);
    }

    @Test
    @DisplayName("L'URL stockée est restituée sans son schéma file://")
    void shouldStripTheFileSchemeFromTheStoredUrl() {
        JSONObject document = new JSONObject().put(Constants.URL, "file:///mnt/documents/note.pdf");

        assertThat(DocumentsUtils.getDocumentUrlFromDocument(document)).isEqualTo("/mnt/documents/note.pdf");
        assertThat(DocumentsUtils.getDocumentNameFromUrl("/mnt/documents/note.pdf")).isEqualTo("note.pdf");
    }

    private void givenDocumentWithFile(String fileName) throws RmesException {
        when(operationDocumentsQueries.getDocumentQuery(ID, false)).thenReturn("document-query");
        when(repoGestion.getResponseAsObject("document-query")).thenReturn(new JSONObject()
                .put(Constants.URI, DOCUMENT_IRI)
                .put(Constants.URL, "file://" + storageFolder.resolve(fileName)));
    }

    private static InputStream content(String content) {
        return new ByteArrayInputStream(content.getBytes());
    }
}
