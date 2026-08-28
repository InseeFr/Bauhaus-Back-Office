package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import fr.insee.rmes.BauhausLanguagesProperties;
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
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.modules.commons.configuration.StorageProperties;
import fr.insee.rmes.modules.commons.domain.model.Document;
import fr.insee.rmes.modules.commons.domain.port.serverside.FilesOperations;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationDocumentsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.IdGenerator;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static fr.insee.rmes.PropertiesKeys.DOCUMENTS_BASE_URI;
import static fr.insee.rmes.PropertiesKeys.LINKS_BASE_URI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Règles métier de la création et de la mise à jour d'un document ou d'un lien.
 *
 * <p>Documents et liens partagent le même type RDF ({@code foaf:Document}) et le même graphe ;
 * ce qui les distingue est l'IRI ({@code documents/document/...} contre {@code documents/page/...}),
 * le fichier déposé pour l'un et l'URL externe pour l'autre. Ces tests verrouillent cette
 * distinction et les contrôles qui la protègent.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentsUtilsCreationTest {

    private static final String ID = "1000";
    private static final String DOCUMENT_IRI = "http://bauhaus/documents/document/" + ID;
    private static final String LINK_IRI = "http://bauhaus/documents/page/" + ID;

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
        // par défaut : aucun libellé en doublon, aucune URL déjà référencée
        when(repoGestion.getResponseAsBoolean(any())).thenReturn(false);
        when(repoGestion.getResponseAsObject(any())).thenReturn(new JSONObject());
    }

    @Test
    @DisplayName("Un document est écrit sous l'IRI documents/document et son fichier est déposé dans le stockage gestion")
    void shouldWriteADocumentAndUploadItsFile() throws RmesException {
        documentsUtils.createDocument(ID, """
                {"labelLg1": "Note méthodologique", "labelLg2": "Methodological note"}""",
                false, content("contenu"), "note.pdf");

        Model model = modelLoadedFor(DOCUMENT_IRI);
        assertThat(model.filter(null, RDF.TYPE, FOAF.DOCUMENT).subjects())
                .extracting(Object::toString)
                .containsExactly(DOCUMENT_IRI);
        assertThat(labelsOf(model)).containsExactlyInAnyOrder("Note méthodologique", "Methodological note");

        ArgumentCaptor<Document> uploaded = ArgumentCaptor.forClass(Document.class);
        verify(filesOperations).write(any(InputStream.class), uploaded.capture());
        assertThat(uploaded.getValue().name()).isEqualTo("note.pdf");
    }

    @Test
    @DisplayName("Un lien est écrit sous l'IRI documents/page et ne dépose aucun fichier")
    void shouldWriteALinkWithoutUploadingAnyFile() throws RmesException {
        documentsUtils.createDocument(ID, """
                {"labelLg1": "Site de l'Insee", "url": "https://www.insee.fr/page"}""",
                true, null, null);

        Model model = modelLoadedFor(LINK_IRI);
        assertThat(model.filter(null, RDF.TYPE, FOAF.DOCUMENT).subjects())
                .extracting(Object::toString)
                .containsExactly(LINK_IRI);
        verify(filesOperations, never()).write(any(), any());
    }

    @Test
    @DisplayName("Un lien sans URL est refusé")
    void shouldRejectALinkWithoutUrl() {
        assertThatThrownBy(() -> documentsUtils.createDocument(ID, """
                {"labelLg1": "Lien sans url"}""", true, null, null))
                .isInstanceOf(RmesNotAcceptableException.class)
                .satisfies(thrown -> assertThat(detailsOf(thrown)).contains(String.valueOf(ErrorCodes.LINK_EMPTY_URL)));
    }

    @Test
    @DisplayName("Un lien dont l'URL est malformée est refusé")
    void shouldRejectALinkWithAMalformedUrl() {
        assertThatThrownBy(() -> documentsUtils.createDocument(ID, """
                {"labelLg1": "Lien cassé", "url": "pas-une-url"}""", true, null, null))
                .isInstanceOf(RmesNotAcceptableException.class)
                .satisfies(thrown -> assertThat(detailsOf(thrown)).contains(String.valueOf(ErrorCodes.LINK_BAD_URL)));
    }

    @Test
    @DisplayName("Un lien dont l'URL est déjà référencée par un autre lien est refusé")
    void shouldRejectALinkWhoseUrlIsAlreadyUsed() throws RmesException {
        when(operationDocumentsQueries.getDocumentUriQuery("https://www.insee.fr/page")).thenReturn("existing-url-query");
        when(repoGestion.getResponseAsObject("existing-url-query"))
                .thenReturn(new JSONObject().put("document", "http://bauhaus/documents/page/999"));

        assertThatThrownBy(() -> documentsUtils.createDocument(ID, """
                {"labelLg1": "Doublon", "url": "https://www.insee.fr/page"}""", true, null, null))
                .isInstanceOf(RmesNotAcceptableException.class)
                .satisfies(thrown -> assertThat(detailsOf(thrown)).contains(String.valueOf(ErrorCodes.LINK_EXISTING_URL)));
    }

    @Test
    @DisplayName("Un libellé lg1 déjà porté par un autre document ou lien est refusé")
    void shouldRejectADuplicatedLabelLg1() throws RmesException {
        when(operationDocumentsQueries.checkLabelUnicity(any(), eq("Doublon"), eq("fr"))).thenReturn("unicity-lg1");
        when(repoGestion.getResponseAsBoolean("unicity-lg1")).thenReturn(true);

        assertThatThrownBy(() -> documentsUtils.createDocument(ID, """
                {"labelLg1": "Doublon"}""", false, content("x"), "doublon.pdf"))
                .isInstanceOf(RmesBadRequestException.class)
                .satisfies(thrown -> assertThat(detailsOf(thrown))
                        .contains(ErrorCodes.OPERATION_DOCUMENT_LINK_EXISTING_LABEL_LG1));
    }

    @Test
    @DisplayName("Un libellé lg2 déjà porté par un autre document ou lien est refusé")
    void shouldRejectADuplicatedLabelLg2() throws RmesException {
        when(operationDocumentsQueries.checkLabelUnicity(any(), eq("Duplicate"), eq("en"))).thenReturn("unicity-lg2");
        when(repoGestion.getResponseAsBoolean("unicity-lg2")).thenReturn(true);

        assertThatThrownBy(() -> documentsUtils.createDocument(ID, """
                {"labelLg1": "Unique", "labelLg2": "Duplicate"}""", false, content("x"), "unique.pdf"))
                .isInstanceOf(RmesBadRequestException.class)
                .satisfies(thrown -> assertThat(detailsOf(thrown))
                        .contains(ErrorCodes.OPERATION_DOCUMENT_LINK_EXISTING_LABEL_LG2));
    }

    @Test
    @DisplayName("Un libellé absent n'est pas soumis au contrôle d'unicité")
    void shouldNotCheckUnicityOfAMissingLabel() throws RmesException {
        documentsUtils.createDocument(ID, """
                {"labelLg1": "Sans libellé anglais"}""", false, content("x"), "sans-lg2.pdf");

        // Une valeur nulle injectée dans une requête SPARQL est refusée par SparqlLiterals :
        // interroger la base pour un libellé absent se solderait par une 500.
        verify(operationDocumentsQueries, never()).checkLabelUnicity(any(), eq(null), any());
        assertThat(modelLoadedFor(DOCUMENT_IRI)).isNotEmpty();
    }

    @Test
    @DisplayName("Un fichier déjà présent dans le stockage n'est pas écrasé silencieusement")
    void shouldRejectADocumentWhoseFileAlreadyExistsOnDisk() throws Exception {
        Files.writeString(storageFolder.resolve("deja-la.pdf"), "contenu existant");

        assertThatThrownBy(() -> documentsUtils.createDocument(ID, """
                {"labelLg1": "Nouveau"}""", false, content("x"), "deja-la.pdf"))
                .isInstanceOf(RmesBadRequestException.class)
                .satisfies(thrown -> assertThat(detailsOf(thrown))
                        .contains(String.valueOf(ErrorCodes.DOCUMENT_CREATION_EXISTING_FILE)));
    }

    @Test
    @DisplayName("La mise à jour d'un document écrit sous l'IRI document, celle d'un lien sous l'IRI page")
    void shouldUpdateUnderTheIriMatchingTheKind() throws RmesException {
        documentsUtils.setDocument(ID, """
                {"labelLg1": "Document mis à jour"}""", false);
        assertThat(modelLoadedFor(DOCUMENT_IRI)).isNotEmpty();

        documentsUtils.setDocument(ID, """
                {"labelLg1": "Lien mis à jour", "url": "https://www.insee.fr/page"}""", true);
        assertThat(modelLoadedFor(LINK_IRI)).isNotEmpty();
    }

    private Model modelLoadedFor(String iri) throws RmesException {
        ArgumentCaptor<IRI> uriCaptor = ArgumentCaptor.forClass(IRI.class);
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(repoGestion, atLeastOnce()).loadSimpleObject(uriCaptor.capture(), modelCaptor.capture());
        for (int i = 0; i < uriCaptor.getAllValues().size(); i++) {
            if (iri.equals(uriCaptor.getAllValues().get(i).stringValue())) {
                return modelCaptor.getAllValues().get(i);
            }
        }
        throw new AssertionError("Aucun objet chargé sous l'IRI " + iri);
    }

    private static String detailsOf(Throwable thrown) {
        return ((RmesException) thrown).getDetails();
    }

    private static List<String> labelsOf(Model model) {
        return model.filter(null, RDFS.LABEL, null).objects().stream()
                .map(Value::stringValue)
                .toList();
    }

    private static InputStream content(String content) {
        return new ByteArrayInputStream(content.getBytes());
    }
}
