package fr.insee.rmes.bauhaus_services.distribution;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.utils.DateUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class DistributionServiceImplTest {
    @Mock
    Config config;

    @Mock
    RepositoryGestion repositoryGestion;

    @InjectMocks
    DistributionServiceImpl distributionService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnDistributions() throws RmesException {
        JSONArray array = new JSONArray();
        array.put("result");

        when(repositoryGestion.getResponseAsArray("query")).thenReturn(array);
        try (MockedStatic<DistributionQueries> mockedFactory = Mockito.mockStatic(DistributionQueries.class)) {
            mockedFactory.when(DistributionQueries::getDistributions).thenReturn("query");
            String query = distributionService.getDistributions();
            Assertions.assertEquals(query, "[\"result\"]");
        }
    }

    @Test
    void shouldReturnDataset() throws RmesException, JSONException {
        JSONObject object = new JSONObject();
        object.put("id", "1");

        when(repositoryGestion.getResponseAsObject("query")).thenReturn(object);
        try (MockedStatic<DistributionQueries> mockedFactory = Mockito.mockStatic(DistributionQueries.class)) {
            mockedFactory.when(() -> DistributionQueries.getDistribution("1")).thenReturn("query");
            String query = distributionService.getDistributionByID("1");
            Assertions.assertEquals(query, "{\"id\":\"1\"}");
        }
    }

    @Test
    void shouldReturnAnErrorIfIdDatasetNotDefinedWhenCreating() throws RmesException, JSONException {
        try (MockedStatic<DistributionQueries> mockedFactory = Mockito.mockStatic(DistributionQueries.class)) {
            mockedFactory.when(() -> DistributionQueries.lastDatasetId()).thenReturn("query");
            JSONObject body = new JSONObject();


            when(repositoryGestion.getResponseAsObject(anyString())).then(invocationOnMock -> {
                JSONObject lastId = new JSONObject();
                lastId.put("id", "1000");
                return lastId;
            });
            RmesException exception = assertThrows(RmesBadRequestException.class, () -> distributionService.create(body.toString()));
            Assertions.assertEquals(exception.getDetails(), "{\"message\":\"The property idDataset is required\"}");
        }
    }

    @Test
    void shouldReturnAnErrorIfLabelLg1NotDefinedWhenCreating() throws RmesException, JSONException {
        try (MockedStatic<DistributionQueries> mockedFactory = Mockito.mockStatic(DistributionQueries.class)) {
            mockedFactory.when(() -> DistributionQueries.lastDatasetId()).thenReturn("query");
            JSONObject body = new JSONObject();
            body.put("idDataset", "idDataset");

            when(repositoryGestion.getResponseAsObject(anyString())).then(invocationOnMock -> {
                JSONObject lastId = new JSONObject();
                lastId.put("id", "1000");
                return lastId;
            });
            RmesException exception = assertThrows(RmesBadRequestException.class, () -> distributionService.create(body.toString()));
            Assertions.assertEquals(exception.getDetails(), "{\"message\":\"The property labelLg1 is required\"}");
        }
    }

    @Test
    void shouldReturnAnErrorIfLabelLg2NotDefinedWhenCreating() throws RmesException, JSONException {
        try (MockedStatic<DistributionQueries> mockedFactory = Mockito.mockStatic(DistributionQueries.class)) {
            mockedFactory.when(() -> DistributionQueries.lastDatasetId()).thenReturn("query");
            JSONObject body = new JSONObject();
            body.put("idDataset", "idDataset");
            body.put("labelLg1", "labelLg1");

            when(repositoryGestion.getResponseAsObject(anyString())).then(invocationOnMock -> {
                JSONObject lastId = new JSONObject();
                lastId.put("id", "1000");
                return lastId;
            });
            RmesException exception = assertThrows(RmesBadRequestException.class, () -> distributionService.create(body.toString()));
            Assertions.assertEquals(exception.getDetails(), "{\"message\":\"The property labelLg2 is required\"}");
        }
    }

    @Test
    void shouldPersistNewDistributionWithAndIncrementedId() throws RmesException {
        when(repositoryGestion.getResponseAsObject(anyString())).then(invocationOnMock -> {
            JSONObject lastId = new JSONObject();
            lastId.put("id", "1000");
            return lastId;
        });
        createANewDistribution("d1001");
    }

    @Test
    void shouldPersistNewDistributionWithTheDefaultId() throws RmesException {
        when(repositoryGestion.getResponseAsObject(anyString())).then(invocationOnMock -> {
            JSONObject lastId = new JSONObject();
            return lastId;
        });
        createANewDistribution("d1000");
    }

    @Test
    void shouldPersistNewDistributionWithTheDefaultIdIfUndefined() throws RmesException {
        when(repositoryGestion.getResponseAsObject(anyString())).then(invocationOnMock -> {
            JSONObject lastId = new JSONObject();
            lastId.put("id", "undefined");
            return lastId;
        });
        createANewDistribution("d1000");
    }

    private void createANewDistribution(String nextId) throws RmesException {
        try (
                MockedStatic<DistributionQueries> datasetQueriesMock = Mockito.mockStatic(DistributionQueries.class);
                MockedStatic<RdfUtils> rdfUtilsMock = Mockito.mockStatic(RdfUtils.class);
                MockedStatic<DateUtils> dateUtilsMock = Mockito.mockStatic(DateUtils.class)
        ) {
            IRI iri = SimpleValueFactory.getInstance().createIRI("http://distributionIRI/" + nextId);

            datasetQueriesMock.when(DistributionQueries::lastDatasetId).thenReturn("query");
            datasetQueriesMock.when(() -> DistributionQueries.getDistribution(nextId)).thenReturn("query " + nextId);
            when(repositoryGestion.getResponseAsObject(eq("query " + nextId))).thenReturn(new JSONObject());

            dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn("2023-10-19T11:44:23.335590");
            dateUtilsMock.when(() -> DateUtils.parseDateTime(anyString())).thenReturn(LocalDateTime.parse("2023-10-19T11:44:23.335590"));
            rdfUtilsMock.when(() -> RdfUtils.seriesIRI("2")).thenReturn(SimpleValueFactory.getInstance().createIRI("http://seriesIRI/2"));
            rdfUtilsMock.when(() -> RdfUtils.distributionIRI(nextId)).thenReturn(iri);
            rdfUtilsMock.when(RdfUtils::datasetGraph).thenReturn(SimpleValueFactory.getInstance().createIRI("http://datasetGraph/"));
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(anyString(), anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralDateTime(any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleString(any(), any(), any(), any(), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleString(any(), any(), any(), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleUri(any(IRI.class), any(), any(IRI.class), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleDateTime(any(), any(), any(), any(), any())).thenCallRealMethod();

            JSONObject body = new JSONObject();
            body.put("idDataset", "idDataset");
            body.put("labelLg1", "labelLg1");
            body.put("labelLg2", "labelLg2");
            body.put("descriptionLg1", "descriptionLg1");
            body.put("descriptionLg2", "descriptionLg2");
            body.put("format", "format");
            body.put("taille", "taille");
            body.put("url", "url");

            when(config.getBaseUriGestion()).thenReturn("base-uri-gestion");
            when(config.getOpSeriesBaseUri()).thenReturn("base-op-series-uri");
            when(config.getDatasetsGraph()).thenReturn("dataset-graph");
            when(config.getLg1()).thenReturn("fr");
            when(config.getLg2()).thenReturn("en");




            String id = distributionService.create(body.toString());

            ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);
            verify(repositoryGestion, times(1)).loadSimpleObject(eq(iri), model.capture(), any());

            Assertions.assertEquals("[(http://distributionIRI/d1001, http://purl.org/dc/terms/identifier, \"d1001\", http://datasetGraph/) [http://datasetGraph/], (http://distributionIRI/d1001, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.w3.org/ns/dcat#Distribution, http://datasetGraph/) [http://datasetGraph/], (http://distributionIRI/d1001, http://purl.org/dc/terms/title, \"labelLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://distributionIRI/d1001, http://purl.org/dc/terms/title, \"labelLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://distributionIRI/d1001, http://purl.org/dc/terms/description, \"descriptionLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://distributionIRI/d1001, http://purl.org/dc/terms/description, \"descriptionLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://distributionIRI/d1001, http://purl.org/dc/terms/created, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/], (http://distributionIRI/d1001, http://purl.org/dc/terms/modified, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/], (http://distributionIRI/d1001, http://purl.org/dc/terms/format, \"format\", http://datasetGraph/) [http://datasetGraph/], (http://distributionIRI/d1001, http://www.w3.org/ns/dcat#byteSize, \"taille\", http://datasetGraph/) [http://datasetGraph/], (http://distributionIRI/d1001, http://www.w3.org/ns/dcat#downloadURL, \"url\", http://datasetGraph/) [http://datasetGraph/]]".replaceAll("d1001", nextId), model.getValue().toString());
            Assertions.assertEquals(id, nextId);
        }
    }

    @Test
    void shouldPersistExistingDistributionWithPreviousDatasetRemoval() throws RmesException {
        shouldUpdateExistingDistribution(true);
    }
    @Test
    void shouldPersistExistingDistributionWithoutPreviousDatasetRemoval() throws RmesException {
        shouldUpdateExistingDistribution(false);
    }

    private void shouldUpdateExistingDistribution(boolean withDataSetRemoval) throws RmesException {
        IRI iri = SimpleValueFactory.getInstance().createIRI("http://distributionIRI/d1001");
        try (
                MockedStatic<DistributionQueries> datasetQueriesMock = Mockito.mockStatic(DistributionQueries.class);
                MockedStatic<RdfUtils> rdfUtilsMock = Mockito.mockStatic(RdfUtils.class);
                MockedStatic<DateUtils> dateUtilsMock = Mockito.mockStatic(DateUtils.class)
        ) {
            datasetQueriesMock.when(DistributionQueries::lastDatasetId).thenReturn("query");
            datasetQueriesMock.when(() -> DistributionQueries.getDistribution("d1001")).thenReturn("query d1001");

            if(!withDataSetRemoval){
                when(repositoryGestion.getResponseAsObject(eq("query d1001"))).thenReturn(new JSONObject());
            } else {
                JSONObject distribution = new JSONObject();
                distribution.put("idDataset", "idDataset2");
                when(repositoryGestion.getResponseAsObject(eq("query d1001"))).thenReturn(distribution);
            }


            dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn("2023-10-19T11:44:23.335590");
            dateUtilsMock.when(() -> DateUtils.parseDateTime(eq("2022-10-19T11:44:23.335590"))).thenReturn(LocalDateTime.parse("2022-10-19T11:44:23.335590"));
            dateUtilsMock.when(() -> DateUtils.parseDateTime(eq("2023-10-19T11:44:23.335590"))).thenReturn(LocalDateTime.parse("2023-10-19T11:44:23.335590"));
            rdfUtilsMock.when(() -> RdfUtils.seriesIRI("2")).thenReturn(SimpleValueFactory.getInstance().createIRI("http://seriesIRI/2"));
            rdfUtilsMock.when(() -> RdfUtils.distributionIRI("d1001")).thenReturn(iri);
            rdfUtilsMock.when(RdfUtils::datasetGraph).thenReturn(SimpleValueFactory.getInstance().createIRI("http://datasetGraph/"));
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(anyString(), anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralDateTime(any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleString(any(), any(), any(), any(), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleString(any(), any(), any(), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleUri(any(IRI.class), any(), any(IRI.class), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleDateTime(any(), any(), any(), any(), any())).thenCallRealMethod();

            JSONObject body = new JSONObject();
            body.put("idDataset", "idDataset");
            body.put("labelLg1", "labelLg1");
            body.put("labelLg2", "labelLg2");
            body.put("descriptionLg1", "descriptionLg1");
            body.put("descriptionLg2", "descriptionLg2");
            body.put("format", "format");
            body.put("taille", "taille");
            body.put("url", "url");
            body.put("created", "2022-10-19T11:44:23.335590");

            when(config.getBaseUriGestion()).thenReturn("base-uri-gestion");
            when(config.getOpSeriesBaseUri()).thenReturn("base-op-series-uri");
            when(config.getDatasetsGraph()).thenReturn("dataset-graph");
            when(config.getLg1()).thenReturn("fr");
            when(config.getLg2()).thenReturn("en");


            String id = distributionService.update("d1001", body.toString());

            ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);
            verify(repositoryGestion, times(1)).loadSimpleObject(eq(iri), model.capture(), any());

            if(withDataSetRemoval){
                verify(repositoryGestion, times(1)).deleteTripletByPredicateAndValue(any(), any(), any(), any(), any());
            }
            Assertions.assertEquals("[(http://distributionIRI/d1001, http://purl.org/dc/terms/identifier, \"d1001\", http://datasetGraph/) [http://datasetGraph/], (http://distributionIRI/d1001, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.w3.org/ns/dcat#Distribution, http://datasetGraph/) [http://datasetGraph/], (http://distributionIRI/d1001, http://purl.org/dc/terms/title, \"labelLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://distributionIRI/d1001, http://purl.org/dc/terms/title, \"labelLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://distributionIRI/d1001, http://purl.org/dc/terms/description, \"descriptionLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://distributionIRI/d1001, http://purl.org/dc/terms/description, \"descriptionLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://distributionIRI/d1001, http://purl.org/dc/terms/created, \"2022-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/], (http://distributionIRI/d1001, http://purl.org/dc/terms/modified, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/], (http://distributionIRI/d1001, http://purl.org/dc/terms/format, \"format\", http://datasetGraph/) [http://datasetGraph/], (http://distributionIRI/d1001, http://www.w3.org/ns/dcat#byteSize, \"taille\", http://datasetGraph/) [http://datasetGraph/], (http://distributionIRI/d1001, http://www.w3.org/ns/dcat#downloadURL, \"url\", http://datasetGraph/) [http://datasetGraph/]]", model.getValue().toString());
            Assertions.assertEquals(id, "d1001");
        }
    }

    @Test
    void shouldThrowAnExceptionIfTheBodyIsNotAJSONDuringCreation(){
        Assertions.assertThrows(RmesException.class, () -> distributionService.create(""));
    }

    @Test
    void shouldThrowAnExceptionIfTheBodyIsNotAJSONDuringUpdate(){
        Assertions.assertThrows(RmesException.class, () -> distributionService.update("d1000", ""));
    }
}
