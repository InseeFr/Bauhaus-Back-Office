package fr.insee.rmes.bauhaus_services.datasets;

import fr.insee.rmes.bauhaus_services.distribution.DistributionQueries;
import fr.insee.rmes.bauhaus_services.operations.series.SeriesUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "fr.insee.rmes.bauhaus.baseGraph=http://",
        "fr.insee.rmes.bauhaus.sesame.gestion.baseURI=http://",
        "fr.insee.rmes.bauhaus.datasets.graph=datasetGraph/",
        "fr.insee.rmes.bauhaus.datasets.baseURI=datasetIRI",
        "fr.insee.rmes.bauhaus.distribution.baseURI=distributionIRI",
        "fr.insee.rmes.bauhaus.lg1=fr",
        "fr.insee.rmes.bauhaus.lg2=en"
})
public class DatasetServiceImplTest {

    @MockBean
    SeriesUtils seriesUtils;

    @MockBean
    RepositoryGestion repositoryGestion;

    @Autowired
    DatasetServiceImpl datasetService;

    @Test
    void shouldReturnDatasets() throws RmesException {
        JSONArray array = new JSONArray();
        array.put("result");

        when(repositoryGestion.getResponseAsArray("query")).thenReturn(array);
        try (MockedStatic<DatasetQueries> mockedFactory = Mockito.mockStatic(DatasetQueries.class)) {
            mockedFactory.when(() -> DatasetQueries.getDatasets(any())).thenReturn("query");
            String query = datasetService.getDatasets();
            Assertions.assertEquals(query, "[\"result\"]");
        }
    }

    @Test
    void shouldReturnDataset() throws RmesException, JSONException {
        JSONObject object = new JSONObject();
        object.put("id", "1");
        JSONArray array = new JSONArray().put(object);
        when(repositoryGestion.getResponseAsArray("query")).thenReturn(array);
        try (MockedStatic<DatasetQueries> mockedFactory = Mockito.mockStatic(DatasetQueries.class)) {
            mockedFactory.when(() -> DatasetQueries.getDataset(eq("1"), any())).thenReturn("query");
            String query = datasetService.getDatasetByID("1");
            Assertions.assertEquals(query, "{\"themes\":[],\"id\":\"1\"}");
        }
    }

    @Test
    void shouldReturnDistributions() throws RmesException, JSONException {
        JSONArray array = new JSONArray();
        array.put("1");

        when(repositoryGestion.getResponseAsArray("query")).thenReturn(array);
        try (MockedStatic<DistributionQueries> mockedFactory = Mockito.mockStatic(DistributionQueries.class)) {
            mockedFactory.when(() -> DistributionQueries.getDatasetDistributions(eq("1"), any())).thenReturn("query");
            String query = datasetService.getDistributions("1");
            Assertions.assertEquals(query, "[\"1\"]");
        }
    }

    @Test
    void shouldReturnAnErrorIfLabelLg1NotDefinedWhenCreating() throws RmesException, JSONException {
        try (MockedStatic<DatasetQueries> mockedFactory = Mockito.mockStatic(DatasetQueries.class)) {
            mockedFactory.when(() -> DatasetQueries.lastDatasetId(any())).thenReturn("query");
            JSONObject body = new JSONObject();


            when(repositoryGestion.getResponseAsObject(anyString())).then(invocationOnMock -> {
                JSONObject lastId = new JSONObject();
                lastId.put("id", "1000");
                return lastId;
            });
            RmesException exception = assertThrows(RmesBadRequestException.class, () -> datasetService.create(body.toString()));
            Assertions.assertEquals(exception.getDetails(), "{\"message\":\"The property labelLg1 is required\"}");
        }
    }

    @Test
    void shouldReturnAnErrorIfLabelLg2NotDefinedWhenCreating() throws RmesException {
        try (MockedStatic<DatasetQueries> mockedFactory = Mockito.mockStatic(DatasetQueries.class)) {
            mockedFactory.when(() -> DatasetQueries.lastDatasetId(any())).thenReturn("query");
            JSONObject body = new JSONObject();
            body.put("labelLg1", "labelLg1");


            when(repositoryGestion.getResponseAsObject(anyString())).then(invocationOnMock -> {
                JSONObject lastId = new JSONObject();
                lastId.put("id", "1000");
                return lastId;
            });
            RmesException exception = assertThrows(RmesBadRequestException.class, () -> datasetService.create(body.toString()));
            Assertions.assertEquals(exception.getDetails(), "{\"message\":\"The property labelLg2 is required\"}");
        }
    }

    @Test
    void shouldReturnAnErrorIfCreatorNotDefinedWhenCreating() throws RmesException {
        try (MockedStatic<DatasetQueries> mockedFactory = Mockito.mockStatic(DatasetQueries.class)) {
            mockedFactory.when(() -> DatasetQueries.lastDatasetId(any())).thenReturn("query");
            JSONObject body = new JSONObject();
            body.put("labelLg1", "labelLg1");
            body.put("labelLg2", "labelLg2R");


            when(repositoryGestion.getResponseAsObject(anyString())).then(invocationOnMock -> {
                JSONObject lastId = new JSONObject();
                lastId.put("id", "1000");
                return lastId;
            });
            RmesException exception = assertThrows(RmesBadRequestException.class, () -> datasetService.create(body.toString()));
            Assertions.assertEquals(exception.getDetails(), "{\"message\":\"The property creator is required\"}");
        }
    }

    @Test
    void shouldReturnAnErrorIfContributorNotDefinedWhenCreating() throws RmesException {
        try (MockedStatic<DatasetQueries> mockedFactory = Mockito.mockStatic(DatasetQueries.class)) {
            mockedFactory.when(() -> DatasetQueries.lastDatasetId(any())).thenReturn("query");
            JSONObject body = new JSONObject();
            body.put("labelLg1", "labelLg1");
            body.put("labelLg2", "labelLg2R");
            body.put("creator", "creator");


            when(repositoryGestion.getResponseAsObject(anyString())).then(invocationOnMock -> {
                JSONObject lastId = new JSONObject();
                lastId.put("id", "1000");
                return lastId;
            });
            RmesException exception = assertThrows(RmesBadRequestException.class, () -> datasetService.create(body.toString()));
            Assertions.assertEquals(exception.getDetails(), "{\"message\":\"The property contributor is required\"}");
        }
    }

    @Test
    void shouldReturnAnErrorIfDisseminationStatusNotDefinedWhenCreating() throws RmesException {
        try (MockedStatic<DatasetQueries> mockedFactory = Mockito.mockStatic(DatasetQueries.class)) {
            mockedFactory.when(() -> DatasetQueries.lastDatasetId(any())).thenReturn("query");
            JSONObject body = new JSONObject();
            body.put("labelLg1", "labelLg1");
            body.put("labelLg2", "labelLg2R");
            body.put("creator", "creator");
            body.put("contributor", "contributor");


            when(repositoryGestion.getResponseAsObject(anyString())).then(invocationOnMock -> {
                JSONObject lastId = new JSONObject();
                lastId.put("id", "1000");
                return lastId;
            });
            RmesException exception = assertThrows(RmesBadRequestException.class, () -> datasetService.create(body.toString()));
            Assertions.assertEquals(exception.getDetails(), "{\"message\":\"The property disseminationStatus is required\"}");
        }
    }

    @Test
    void shouldReturnAnErrorIfUnknownSeriesNotDefinedWhenCreating() throws RmesException {
        try (MockedStatic<DatasetQueries> mockedFactory = Mockito.mockStatic(DatasetQueries.class)) {
            mockedFactory.when(() -> DatasetQueries.lastDatasetId(any())).thenReturn("query");
            JSONObject body = new JSONObject();
            body.put("labelLg1", "labelLg1");
            body.put("labelLg2", "labelLg2R");
            body.put("creator", "creator");
            body.put("contributor", "contributor");
            body.put("disseminationStatus", "disseminationStatus");

            when(seriesUtils.isSeriesExist(any())).thenReturn(false);

            when(repositoryGestion.getResponseAsObject(anyString())).then(invocationOnMock -> {
                JSONObject lastId = new JSONObject();
                lastId.put("id", "1000");
                return lastId;
            });
            RmesException exception = assertThrows(RmesBadRequestException.class, () -> datasetService.create(body.toString()));
            Assertions.assertEquals(exception.getDetails(), "{\"message\":\"The series does not exist\"}");
        }
    }

    @Test
    void shouldPersistNewDatasetWithAndIncrementedId() throws RmesException {
        when(repositoryGestion.getResponseAsObject(anyString())).then(invocationOnMock -> {
            JSONObject lastId = new JSONObject();
            lastId.put("id", "1000");
            return lastId;
        });
        createANewDataset("jd1001");
    }

    @Test
    void shouldPersistNewDatasetWithTheDefaultId() throws RmesException {
        when(repositoryGestion.getResponseAsObject(anyString())).then(invocationOnMock -> {
            JSONObject lastId = new JSONObject();
            return lastId;
        });
        createANewDataset("jd1000");
    }

    @Test
    void shouldPersistNewDatasetWithTheDefaultIdIfUndefined() throws RmesException {
        when(repositoryGestion.getResponseAsObject(anyString())).then(invocationOnMock -> {
            JSONObject lastId = new JSONObject();
            lastId.put("id", "undefined");
            return lastId;
        });
        createANewDataset("jd1000");
    }

    private void createANewDataset(String nextId) throws RmesException {
        IRI iri = SimpleValueFactory.getInstance().createIRI("http://datasetIRI/" + nextId);
        try (
                MockedStatic<DatasetQueries> datasetQueriesMock = Mockito.mockStatic(DatasetQueries.class);
                MockedStatic<RdfUtils> rdfUtilsMock = Mockito.mockStatic(RdfUtils.class);
                MockedStatic<DateUtils> dateUtilsMock = Mockito.mockStatic(DateUtils.class)
        ) {

            rdfUtilsMock.when(() -> RdfUtils.createIRI(any())).thenCallRealMethod();

            datasetQueriesMock.when(() -> DatasetQueries.lastDatasetId(any())).thenReturn("query");
            dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn("2023-10-19T11:44:23.335590");
            dateUtilsMock.when(() -> DateUtils.parseDateTime(anyString())).thenReturn(LocalDateTime.parse("2023-10-19T11:44:23.335590"));
            rdfUtilsMock.when(() -> RdfUtils.seriesIRI("2")).thenReturn(SimpleValueFactory.getInstance().createIRI("http://seriesIRI/2"));
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(anyString(), anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralDateTime(any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleString(any(), any(), any(), any(), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleUri(any(IRI.class), any(), any(IRI.class), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleDateTime(any(), any(), any(), any(), any())).thenCallRealMethod();

            JSONObject body = new JSONObject();
            body.put("labelLg1", "labelLg1");
            body.put("labelLg2", "labelLg2");
            body.put("descriptionLg1", "descriptionLg1");
            body.put("descriptionLg2", "descriptionLg2");
            body.put("creator", "creator");
            body.put("contributor", "contributor");
            body.put("disseminationStatus", "disseminationStatus");
            body.put("idSerie", "2");
            body.put("themes", new JSONArray().put("theme"));

            JSONArray distributions = new JSONArray();
            when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(distributions);

            when(seriesUtils.isSeriesExist(anyString())).thenReturn(true);


            String id = datasetService.create(body.toString());

            ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);
            verify(repositoryGestion, times(1)).loadSimpleObject(eq(iri), model.capture(), any());

            Assertions.assertEquals("[(http://datasetIRI/jd1001, http://purl.org/dc/terms/identifier, \"jd1001\", http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.w3.org/ns/dcat#Dataset, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/title, \"labelLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/title, \"labelLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/creator, \"creator\", http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/contributor, \"contributor\", http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/description, \"descriptionLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/description, \"descriptionLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/created, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/modified, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/]]".replaceAll("jd1001", nextId), model.getValue().toString());
            Assertions.assertEquals(id, nextId);
        }
    }

    @Test
    void shouldPersistExistingDataset() throws RmesException {
        IRI iri = SimpleValueFactory.getInstance().createIRI("http://datasetIRI/jd1001");
        IRI distributionIRI = SimpleValueFactory.getInstance().createIRI("http://distributionIRI/jd1001");
        try (
                MockedStatic<DatasetQueries> datasetQueriesMock = Mockito.mockStatic(DatasetQueries.class);
                MockedStatic<RdfUtils> rdfUtilsMock = Mockito.mockStatic(RdfUtils.class);
                MockedStatic<DateUtils> dateUtilsMock = Mockito.mockStatic(DateUtils.class)
        ) {
            rdfUtilsMock.when(() -> RdfUtils.createIRI(any())).thenCallRealMethod();

            datasetQueriesMock.when(() -> DatasetQueries.lastDatasetId(any())).thenReturn("query");
            dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn("2023-10-19T11:44:23.335590");
            dateUtilsMock.when(() -> DateUtils.parseDateTime(eq("2023-10-19T11:44:23.335590"))).thenReturn(LocalDateTime.parse("2023-10-19T11:44:23.335590"));
            dateUtilsMock.when(() -> DateUtils.parseDateTime(eq("2022-10-19T11:44:23.335590"))).thenReturn(LocalDateTime.parse("2022-10-19T11:44:23.335590"));
            rdfUtilsMock.when(() -> RdfUtils.seriesIRI("2")).thenReturn(SimpleValueFactory.getInstance().createIRI("http://seriesIRI/2"));
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(anyString(), anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralDateTime(any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleString(any(), any(), any(), any(), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleUri(any(IRI.class), any(), any(IRI.class), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleDateTime(any(), any(), any(), any(), any())).thenCallRealMethod();

            JSONObject body = new JSONObject();
            body.put("labelLg1", "labelLg1");
            body.put("labelLg2", "labelLg2");
            body.put("created", "2022-10-19T11:44:23.335590");
            body.put("descriptionLg1", "descriptionLg1");
            body.put("descriptionLg2", "descriptionLg2");
            body.put("creator", "creator");
            body.put("contributor", "contributor");
            body.put("disseminationStatus", "disseminationStatus");
            body.put("idSerie", "2");
            body.put("themes", new JSONArray().put("theme"));

            JSONArray distributions = new JSONArray();
            JSONObject d = new JSONObject();
            d.put("id", "d1000");
            distributions.put(d);
            when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(distributions);

            when(seriesUtils.isSeriesExist(anyString())).thenReturn(true);

            String id = datasetService.update("jd1001", body.toString());

            ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);
            verify(repositoryGestion, times(1)).loadSimpleObject(eq(iri), model.capture(), any());

            Assertions.assertEquals("[(http://datasetIRI/jd1001, http://purl.org/dc/terms/identifier, \"jd1001\", http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.w3.org/ns/dcat#Dataset, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/title, \"labelLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/title, \"labelLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/creator, \"creator\", http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/contributor, \"contributor\", http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/description, \"descriptionLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/description, \"descriptionLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/created, \"2022-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/modified, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/dcat#Distribution, http://distributionIRI/d1000, http://datasetGraph/) [http://datasetGraph/]]", model.getValue().toString());
            Assertions.assertEquals(id, "jd1001");
        }
    }

    @Test
    void shouldThrowAnExceptionIfTheBodyIsNotAJSONDuringCreation(){
        Assertions.assertThrows(RmesException.class, () -> datasetService.create(""));
    }

    @Test
    void shouldThrowAnExceptionIfTheBodyIsNotAJSONDuringUpdate(){
        Assertions.assertThrows(RmesException.class, () -> datasetService.update("d1000", ""));
    }
}
