package fr.insee.rmes.testcontainers.documents;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.BauhausUriProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.DocumentsStorageProperties;
import fr.insee.rmes.bauhaus_services.DocumentsService;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsImpl;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.modules.commons.configuration.StorageProperties;
import fr.insee.rmes.modules.commons.infrastructure.filessystem.FileSystemOperation;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationDocumentsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import fr.insee.rmes.utils.IdGenerator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
import static org.mockito.Mockito.mock;

/**
 * Cycle de vie complet d'un document et d'un lien contre un vrai GraphDB et un vrai stockage
 * de fichiers : c'est le seul endroit où les requêtes SPARQL de l'API documents et liens sont
 * réellement exécutées (création, relecture, mise à jour, remplacement du fichier, suppression).
 *
 * <p>Les tests unitaires figent les règles métier avec un dépôt simulé ; ils ne diraient rien
 * d'une requête qui ne ramène pas ce qu'elle promet. Ici, tout ce qui est écrit est relu par
 * l'API elle-même.
 *
 * <p>Le conteneur GraphDB est partagé entre les classes de test : les assertions portent donc
 * sur la présence des objets créés par ce test, jamais sur des dénombrements globaux.
 */
@Tag("integration")
class DocumentsCrudIntegrationTest extends WithGraphDBContainer {

    private static final String BASE_URI_GESTION = "http://bauhaus/";
    /** Valeurs de bauhaus-core.properties : documents et liens ne se distinguent que par ce segment. */
    private static final String DOCUMENTS_PATH = "documents/document";
    private static final String LINKS_PATH = "documents/page";

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(),
            new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    @TempDir
    Path storageFolder;

    private DocumentsService documents;

    @BeforeEach
    void setUp() {
        RdfUtils.setGraphs(GraphsPropertiesStub.stub());
        RdfUtils.setBauhausUriBuilder(new BauhausUriBuilder("http://id.insee.fr/", BASE_URI_GESTION, name -> switch (name) {
            case DOCUMENTS_BASE_URI -> Optional.of(DOCUMENTS_PATH);
            case LINKS_BASE_URI -> Optional.of(LINKS_PATH);
            default -> Optional.empty();
        }));

        BauhausUriProperties uris = new BauhausUriProperties(
                BASE_URI_GESTION, LINKS_PATH, "codes", DOCUMENTS_PATH, "produits/indicateur");
        OperationDocumentsQueries queries = new OperationDocumentsQueries(
                uris, new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());

        DocumentsUtils documentsUtils = new DocumentsUtils(
                repositoryGestion, mock(IdGenerator.class), mock(RepositoryPublication.class),
                new BauhausLanguagesProperties("fr", "en"), mock(PublicationUtils.class),
                mock(ParentUtils.class), new FileSystemOperation(),
                new StorageProperties(storageFolder.toString(), storageFolder.toString()),
                queries, new DocumentsStorageProperties(storageFolder.toString(), "http://bauhaus/"));
        documents = new DocumentsImpl(documentsUtils);
    }

    @Test
    @DisplayName("Un document créé est relu avec ses libellés, et son fichier est déposé dans le stockage")
    void shouldCreateReadAndDownloadADocument() throws Exception {
        String id = documents.createDocument("""
                {"labelLg1": "Note méthodologique", "labelLg2": "Methodological note", "descriptionLg1": "Une note"}""",
                content("contenu du pdf"), "note_" + unique() + ".pdf");

        JSONObject document = documents.getDocument(id);
        assertThat(document.getString(Constants.URI)).isEqualTo(BASE_URI_GESTION + DOCUMENTS_PATH + "/" + id);
        assertThat(document.getString(Constants.LABEL_LG1)).isEqualTo("Note méthodologique");
        assertThat(document.getString(Constants.LABEL_LG2)).isEqualTo("Methodological note");
        assertThat(document.getString(Constants.DESCRIPTION_LG1)).isEqualTo("Une note");

        Path storedFile = Path.of(document.getString(Constants.URL).replace("file://", ""));
        assertThat(Files.readString(storedFile)).isEqualTo("contenu du pdf");

        ResponseEntity<Resource> downloaded = documents.downloadDocument(id);
        assertThat(downloaded.getBody().getContentAsByteArray()).asString().isEqualTo("contenu du pdf");
    }

    @Test
    @DisplayName("Un document créé apparaît dans la liste de tous les documents")
    void shouldListTheCreatedDocument() throws Exception {
        String id = documents.createDocument("""
                {"labelLg1": "Document listé"}""", content("x"), "liste_" + unique() + ".pdf");

        assertThat(idsOf(new JSONArray(documents.getDocuments()))).contains(id);
    }

    @Test
    @DisplayName("Mettre à jour un document remplace ses libellés au lieu de les cumuler")
    void shouldReplaceLabelsOnUpdate() throws Exception {
        String id = documents.createDocument("""
                {"labelLg1": "Libellé initial"}""", content("x"), "maj_" + unique() + ".pdf");
        String url = documents.getDocument(id).getString(Constants.URL);

        // La mise à jour réécrit l'objet entier : le corps porte l'état complet du document,
        // url comprise, sans quoi le lien vers le fichier serait perdu.
        documents.setDocument(id, """
                {"labelLg1": "Libellé corrigé", "labelLg2": "Fixed label", "url": "%s"}""".formatted(url));

        JSONObject document = documents.getDocument(id);
        assertThat(document.getString(Constants.LABEL_LG1)).isEqualTo("Libellé corrigé");
        assertThat(document.getString(Constants.LABEL_LG2)).isEqualTo("Fixed label");
        assertThat(document.getString(Constants.URL)).isEqualTo(url);
    }

    @Test
    @DisplayName("Remplacer le fichier par un fichier d'un autre nom déplace le fichier et réécrit l'URL")
    void shouldReplaceTheAttachedFile() throws Exception {
        String suffix = unique();
        String id = documents.createDocument("""
                {"labelLg1": "Document à remplacer"}""", content("version 1"), "fichier_" + suffix + ".pdf");
        Path firstFile = Path.of(documents.getDocument(id).getString(Constants.URL).replace("file://", ""));

        documents.changeDocument(id, content("version 2"), "fichier_" + suffix + "_v2.pdf");

        Path secondFile = Path.of(documents.getDocument(id).getString(Constants.URL).replace("file://", ""));
        assertThat(secondFile.getFileName()).hasToString("fichier_" + suffix + "_v2.pdf");
        assertThat(Files.readString(secondFile)).isEqualTo("version 2");
        assertThat(firstFile).as("l'ancien fichier est retiré du stockage").doesNotExist();
    }

    @Test
    @DisplayName("Supprimer un document efface ses triplets et son fichier")
    void shouldDeleteADocumentAndItsFile() throws Exception {
        String id = documents.createDocument("""
                {"labelLg1": "Document à supprimer"}""", content("x"), "suppr_" + unique() + ".pdf");
        Path storedFile = Path.of(documents.getDocument(id).getString(Constants.URL).replace("file://", ""));

        assertThat(documents.deleteDocument(id)).isEqualTo(HttpStatus.OK);

        assertThat(storedFile).doesNotExist();
        assertThatThrownBy(() -> documents.getDocument(id)).isInstanceOf(RmesNotFoundException.class);
    }

    @Test
    @DisplayName("Un lien est créé, relu et supprimé sans jamais toucher au stockage de fichiers")
    void shouldHandleTheLinkLifecycleWithoutAnyFile() throws Exception {
        String url = "https://www.insee.fr/fr/statistiques/" + unique();
        String id = documents.setLink("""
                {"labelLg1": "Page Insee", "url": "%s"}""".formatted(url));

        JSONObject link = documents.getLink(id);
        assertThat(link.getString(Constants.URI)).isEqualTo(BASE_URI_GESTION + LINKS_PATH + "/" + id);
        assertThat(link.getString(Constants.URL)).isEqualTo(url);
        assertThat(link.getString(Constants.LABEL_LG1)).isEqualTo("Page Insee");

        documents.setLink(id, """
                {"labelLg1": "Page Insee mise à jour", "url": "%s"}""".formatted(url));
        assertThat(documents.getLink(id).getString(Constants.LABEL_LG1)).isEqualTo("Page Insee mise à jour");

        assertThat(documents.deleteLink(id)).isEqualTo(HttpStatus.OK);
        assertThatThrownBy(() -> documents.getLink(id)).isInstanceOf(RmesNotFoundException.class);
        assertThat(storageFolder).isEmptyDirectory();
    }

    @Test
    @DisplayName("Deux liens ne peuvent pas partager la même URL")
    void shouldRejectTwoLinksSharingTheSameUrl() throws Exception {
        String url = "https://www.insee.fr/fr/doublon/" + unique();
        documents.setLink("""
                {"labelLg1": "Premier lien %s", "url": "%s"}""".formatted(unique(), url));

        assertThatThrownBy(() -> documents.setLink("""
                {"labelLg1": "Second lien %s", "url": "%s"}""".formatted(unique(), url)))
                .isInstanceOf(RmesNotAcceptableException.class)
                .satisfies(thrown -> assertThat(((RmesException) thrown).getDetails())
                        .contains(String.valueOf(ErrorCodes.LINK_EXISTING_URL)));
    }

    @Test
    @DisplayName("Deux documents ne peuvent pas partager le même libellé lg1")
    void shouldRejectTwoDocumentsSharingTheSameLabel() throws Exception {
        String label = "Libellé unique " + unique();
        documents.createDocument("""
                {"labelLg1": "%s"}""".formatted(label), content("x"), "unicite_" + unique() + ".pdf");

        assertThatThrownBy(() -> documents.createDocument("""
                {"labelLg1": "%s"}""".formatted(label), content("y"), "unicite_" + unique() + ".pdf"))
                .isInstanceOf(RmesBadRequestException.class)
                .satisfies(thrown -> assertThat(((RmesException) thrown).getDetails())
                        .contains(ErrorCodes.OPERATION_DOCUMENT_LINK_EXISTING_LABEL_LG1));
    }

    @Test
    @DisplayName("Un document encore rattaché à une rubrique de SIMS n'est pas supprimable")
    void shouldRefuseToDeleteADocumentAttachedToASims() throws Exception {
        String id = documents.createDocument("""
                {"labelLg1": "Document rattaché %s"}""".formatted(unique()),
                content("x"), "rattache_" + unique() + ".pdf");
        String documentIri = BASE_URI_GESTION + DOCUMENTS_PATH + "/" + id;
        String simsGraph = "http://rdf.insee.fr/graphes/documents-crud-it/sims-" + id;
        attachToSims(simsGraph, documentIri);

        assertThatThrownBy(() -> documents.deleteDocument(id))
                .isInstanceOf(RmesBadRequestException.class)
                .satisfies(thrown -> assertThat(((RmesException) thrown).getDetails())
                        .contains(String.valueOf(ErrorCodes.DOCUMENT_DELETION_LINKED)));

        repositoryGestion.executeUpdate("CLEAR GRAPH <" + simsGraph + ">");
        assertThat(documents.deleteDocument(id))
                .as("une fois le rattachement retiré, la suppression est acceptée")
                .isEqualTo(HttpStatus.OK);
    }

    /** Rattache le document à une rubrique de SIMS sous la forme attendue : une rdf:List. */
    private void attachToSims(String graph, String documentIri) throws RmesException {
        repositoryGestion.executeUpdate("""
                INSERT DATA { GRAPH <%s> {
                    <%s/texte> <http://rdf.insee.fr/def/base#additionalMaterial> <%s/cellule> .
                    <%s/cellule> <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <%s> .
                    <%s/cellule> <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> <http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> .
                } }""".formatted(graph, graph, graph, graph, documentIri, graph));
    }

    private static List<String> idsOf(JSONArray documents) {
        return JSONUtils.stream(documents).map(doc -> doc.getString(Constants.ID)).toList();
    }

    /** Le conteneur est partagé : chaque test travaille sur des libellés, URLs et noms de fichiers qui lui sont propres. */
    private String unique() {
        return Long.toHexString(System.nanoTime());
    }

    private static InputStream content(String content) {
        return new ByteArrayInputStream(content.getBytes());
    }
}
