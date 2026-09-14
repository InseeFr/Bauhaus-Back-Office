package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.DocumentsStorageProperties;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.configuration.StorageProperties;
import fr.insee.rmes.modules.commons.domain.model.Document;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.eclipse.rdf4j.common.iteration.CloseableIteratorIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentsPublicationTest {

    private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();
    private static final String GESTION = "http://gestion/";
    private static final String PUBLICATION = "http://publication/";

    @Mock
    private DocumentsUtils docUtils;

    @Mock
    private fr.insee.rmes.modules.commons.domain.port.serverside.FilesOperations filesOperations;

    @Mock
    private RepositoryGestion repoGestion;

    @Mock
    private RepositoryPublication repositoryPublication;

    @Spy
    private PublicationUtils publicationUtils = new PublicationUtils(GESTION, PUBLICATION, null, null);

    @Spy
    private StorageProperties storageProperties = new StorageProperties("/gestion", "/publication");

    @Spy
    private DocumentsStorageProperties documentsStorage = new DocumentsStorageProperties("/gestion", "http://web4g");

    @InjectMocks
    private DocumentsPublication documentsPublication;

    @BeforeEach
    void initStaticUris() {
        RdfUtils.setGraphs(GraphsPropertiesStub.stub());
        RdfUtils.setBauhausUriBuilder(new BauhausUriBuilder(PUBLICATION, GESTION, name -> Optional.of("documents")));
    }

    @Test
    void shouldCollectIdsOfDocumentsMissingFromStorageWithoutCopyingAnything() throws RmesException {
        JSONArray documents = new JSONArray()
                .put(new JSONObject().put("id", "1").put("url", "file:///gestion/doc1.pdf"))
                .put(new JSONObject().put("id", "2").put("url", "file:///gestion/doc2.pdf"))
                .put(new JSONObject().put("id", "3").put("url", "file:///gestion/doc3.pdf"));
        when(docUtils.getListDocumentSims("sims1")).thenReturn(documents);
        when(docUtils.existsInStorage("doc1.pdf")).thenReturn(false);
        when(docUtils.existsInStorage("doc2.pdf")).thenReturn(true);
        when(docUtils.existsInStorage("doc3.pdf")).thenReturn(false);

        Set<String> missing = documentsPublication.findMissingDocuments("sims1");

        assertThat(missing).containsExactlyInAnyOrder("1", "3");
        verify(filesOperations, never()).copy(any(Document.class), any(Document.class));
    }

    @Test
    void shouldReturnEmptySetWhenEveryDocumentExistsInStorage() throws RmesException {
        JSONArray documents = new JSONArray()
                .put(new JSONObject().put("id", "1").put("url", "file:///gestion/doc1.pdf"))
                .put(new JSONObject().put("id", "2").put("url", "file:///gestion/doc2.pdf"));
        when(docUtils.getListDocumentSims("sims1")).thenReturn(documents);
        when(docUtils.existsInStorage("doc1.pdf")).thenReturn(true);
        when(docUtils.existsInStorage("doc2.pdf")).thenReturn(true);

        assertThat(documentsPublication.findMissingDocuments("sims1")).isEmpty();
    }

    /**
     * Publier un SIMS copie le fichier de chaque document vers le stockage de publication et
     * réécrit son URL : celle de la base de gestion ne serait pas joignable depuis la diffusion.
     */
    @Test
    void shouldCopyTheFileAndRewriteTheUrlOfEachPublishedDocument() throws RmesException {
        when(docUtils.getListDocumentSims("sims1"))
                .thenReturn(
                        new JSONArray().put(new JSONObject().put("id", "1").put("url", "file:///gestion/doc1.pdf")));
        when(docUtils.getIdFromJson(any(JSONObject.class))).thenReturn(1);
        when(docUtils.getListLinksSims("sims1")).thenReturn(new JSONArray());
        IRI document = (IRI) RdfUtils.documentIRI("1");
        when(repoGestion.getConnection()).thenReturn(null);
        when(repoGestion.getStatements(any(), eq(document)))
                .thenReturn(statements(
                        VF.createStatement(
                                document,
                                VF.createIRI(GESTION + "def/url"),
                                VF.createLiteral("file:///gestion/doc1.pdf")),
                        VF.createStatement(document, VF.createIRI(GESTION + "def/label"), VF.createLiteral("doc 1"))));

        documentsPublication.publishAllDocumentsInSims("sims1");

        verify(filesOperations).copy(new Document("/gestion", "doc1.pdf"), new Document("/publication", "doc1.pdf"));
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(repositoryPublication).publishResource(any(Resource.class), modelCaptor.capture(), anyString());
        Model model = modelCaptor.getValue();
        assertThat(model.stream().map(statement -> statement.getObject().stringValue()))
                .contains("http://web4g/doc1.pdf", "doc 1");
    }

    @Test
    void shouldPublishTheLinksOfTheSimsUnderTheirPublicationUri() throws RmesException {
        when(docUtils.getListDocumentSims("sims1")).thenReturn(new JSONArray());
        when(docUtils.getListLinksSims("sims1"))
                .thenReturn(new JSONArray().put(new JSONObject().put("id", "2").put("url", "http://lien")));
        when(docUtils.getIdFromJson(any(JSONObject.class))).thenReturn(2);
        IRI link = (IRI) RdfUtils.linkIRI("2");
        when(repoGestion.getConnection()).thenReturn(null);
        when(repoGestion.getStatements(any(), eq(link)))
                .thenReturn(statements(
                        VF.createStatement(link, VF.createIRI(GESTION + "def/label"), VF.createLiteral("lien"))));

        documentsPublication.publishAllDocumentsInSims("sims1");

        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(repositoryPublication).publishResource(any(Resource.class), modelCaptor.capture(), anyString());
        assertThat(modelCaptor.getValue().stream()
                        .map(statement -> statement.getSubject().stringValue()))
                .allMatch(subject -> subject.startsWith(PUBLICATION));
    }

    private static RepositoryResult<Statement> statements(Statement... statements) {
        return new RepositoryResult<>(
                new CloseableIteratorIteration<>(List.of(statements).iterator()));
    }
}
