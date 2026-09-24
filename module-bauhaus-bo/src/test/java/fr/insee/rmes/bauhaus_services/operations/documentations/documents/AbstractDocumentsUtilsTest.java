package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import static fr.insee.rmes.PropertiesKeys.DOCUMENTS_BASE_URI;
import static fr.insee.rmes.PropertiesKeys.LINKS_BASE_URI;
import static org.mockito.Mockito.when;

import fr.insee.rmes.DocumentsStorageProperties;
import fr.insee.rmes.bauhaus_services.operations.OperationsParentRepository;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.configuration.StorageProperties;
import fr.insee.rmes.modules.commons.domain.port.serverside.FilesOperations;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationDocumentsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.IdGenerator;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import org.mockito.Mock;

/** Socle des tests de {@link DocumentsUtils} : ses collaborateurs simulés et les fabriques communes. */
abstract class AbstractDocumentsUtilsTest {

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

    DocumentsUtils documentsUtils;

    /** Documents sous http://bauhaus/documents/document, liens sous http://bauhaus/documents/page. */
    static void useDocumentAndLinkBaseUris() {
        RdfUtils.setGraphs(GraphsPropertiesStub.stub());
        RdfUtils.setBauhausUriBuilder(
                new BauhausUriBuilder("http://id.insee.fr/", "http://bauhaus/", name -> switch (name) {
                    case DOCUMENTS_BASE_URI -> Optional.of("documents/document");
                    case LINKS_BASE_URI -> Optional.of("documents/page");
                    default -> Optional.empty();
                }));
    }

    DocumentsUtils newDocumentsUtils() {
        return new DocumentsUtils(
                repoGestion,
                idGenerator,
                repositoryPublication,
                publicationUtils,
                filesOperations,
                storageProperties,
                operationDocumentsQueries);
    }

    /** Le stockage gestion des documents est {@code storageFolder}, et ce dossier existe. */
    void givenExistingStorageFolder(Path storageFolder) {
        when(documentsStorage.storageGestion()).thenReturn(storageFolder.toString());
        when(storageProperties.directoryGestion()).thenReturn(storageFolder.toString());
        when(filesOperations.exists(storageFolder.toString())).thenReturn(true);
    }

    static String detailsOf(Throwable thrown) {
        return ((RmesException) thrown).getDetails();
    }

    static InputStream content(String content) {
        return new ByteArrayInputStream(content.getBytes());
    }
}
