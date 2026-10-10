package fr.insee.rmes.bauhaus_services.operations.series;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.operations.OperationsParentRepository;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeriesPublicationTest {

    @Mock
    OperationsParentRepository operationsParentRepository;

    @Mock
    PublicationUtils publicationUtils;

    @Mock
    RepositoryGestion repoGestion;

    @Mock
    RepositoryPublication repositoryPublication;

    @Mock
    OperationSeriesQueries operationSeriesQueries;

    @Mock
    RepositoryConnection repositoryConnection;

    @Mock
    RepositoryResult<Statement> statements;

    @Mock
    RepositoryResult<Statement> hasPartStatements;

    @Mock
    RepositoryResult<Statement> replacesStatements;

    @Mock
    RepositoryResult<Statement> isReplacedByStatements;

    @Mock
    Statement statement;

    @Mock
    Resource resource;

    @Mock
    IRI iri;

    private static final String SERIES_ID = "series123";
    private static final String FAMILY_ID = "family123";

    private SeriesPublication seriesPublication;
    private JSONObject seriesJson;

    @BeforeEach
    void setUp() {
        seriesPublication = new SeriesPublication(
                operationsParentRepository,
                publicationUtils,
                repoGestion,
                repositoryPublication,
                operationSeriesQueries);

        JSONObject familyJson = new JSONObject();
        familyJson.put(Constants.ID, "family123");

        seriesJson = new JSONObject();
        seriesJson.put(Constants.FAMILY, familyJson);
    }

    @Test
    void publishSeries_shouldThrowRmesBadRequestException_whenSeriesIsAlreadyPublished() throws RmesException {
        String seriesId = "series123";

        when(operationsParentRepository.getFamOpSerValidationStatus(seriesId))
                .thenReturn(ValidationStatus.VALIDATED.getValue());

        RmesBadRequestException exception = assertThrows(
                RmesBadRequestException.class, () -> seriesPublication.publishSeries(seriesId, seriesJson));

        assertThat(exception.getDetails()).contains("\"code\":1301");
        assertThat(exception.getDetails()).contains("Series: " + seriesId);
        verify(repoGestion, never()).getConnection();
    }

    @Test
    void publishSeries_shouldNotThrow_whenSeriesHasBeenModifiedSinceItsPublication() throws RmesException {
        String seriesId = "series123";

        when(operationsParentRepository.getFamOpSerValidationStatus(seriesId))
                .thenReturn(ValidationStatus.MODIFIED.getValue());
        when(operationsParentRepository.getValidationStatus("family123"))
                .thenReturn(ValidationStatus.UNPUBLISHED.getValue());

        RmesBadRequestException exception = assertThrows(
                RmesBadRequestException.class, () -> seriesPublication.publishSeries(seriesId, seriesJson));

        assertThat(exception.getDetails()).contains("cannot be published before its family is published");
    }

    @Test
    void publishSeries_shouldThrowRmesBadRequestException_whenFamilyIsUnpublished() throws RmesException {
        assertPublicationRejectedUntilTheFamilyIsPublished(ValidationStatus.UNPUBLISHED.getValue());
    }

    @Test
    void publishSeries_shouldThrowRmesNotFoundException_whenSeriesDoesNotExist() throws RmesException {
        givenValidatedFamily();

        try (MockedStatic<PublicationUtils> mockedPublicationUtils = mockStatic(PublicationUtils.class);
                MockedStatic<RdfUtils> mockedRdfUtils = mockStatic(RdfUtils.class)) {

            givenPublishableSeries(mockedPublicationUtils, mockedRdfUtils);

            when(repoGestion.getStatements(repositoryConnection, resource)).thenReturn(statements);
            when(statements.hasNext()).thenReturn(false); // This will trigger the checkIfSeriesExist exception

            RmesNotFoundException exception = assertThrows(
                    RmesNotFoundException.class, () -> seriesPublication.publishSeries(SERIES_ID, seriesJson));

            assertThat(exception.getDetails()).contains(SERIES_ID);
        }
    }

    @Test
    void publishSeries_shouldSuccessfullyPublish_whenConditionsAreMet() throws RmesException {
        JSONArray operations = new JSONArray();
        JSONObject operation = new JSONObject();
        operation.put("operation", "http://example.org/operation1");
        operations.put(operation);

        publishExistingSeries(operations, mockedRdfUtils -> {
            mockedRdfUtils.when(RdfUtils::operationsGraph).thenReturn(resource);
            mockedRdfUtils.when(() -> RdfUtils.createIRI(anyString())).thenReturn(iri);
            mockedRdfUtils.when(() -> RdfUtils.toString(any())).thenReturn("http://example.org/predicate");
        });

        verify(repositoryPublication).publishResource(eq(resource), any(Model.class), eq("serie"));
        verify(statements).close();
        verify(hasPartStatements).close();
        verify(replacesStatements).close();
        verify(isReplacedByStatements).close();
        verify(repositoryConnection).close();
    }

    @Test
    void addStatementsToModel_shouldAddAllStatements() {
        Model model = new LinkedHashModel();

        when(hasPartStatements.hasNext()).thenReturn(true, true, false);
        when(hasPartStatements.next()).thenReturn(statement, statement);
        givenStatementLinkingResources();

        seriesPublication.addStatementsToModel(model, hasPartStatements);

        assertThat(model.size()).isEqualTo(1);
    }

    @Test
    void transformSubjectAndObject_shouldTransformBothSubjectAndObject() {
        Model model = new LinkedHashModel();

        givenStatementLinkingResources();

        seriesPublication.transformSubjectAndObject(model, statement);

        verify(publicationUtils, times(2)).tranformBaseURIToPublish(resource);
        assertThat(model.size()).isEqualTo(1);
    }

    @Test
    void constructor_shouldCreateInstanceWithAllDependencies() {
        SeriesPublication publication = new SeriesPublication(
                operationsParentRepository,
                publicationUtils,
                repoGestion,
                repositoryPublication,
                operationSeriesQueries);

        assertThat(publication).isNotNull();
    }

    @Test
    void publishSeries_shouldHandleNullFamily() {
        String seriesId = "series123";
        JSONObject emptySeries = new JSONObject();

        assertThrows(Exception.class, () -> seriesPublication.publishSeries(seriesId, emptySeries));
    }

    @Test
    void publishSeries_shouldValidateStatusCorrectly() throws RmesException {
        assertPublicationRejectedUntilTheFamilyIsPublished(Constants.UNDEFINED);
    }

    @Test
    void publishSeries_shouldHandleModifiedStatus() throws RmesException {
        assertPublicationRejectedUntilTheFamilyIsPublished(ValidationStatus.MODIFIED.getValue());
    }

    @Test
    void publishSeries_shouldHandlePredicatesCorrectly() throws RmesException {
        publishExistingSeries(
                new JSONArray(),
                mockedRdfUtils ->
                        mockedRdfUtils.when(() -> RdfUtils.toString(any())).thenReturn("http://example.org/isPartOf"));

        verify(publicationUtils, times(1)).tranformBaseURIToPublish(any(Resource.class));
    }

    /**
     * La famille porte un statut que {@link PublicationUtils#isUnublished} juge non publié : la
     * série est refusée, et le message nomme la série et sa famille.
     */
    private void assertPublicationRejectedUntilTheFamilyIsPublished(String familyStatus) throws RmesException {
        when(operationsParentRepository.getValidationStatus(FAMILY_ID)).thenReturn(familyStatus);

        try (MockedStatic<PublicationUtils> mockedPublicationUtils = mockStatic(PublicationUtils.class)) {
            mockedPublicationUtils
                    .when(() -> PublicationUtils.isUnublished(familyStatus))
                    .thenReturn(true);

            RmesBadRequestException exception = assertThrows(
                    RmesBadRequestException.class, () -> seriesPublication.publishSeries(SERIES_ID, seriesJson));

            assertThat(exception.getDetails()).contains("Series: " + SERIES_ID + " ; Family: " + FAMILY_ID);
        }
    }

    /**
     * Publie une série existante d'une famille validée, dont les opérations publiées sont
     * {@code operations} ; {@code rdfUtilsStubs} complète le comportement de {@link RdfUtils}.
     */
    private void publishExistingSeries(JSONArray operations, Consumer<MockedStatic<RdfUtils>> rdfUtilsStubs)
            throws RmesException {
        givenValidatedFamily();

        try (MockedStatic<PublicationUtils> mockedPublicationUtils = mockStatic(PublicationUtils.class);
                MockedStatic<RdfUtils> mockedRdfUtils = mockStatic(RdfUtils.class);
                MockedStatic<JSONUtils> mockedJSONUtils = mockStatic(JSONUtils.class)) {

            givenPublishableSeries(mockedPublicationUtils, mockedRdfUtils);
            rdfUtilsStubs.accept(mockedRdfUtils);
            givenStatementsOfAnExistingSeries(mockedJSONUtils, operations);

            seriesPublication.publishSeries(SERIES_ID, seriesJson);
        }
    }

    private void givenValidatedFamily() throws RmesException {
        when(operationsParentRepository.getValidationStatus(FAMILY_ID))
                .thenReturn(ValidationStatus.VALIDATED.getValue());
        when(repoGestion.getConnection()).thenReturn(repositoryConnection);
    }

    private void givenPublishableSeries(
            MockedStatic<PublicationUtils> mockedPublicationUtils, MockedStatic<RdfUtils> mockedRdfUtils) {
        mockedPublicationUtils
                .when(() -> PublicationUtils.isUnublished(ValidationStatus.VALIDATED.getValue()))
                .thenReturn(false);
        mockedRdfUtils.when(() -> RdfUtils.seriesIRI(SERIES_ID)).thenReturn(resource);
    }

    /** La série existe (un triplet), et ses liens hasPart / replaces / isReplacedBy sont lus. */
    private void givenStatementsOfAnExistingSeries(MockedStatic<JSONUtils> mockedJSONUtils, JSONArray operations)
            throws RmesException {
        when(operationSeriesQueries.getPublishedOperationsForSeries(anyString()))
                .thenReturn("SELECT * WHERE { }");

        mockedJSONUtils.when(() -> JSONUtils.stream(any(JSONArray.class))).thenReturn(Stream.empty());

        when(repoGestion.getStatements(repositoryConnection, resource)).thenReturn(statements);
        when(repoGestion.getHasPartStatements(repositoryConnection, resource)).thenReturn(hasPartStatements);
        when(repoGestion.getReplacesStatements(repositoryConnection, resource)).thenReturn(replacesStatements);
        when(repoGestion.getIsReplacedByStatements(repositoryConnection, resource))
                .thenReturn(isReplacedByStatements);
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(operations);

        when(statements.hasNext()).thenReturn(true, false);

        when(publicationUtils.tranformBaseURIToPublish(any(Resource.class))).thenReturn(resource);
    }

    private void givenStatementLinkingResources() {
        when(statement.getSubject()).thenReturn(resource);
        when(statement.getPredicate()).thenReturn(iri);
        when(statement.getObject()).thenReturn(resource);
        when(statement.getContext()).thenReturn(resource);
        when(publicationUtils.tranformBaseURIToPublish(any(Resource.class))).thenReturn(resource);
    }
}
