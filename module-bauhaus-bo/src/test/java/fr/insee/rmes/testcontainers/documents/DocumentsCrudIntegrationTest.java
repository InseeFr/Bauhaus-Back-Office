package fr.insee.rmes.testcontainers.documents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.BauhausUriProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.modules.commons.infrastructure.filessystem.FileSystemOperation;
import fr.insee.rmes.modules.operations.documents.domain.DomainDocumentManagementService;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.DocumentNotFoundException;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.DocumentRuleViolationException;
import fr.insee.rmes.modules.operations.documents.domain.exceptions.DocumentRuleViolationException.Violation;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentForm;
import fr.insee.rmes.modules.operations.documents.domain.model.DocumentKind;
import fr.insee.rmes.modules.operations.documents.domain.model.FileSize;
import fr.insee.rmes.modules.operations.documents.domain.model.ManagedDocument;
import fr.insee.rmes.modules.operations.documents.domain.model.StoredFile;
import fr.insee.rmes.modules.operations.documents.domain.model.UploadedFile;
import fr.insee.rmes.modules.operations.documents.infrastructure.graphdb.GraphDBManagedDocumentRepository;
import fr.insee.rmes.modules.operations.documents.infrastructure.storage.FilesOperationsDocumentFileStorage;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;

/**
 * Cycle de vie complet d'un document et d'un lien contre un vrai GraphDB et un vrai stockage de
 * fichiers, à travers le service de domaine et ses vrais adaptateurs : tout ce qui est écrit est
 * relu, la taille du fichier comprise.
 *
 * <p>Le conteneur GraphDB est partagé entre les classes de test : les assertions portent donc sur
 * les objets créés par ce test, jamais sur des dénombrements globaux.
 */
@Tag("integration")
class DocumentsCrudIntegrationTest extends WithGraphDBContainer {

    private static final String BASE_URI_GESTION = "http://bauhaus/";
    private static final String DOCUMENTS_PATH = "documents/document";
    private static final String LINKS_PATH = "documents/page";

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    @TempDir
    Path storageFolder;

    private DomainDocumentManagementService documents;

    @BeforeEach
    void setUp() {
        GraphDBManagedDocumentRepository repository = new GraphDBManagedDocumentRepository(
                repositoryGestion,
                GraphsPropertiesStub.stub(),
                new BauhausUriProperties(BASE_URI_GESTION, LINKS_PATH, "codes", DOCUMENTS_PATH, "produits/indicateur"),
                new BauhausLanguagesProperties("fr", "en"));
        FilesOperationsDocumentFileStorage storage = new FilesOperationsDocumentFileStorage(
                new FileSystemOperation(), storageFolder.toString(), storageFolder.toString());
        documents = new DomainDocumentManagementService(repository, storage, _ -> List.of(), Set.of("pdf", "odt"));
    }

    private static DocumentForm labelled(String labelLg1) {
        return new DocumentForm(labelLg1, null, null, null, null, null, null);
    }

    private static UploadedFile upload(String name, String content) {
        return new UploadedFile(name, new ByteArrayInputStream(content.getBytes()), content.length());
    }

    private ManagedDocument read(DocumentKind kind, String id) throws Exception {
        return documents.get(kind, id).document();
    }

    private static void assertViolation(Executable call, Violation violation) {
        assertThatThrownBy(call::execute)
                .isInstanceOfSatisfying(
                        DocumentRuleViolationException.class,
                        e -> assertThat(e.violation()).isEqualTo(violation));
    }

    @Test
    @DisplayName("Un document créé est relu avec ses libellés et sa taille, et son fichier est déposé dans le stockage")
    void shouldCreateReadAndDownloadADocument() throws Exception {
        String fileName = "note_" + unique() + ".pdf";
        String id = documents.createDocument(
                new DocumentForm(
                        "Note méthodologique " + unique(),
                        "Methodological note " + unique(),
                        "Une note",
                        null,
                        "2026-09-24",
                        "fr",
                        null),
                upload(fileName, "contenu du pdf"));

        ManagedDocument document = read(DocumentKind.DOCUMENT, id);
        assertThat(document.uri()).isEqualTo(BASE_URI_GESTION + DOCUMENTS_PATH + "/" + id);
        assertThat(document.form().descriptionLg1()).isEqualTo("Une note");
        assertThat(document.form().updatedDate()).isEqualTo("2026-09-24");
        assertThat(document.size()).isEqualTo(new FileSize(14));
        assertThat(document.form().url()).isEqualTo("file://" + storageFolder.resolve(fileName));
        assertThat(storageFolder.resolve(fileName)).hasContent("contenu du pdf");

        StoredFile file = documents.download(id);
        try (InputStream content = file.content()) {
            assertThat(content).hasContent("contenu du pdf");
        }
    }

    @Test
    @DisplayName("Un document créé apparaît dans la liste de tous les documents")
    void shouldListTheCreatedDocument() throws Exception {
        String id = documents.createDocument(
                labelled("Document listé " + unique()), upload("liste_" + unique() + ".pdf", "x"));

        assertThat(documents.getAll()).extracting(ManagedDocument::id).contains(id);
    }

    @Test
    @DisplayName("Mettre à jour un document remplace ses libellés et garde son fichier et sa taille")
    void shouldReplaceLabelsOnUpdateAndKeepTheFile() throws Exception {
        String id = documents.createDocument(
                new DocumentForm("Libellé initial " + unique(), "Initial " + unique(), null, null, null, null, null),
                upload("maj_" + unique() + ".pdf", "12345"));
        ManagedDocument before = read(DocumentKind.DOCUMENT, id);

        documents.update(DocumentKind.DOCUMENT, id, labelled("Libellé corrigé " + id));

        ManagedDocument after = read(DocumentKind.DOCUMENT, id);
        assertThat(after.form().labelLg1()).isEqualTo("Libellé corrigé " + id);
        assertThat(after.form().labelLg2())
                .as("l'ancien libellé n'est pas cumulé")
                .isNull();
        assertThat(after.form().url()).isEqualTo(before.form().url());
        assertThat(after.size()).isEqualTo(new FileSize(5));
    }

    @Test
    @DisplayName("Remplacer le fichier par un fichier d'un autre nom le déplace, réécrit l'URL et la taille")
    void shouldReplaceTheAttachedFile() throws Exception {
        String suffix = unique();
        String id = documents.createDocument(
                labelled("Document à remplacer " + suffix), upload("fichier_" + suffix + ".pdf", "version 1"));

        assertThat(documents.replaceFile(id, upload("fichier_" + suffix + "_v2.pdf", "version deux")))
                .contains("file://" + storageFolder.resolve("fichier_" + suffix + "_v2.pdf"));

        ManagedDocument document = read(DocumentKind.DOCUMENT, id);
        assertThat(document.size()).isEqualTo(new FileSize(12));
        assertThat(storageFolder.resolve("fichier_" + suffix + "_v2.pdf")).hasContent("version deux");
        assertThat(storageFolder.resolve("fichier_" + suffix + ".pdf"))
                .as("l'ancien fichier est retiré du stockage")
                .doesNotExist();
    }

    @Test
    @DisplayName("Supprimer un document efface ses triplets, sa taille comprise, et son fichier")
    void shouldDeleteADocumentAndItsFile() throws Exception {
        String fileName = "suppr_" + unique() + ".pdf";
        String id = documents.createDocument(labelled("Document à supprimer " + unique()), upload(fileName, "x"));
        String iri = BASE_URI_GESTION + DOCUMENTS_PATH + "/" + id;

        documents.delete(DocumentKind.DOCUMENT, id);

        assertThat(storageFolder.resolve(fileName)).doesNotExist();
        assertThatThrownBy(() -> documents.get(DocumentKind.DOCUMENT, id))
                .isInstanceOf(DocumentNotFoundException.class);
        assertThat(repositoryGestion.getResponseAsBoolean("ASK { GRAPH ?g { <" + iri + "> ?p ?o } }"))
                .as("aucun triplet orphelin, dcterms:extent compris")
                .isFalse();
    }

    @Test
    @DisplayName("Un lien est créé, relu et supprimé sans jamais toucher au stockage de fichiers")
    void shouldHandleTheLinkLifecycleWithoutAnyFile() throws Exception {
        String url = "https://www.insee.fr/fr/statistiques/" + unique();
        String id = documents.createLink(new DocumentForm("Page Insee " + unique(), null, null, null, null, null, url));

        ManagedDocument link = read(DocumentKind.LINK, id);
        assertThat(link.uri()).isEqualTo(BASE_URI_GESTION + LINKS_PATH + "/" + id);
        assertThat(link.form().url()).isEqualTo(url);
        assertThat(link.size()).isNull();

        documents.update(
                DocumentKind.LINK, id, new DocumentForm("Page mise à jour " + id, null, null, null, null, null, url));
        assertThat(read(DocumentKind.LINK, id).form().labelLg1()).isEqualTo("Page mise à jour " + id);

        documents.delete(DocumentKind.LINK, id);
        assertThatThrownBy(() -> documents.get(DocumentKind.LINK, id)).isInstanceOf(DocumentNotFoundException.class);
        assertThat(storageFolder).isEmptyDirectory();
    }

    @Test
    @DisplayName("Deux liens ne peuvent pas partager la même URL")
    void shouldRejectTwoLinksSharingTheSameUrl() throws Exception {
        String url = "https://www.insee.fr/fr/doublon/" + unique();
        documents.createLink(new DocumentForm("Premier lien " + unique(), null, null, null, null, null, url));

        assertViolation(
                () -> documents.createLink(
                        new DocumentForm("Second lien " + unique(), null, null, null, null, null, url)),
                Violation.LINK_URL_ALREADY_USED);
    }

    @Test
    @DisplayName("Deux documents ne peuvent pas partager le même libellé lg1")
    void shouldRejectTwoDocumentsSharingTheSameLabel() throws Exception {
        String label = "Libellé unique " + unique();
        documents.createDocument(labelled(label), upload("unicite_" + unique() + ".pdf", "x"));

        assertViolation(
                () -> documents.createDocument(labelled(label), upload("unicite_" + unique() + ".pdf", "y")),
                Violation.LABEL_LG1_ALREADY_USED);
    }

    @Test
    @DisplayName("Un document encore rattaché à une rubrique de SIMS n'est pas supprimable")
    void shouldRefuseToDeleteADocumentAttachedToASims() throws Exception {
        String id = documents.createDocument(
                labelled("Document rattaché " + unique()), upload("rattache_" + unique() + ".pdf", "x"));
        String documentIri = BASE_URI_GESTION + DOCUMENTS_PATH + "/" + id;
        String simsGraph = "http://rdf.insee.fr/graphes/documents-crud-it/sims-" + id;
        attachToSims(simsGraph, documentIri);

        assertViolation(() -> documents.delete(DocumentKind.DOCUMENT, id), Violation.REFERENCED_BY_SIMS);

        repositoryGestion.executeUpdate("CLEAR GRAPH <" + simsGraph + ">");
        documents.delete(DocumentKind.DOCUMENT, id);
        assertThatThrownBy(() -> documents.get(DocumentKind.DOCUMENT, id))
                .as("une fois le rattachement retiré, la suppression est acceptée")
                .isInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    @DisplayName("Un fichier de même nom qu'un autre document est refusé au lieu de l'écraser")
    void shouldRefuseAFileNameAlreadyUsedByAnotherDocument() throws Exception {
        String fileName = "partage_" + unique() + ".pdf";
        documents.createDocument(labelled("Premier " + unique()), upload(fileName, "premier"));

        assertViolation(
                () -> documents.createDocument(labelled("Second " + unique()), upload(fileName, "second")),
                Violation.FILE_ALREADY_EXISTS);
        assertThat(storageFolder.resolve(fileName)).hasContent("premier");
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

    private static String unique() {
        return Long.toHexString(System.nanoTime());
    }
}
