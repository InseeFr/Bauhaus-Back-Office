package fr.insee.rmes.bauhaus_services.operations.series;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import fr.insee.rmes.utils.JSONUtils;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeriesPublicationTest {

    @Mock
    ParentUtils parentUtils;

    @Mock
    PublicationUtils publicationUtils;

    @Mock
    RepositoryGestion repoGestion;

    @Mock
    RepositoryPublication repositoryPublication;

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

    private SeriesPublication seriesPublication;
    private JSONObject seriesJson;

    @BeforeEach
    void setUp() {
        seriesPublication = new SeriesPublication(parentUtils, publicationUtils, repoGestion, repositoryPublication);
        
        JSONObject familyJson = new JSONObject();
        familyJson.put(Constants.ID, "family123");

        seriesJson = new JSONObject();
        seriesJson.put(Constants.FAMILY, familyJson);
    }

    @Test
    void publishSeries_shouldThrowRmesBadRequestException_whenFamilyIsUnpublished() throws RmesException {
        String seriesId = "series123";
        String familyId = "family123";

        when(parentUtils.getValidationStatus(familyId)).thenReturn(ValidationStatus.UNPUBLISHED.getValue());

        try (MockedStatic<PublicationUtils> mockedPublicationUtils = mockStatic(PublicationUtils.class)) {
            mockedPublicationUtils.when(() -> PublicationUtils.isUnublished(ValidationStatus.UNPUBLISHED.getValue()))
                    .thenReturn(true);

            RmesBadRequestException exception = assertThrows(RmesBadRequestException.class,
                    () -> seriesPublication.publishSeries(seriesId, seriesJson));

            assertThat(exception.getDetails()).contains("Series: " + seriesId + " ; Family: " + familyId);
        }
    }

    @Test
    void publishSeries_shouldThrowRmesNotFoundException_whenSeriesDoesNotExist() throws RmesException {
        String seriesId = "series123";
        String familyId = "family123";

        when(parentUtils.getValidationStatus(familyId)).thenReturn(ValidationStatus.VALIDATED.getValue());
        when(repoGestion.getConnection()).thenReturn(repositoryConnection);

        try (MockedStatic<PublicationUtils> mockedPublicationUtils = mockStatic(PublicationUtils.class);
             MockedStatic<RdfUtils> mockedRdfUtils = mockStatic(RdfUtils.class)) {

            mockedPublicationUtils.when(() -> PublicationUtils.isUnublished(ValidationStatus.VALIDATED.getValue()))
                    .thenReturn(false);
            mockedRdfUtils.when(() -> RdfUtils.seriesIRI(seriesId)).thenReturn(resource);

            when(repoGestion.getStatements(repositoryConnection, resource)).thenReturn(statements);
            when(statements.hasNext()).thenReturn(false);

            RmesNotFoundException exception = assertThrows(RmesNotFoundException.class,
                    () -> seriesPublication.publishSeries(seriesId, seriesJson));

            assertThat(exception.getDetails()).contains(seriesId);
        }
    }

    @Test
    void publishSeries_shouldSuccessfullyPublish_whenConditionsAreMet() throws RmesException {
        String seriesId = "series123";
        String familyId = "family123";

        when(parentUtils.getValidationStatus(familyId)).thenReturn(ValidationStatus.VALIDATED.getValue());
        when(repoGestion.getConnection()).thenReturn(repositoryConnection);

        JSONArray operations = new JSONArray();
        JSONObject operation = new JSONObject();
        operation.put("operation", "http://example.org/operation1");
        operations.put(operation);

        try (MockedStatic<PublicationUtils> mockedPublicationUtils = mockStatic(PublicationUtils.class);
             MockedStatic<RdfUtils> mockedRdfUtils = mockStatic(RdfUtils.class);
             MockedStatic<OpSeriesQueries> mockedQueries = mockStatic(OpSeriesQueries.class);
             MockedStatic<JSONUtils> mockedJSONUtils = mockStatic(JSONUtils.class)) {

            mockedPublicationUtils.when(() -> PublicationUtils.isUnublished(ValidationStatus.VALIDATED.getValue()))
                    .thenReturn(false);
            mockedRdfUtils.when(() -> RdfUtils.seriesIRI(seriesId)).thenReturn(resource);
            mockedRdfUtils.when(RdfUtils::operationsGraph).thenReturn(resource);
            mockedRdfUtils.when(() -> RdfUtils.createIRI(anyString())).thenReturn(iri);
            mockedRdfUtils.when(() -> RdfUtils.toString(any())).thenReturn("http://example.org/predicate");
            
            mockedQueries.when(() -> OpSeriesQueries.getPublishedOperationsForSeries(anyString()))
                    .thenReturn("SELECT * WHERE { }");
            
            mockedJSONUtils.when(() -> JSONUtils.stream(any(JSONArray.class)))
                    .thenReturn(java.util.stream.Stream.empty());

            when(repoGestion.getStatements(repositoryConnection, resource)).thenReturn(statements);
            when(repoGestion.getHasPartStatements(repositoryConnection, resource)).thenReturn(hasPartStatements);
            when(repoGestion.getReplacesStatements(repositoryConnection, resource)).thenReturn(replacesStatements);
            when(repoGestion.getIsReplacedByStatements(repositoryConnection, resource)).thenReturn(isReplacedByStatements);
            when(repoGestion.getResponseAsArray(anyString())).thenReturn(operations);

            when(statements.hasNext()).thenReturn(true, false);

            when(publicationUtils.tranformBaseURIToPublish(any(Resource.class))).thenReturn(resource);

            seriesPublication.publishSeries(seriesId, seriesJson);

            verify(repositoryPublication).publishResource(eq(resource), any(Model.class), eq("serie"));
            verify(repoGestion).closeStatements(statements);
            verify(repoGestion).closeStatements(hasPartStatements);
            verify(repositoryConnection).close();
        }
    }

    @Test
    void addStatementsToModel_shouldAddAllStatements() {
        Model model = new LinkedHashModel();
        
        when(hasPartStatements.hasNext()).thenReturn(true, true, false);
        when(hasPartStatements.next()).thenReturn(statement, statement);
        when(statement.getSubject()).thenReturn(resource);
        when(statement.getPredicate()).thenReturn(iri);
        when(statement.getObject()).thenReturn(resource);
        when(statement.getContext()).thenReturn(resource);
        when(publicationUtils.tranformBaseURIToPublish(any(Resource.class))).thenReturn(resource);

        seriesPublication.addStatementsToModel(model, hasPartStatements);

        assertThat(model.size()).isEqualTo(1);
    }

    @Test
    void transformSubjectAndObject_shouldTransformBothSubjectAndObject() {
        Model model = new LinkedHashModel();
        
        when(statement.getSubject()).thenReturn(resource);
        when(statement.getPredicate()).thenReturn(iri);
        when(statement.getObject()).thenReturn(resource);
        when(statement.getContext()).thenReturn(resource);
        when(publicationUtils.tranformBaseURIToPublish(any(Resource.class))).thenReturn(resource);

        seriesPublication.transformSubjectAndObject(model, statement);

        verify(publicationUtils, times(2)).tranformBaseURIToPublish(eq(resource));
        assertThat(model.size()).isEqualTo(1);
    }

    @Test
    void publishSeries_shouldTestPrivateMethodIndirectly_checkIfSeriesExistThroughPublishSeries() throws RmesException {
        String seriesId = "series123";
        String familyId = "family123";

        when(parentUtils.getValidationStatus(familyId)).thenReturn(ValidationStatus.VALIDATED.getValue());
        when(repoGestion.getConnection()).thenReturn(repositoryConnection);

        try (MockedStatic<PublicationUtils> mockedPublicationUtils = mockStatic(PublicationUtils.class);
             MockedStatic<RdfUtils> mockedRdfUtils = mockStatic(RdfUtils.class)) {

            mockedPublicationUtils.when(() -> PublicationUtils.isUnublished(ValidationStatus.VALIDATED.getValue()))
                    .thenReturn(false);
            mockedRdfUtils.when(() -> RdfUtils.seriesIRI(seriesId)).thenReturn(resource);

            when(repoGestion.getStatements(repositoryConnection, resource)).thenReturn(statements);
            when(statements.hasNext()).thenReturn(false); // This will trigger the checkIfSeriesExist exception

            RmesNotFoundException exception = assertThrows(RmesNotFoundException.class,
                    () -> seriesPublication.publishSeries(seriesId, seriesJson));

            assertThat(exception.getDetails()).contains(seriesId);
        }
    }

    @Test
    void constructor_shouldCreateInstanceWithAllDependencies() {
        SeriesPublication publication = new SeriesPublication(parentUtils, publicationUtils, repoGestion, repositoryPublication);
        
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
        String seriesId = "series123";
        String familyId = "family123";

        when(parentUtils.getValidationStatus(familyId)).thenReturn(Constants.UNDEFINED);

        try (MockedStatic<PublicationUtils> mockedPublicationUtils = mockStatic(PublicationUtils.class)) {
            mockedPublicationUtils.when(() -> PublicationUtils.isUnublished(Constants.UNDEFINED))
                    .thenReturn(true);

            RmesBadRequestException exception = assertThrows(RmesBadRequestException.class,
                    () -> seriesPublication.publishSeries(seriesId, seriesJson));

            assertThat(exception.getDetails()).contains("Series: " + seriesId + " ; Family: " + familyId);
        }
    }

    @Test
    void publishSeries_shouldHandleModifiedStatus() throws RmesException {
        String seriesId = "series123";
        String familyId = "family123";

        when(parentUtils.getValidationStatus(familyId)).thenReturn(ValidationStatus.MODIFIED.getValue());

        try (MockedStatic<PublicationUtils> mockedPublicationUtils = mockStatic(PublicationUtils.class)) {
            mockedPublicationUtils.when(() -> PublicationUtils.isUnublished(ValidationStatus.MODIFIED.getValue()))
                    .thenReturn(true);

            RmesBadRequestException exception = assertThrows(RmesBadRequestException.class,
                    () -> seriesPublication.publishSeries(seriesId, seriesJson));

            assertThat(exception.getDetails()).contains("Series: " + seriesId + " ; Family: " + familyId);
        }
    }

    @Test
    void publishSeries_shouldHandlePredicatesCorrectly() throws RmesException {
        String seriesId = "series123";
        String familyId = "family123";

        when(parentUtils.getValidationStatus(familyId)).thenReturn(ValidationStatus.VALIDATED.getValue());
        when(repoGestion.getConnection()).thenReturn(repositoryConnection);

        try (MockedStatic<PublicationUtils> mockedPublicationUtils = mockStatic(PublicationUtils.class);
             MockedStatic<RdfUtils> mockedRdfUtils = mockStatic(RdfUtils.class);
             MockedStatic<OpSeriesQueries> mockedQueries = mockStatic(OpSeriesQueries.class);
             MockedStatic<JSONUtils> mockedJSONUtils = mockStatic(JSONUtils.class)) {

            mockedPublicationUtils.when(() -> PublicationUtils.isUnublished(ValidationStatus.VALIDATED.getValue()))
                    .thenReturn(false);
            mockedRdfUtils.when(() -> RdfUtils.seriesIRI(seriesId)).thenReturn(resource);
            mockedRdfUtils.when(() -> RdfUtils.toString(any())).thenReturn("http://example.org/isPartOf");

            mockedQueries.when(() -> OpSeriesQueries.getPublishedOperationsForSeries(anyString()))
                    .thenReturn("SELECT * WHERE { }");

            mockedJSONUtils.when(() -> JSONUtils.stream(any(JSONArray.class)))
                    .thenReturn(java.util.stream.Stream.empty());

            when(repoGestion.getStatements(repositoryConnection, resource)).thenReturn(statements);
            when(repoGestion.getHasPartStatements(repositoryConnection, resource)).thenReturn(hasPartStatements);
            when(repoGestion.getReplacesStatements(repositoryConnection, resource)).thenReturn(replacesStatements);
            when(repoGestion.getIsReplacedByStatements(repositoryConnection, resource)).thenReturn(isReplacedByStatements);
            when(repoGestion.getResponseAsArray(anyString())).thenReturn(new JSONArray());

            when(statements.hasNext()).thenReturn(true, false);

            when(publicationUtils.tranformBaseURIToPublish(any(Resource.class))).thenReturn(resource);

            seriesPublication.publishSeries(seriesId, seriesJson);

            verify(publicationUtils, times(1)).tranformBaseURIToPublish(any(Resource.class));
        }
    }
}