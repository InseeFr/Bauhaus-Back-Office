package fr.insee.rmes.bauhaus_services.datasets;

import fr.insee.rmes.bauhaus_services.distribution.DistributionQueries;
import fr.insee.rmes.bauhaus_services.operations.series.SeriesUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.dataset.Dataset;
import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.Deserializer;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "fr.insee.rmes.bauhaus.baseGraph=http://",
        "fr.insee.rmes.bauhaus.sesame.gestion.baseURI=http://",
        "fr.insee.rmes.bauhaus.datasets.graph=datasetGraph/",
        "fr.insee.rmes.bauhaus.datasets.baseURI=datasetIRI",
        "fr.insee.rmes.bauhaus.datasets.record.baseURI=recordIRI",
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
            mockedFactory.when(() -> DatasetQueries.getDatasets(anyString(), eq(null))).thenReturn("query");
            String query = datasetService.getDatasets();
            Assertions.assertEquals(query, "[\"result\"]");
        }
    }

    @Test
    void getDatasetsForDistributionCreationWithStamp() throws RmesException {
        JSONArray array = new JSONArray();
        array.put("result");

        when(repositoryGestion.getResponseAsArray("query")).thenReturn(array);
        try (MockedStatic<DatasetQueries> mockedFactory = Mockito.mockStatic(DatasetQueries.class)) {
            mockedFactory.when(() -> DatasetQueries.getDatasets(anyString(), eq("fakeStampForDvAndQf"))).thenReturn("query");
            String query = datasetService.getDatasetsForDistributionCreation("fakeStampForDvAndQf");
            Assertions.assertEquals(query, "[\"result\"]");
        }
    }

    @Test
    void shouldReturnDataset() throws RmesException, JSONException {
        JSONObject object = new JSONObject();
        object.put("id", "1");
        JSONArray array = new JSONArray().put(object);
        when(repositoryGestion.getResponseAsArray("query")).thenReturn(array);
        when(repositoryGestion.getResponseAsArray("query-creators")).thenReturn(new JSONArray().put(new JSONObject().put("creator", "creator-1")));
        when(repositoryGestion.getResponseAsArray("query-spacialResolutions")).thenReturn(new JSONArray().put(new JSONObject().put("spacialResolution", "spacialResolutions-1")));
        when(repositoryGestion.getResponseAsArray("query-statisticalUnits")).thenReturn(new JSONArray().put(new JSONObject().put("statisticalUnit", "statisticalUnit-1")));
        try (MockedStatic<DatasetQueries> mockedFactory = Mockito.mockStatic(DatasetQueries.class)) {
            mockedFactory.when(() -> DatasetQueries.getDataset(eq("1"), any())).thenReturn("query");
            mockedFactory.when(() -> DatasetQueries.getDatasetCreators(eq("1"), any())).thenReturn("query-creators");
            mockedFactory.when(() -> DatasetQueries.getDatasetSpacialResolutions(eq("1"), any())).thenReturn("query-spacialResolutions");
            mockedFactory.when(() -> DatasetQueries.getDatasetStatisticalUnits(eq("1"), any())).thenReturn("query-statisticalUnits");
            String query = datasetService.getDatasetByID("1");
            Assertions.assertEquals(query, "{\"themes\":[],\"catalogRecord\":{},\"creators\":[\"creator-1\"],\"statisticalUnit\":[\"statisticalUnit-1\"],\"id\":\"1\",\"spacialResolutions\":[\"spacialResolutions-1\"]}");
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

            JSONObject record = new JSONObject();
            record.put("creator", "creator");
            body.put("catalogRecord", record);



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

            body.put("catalogRecord", this.generateCatalogRecord());

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
            body.put("disseminationStatus", "disseminationStatus");
            body.put("catalogRecord", this.generateCatalogRecord());

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

    @Test
    void shouldUpdatedatasetUpdated() throws RmesException {
        IRI iri = SimpleValueFactory.getInstance().createIRI("http://datasetIRI/jd1001");
        IRI catalogRecordIri = SimpleValueFactory.getInstance().createIRI("http://recordIRI/jd1001");
        try (
                MockedStatic<DatasetQueries> datasetQueriesMock = Mockito.mockStatic(DatasetQueries.class);
                MockedStatic<RdfUtils> rdfUtilsMock = Mockito.mockStatic(RdfUtils.class);
                MockedStatic<DateUtils> dateUtilsMock = Mockito.mockStatic(DateUtils.class)
        ) {
            rdfUtilsMock.when(() -> RdfUtils.createIRI(any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.toURI(any())).thenCallRealMethod();
            dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn("2024-01-19T11:44:23.335590");

            JSONObject jsonDataset = new JSONObject();
            jsonDataset.put("descriptionLg1", "descriptionLg1");
            jsonDataset.put("descriptionLg2", "descriptionLg2");
            jsonDataset.put("observationNumber", 5);

            JSONObject record = new JSONObject();
            record.put("creator", "creator");
            record.put("contributor", "contributor");
            record.put("created", "2023-10-19T11:44:23.335590");
            record.put("updated", "2023-12-19T11:44:23.335590");
            jsonDataset.put("catalogRecord", record);
            jsonDataset.put("id", "d1000");
            Dataset dataset = Deserializer.deserializeBody(String.valueOf(jsonDataset), Dataset.class);
            dataset.getCatalogRecord().setUpdated(DateUtils.getCurrentDate());
            Assertions.assertEquals("2024-01-19T11:44:23.335590", dataset.getCatalogRecord().getUpdated());
        }
    }

    private void generateGeneralInformation(JSONObject body){
        body.put("labelLg1", "labelLg1");
        body.put("labelLg2", "labelLg2");
        body.put("subTitleLg1", "subTitleLg1");
        body.put("subTitleLg2", "subTitleLg2");
        body.put("accrualPeriodicity", "https://accrualPeriodicity");
        body.put("accessRights", "https://accessRights");
        body.put("confidentialityStatus", "https://confidentialityStatus");
        body.put("creators", List.of("c1", "c2"));
        body.put("publisher", "c3");
        body.put("landingPageLg1", "landingPageLg1");
        body.put("landingPageLg2", "landingPageLg2");
        body.put("updated", "2023-10-19T11:44:23.335590");
        body.put("issued", "2023-10-19T11:44:23.335590");
    }
    private void generateGeneralManagment(JSONObject body){
        body.put("disseminationStatus", "https://disseminationStatus");
        body.put("processStep", "https://disseminationStatus");
        body.put("archiveUnit", "https://archiveUnit");
    }

    private void generateStatisticsINformations(JSONObject body){
        body.put("type", "http://type");
        body.put("statisticalUnit", List.of("https://statisticalUnit"));
        body.put("dataStructure", "https://dataStructure");
        body.put("observationNumber", 2);
        body.put("spacialCoverage", "https://spacialCoverage");
        body.put("temporalResolution", "https://temporalResolution");
        body.put("spacialResolutions", List.of("http://spacialResolutions"));
    }

    private JSONObject generateCatalogRecord() {
        JSONObject record = new JSONObject();
        record.put("creator", "creator");
        record.put("contributor", "contributor");
        return record;
    }

    private void createANewDataset(String nextId) throws RmesException {
        IRI iri = SimpleValueFactory.getInstance().createIRI("http://datasetIRI/" + nextId);
        IRI catalogRecordIri = SimpleValueFactory.getInstance().createIRI("http://recordIRI/" + nextId);
        try (
                MockedStatic<DatasetQueries> datasetQueriesMock = Mockito.mockStatic(DatasetQueries.class);
                MockedStatic<RdfUtils> rdfUtilsMock = Mockito.mockStatic(RdfUtils.class);
                MockedStatic<DateUtils> dateUtilsMock = Mockito.mockStatic(DateUtils.class)
        ) {

            rdfUtilsMock.when(() -> RdfUtils.createIRI(any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.toURI(anyString())).thenCallRealMethod();

            datasetQueriesMock.when(() -> DatasetQueries.lastDatasetId(any())).thenReturn("query");
            dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn("2023-10-19T11:44:23.335590");
            dateUtilsMock.when(() -> DateUtils.parseDate(anyString())).thenCallRealMethod();
            dateUtilsMock.when(() -> DateUtils.parseDateTime(anyString())).thenReturn(LocalDateTime.parse("2023-10-19T11:44:23.335590"));
            rdfUtilsMock.when(() -> RdfUtils.seriesIRI("2")).thenReturn(SimpleValueFactory.getInstance().createIRI("http://seriesIRI/2"));
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(anyString(), anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralDateTime(any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralDate(any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleString(any(), any(), any(), any(), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleString(any(), any(), any(), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleInt(any(), any(), any(), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleUri(any(IRI.class), any(), any(IRI.class), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleDateTime(any(), any(), any(), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleUri(any(), any(IRI.class), any(String.class), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralInt(any())).thenCallRealMethod();
            rdfUtilsMock.when(RdfUtils::createBlankNode).thenCallRealMethod();



            JSONObject body = new JSONObject();

            this.generateGeneralInformation(body);
            this.generateGeneralManagment(body);
            this.generateStatisticsINformations(body);

            body.put("catalogRecord", this.generateCatalogRecord());

            body.put("descriptionLg2", "descriptionLg2");

            body.put("idSerie", "2");
            body.put("themes", new JSONArray().put("https://theme"));


            JSONArray distributions = new JSONArray();
            when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(distributions);

            when(seriesUtils.isSeriesExist(anyString())).thenReturn(true);


            String id = datasetService.create(body.toString());

            ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);
            verify(repositoryGestion, times(1)).loadSimpleObject(eq(iri), model.capture(), any());

            ArgumentCaptor<Model> model2 = ArgumentCaptor.forClass(Model.class);
            verify(repositoryGestion, times(1)).loadSimpleObject(eq(catalogRecordIri), model2.capture(), any());

            Assertions.assertEquals("[(http://datasetIRI/jd1001, http://purl.org/dc/terms/identifier, \"jd1001\", http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.w3.org/ns/dcat#Dataset, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/title, \"labelLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/title, \"labelLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#subtitle, \"subTitleLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#subtitle, \"subTitleLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/accrualPeriodicity, https://accrualPeriodicity, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/accessRights, https://accessRights, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#confidentialityStatus, https://confidentialityStatus, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/creator, \"c1\", http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/creator, \"c2\", http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/publisher, \"c3\", http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/dcat#landingPage, \"landingPageLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/dcat#landingPage, \"landingPageLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/modified, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/issued, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#disseminationStatus, https://disseminationStatus, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#processStep, https://disseminationStatus, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#archiveUnit, \"https://archiveUnit\", http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/description, \"descriptionLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/type, http://type, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#statisticalUnit, https://statisticalUnit, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#structure, https://dataStructure, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#numObservations, \"2\"^^<http://www.w3.org/2001/XMLSchema#int>, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/spatial, https://spacialCoverage, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/dcat#temporalResolution, https://temporalResolution, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#spatialResolution, http://spacialResolutions, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#validationState, \"Unpublished\", http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/prov#wasGeneratedBy, http://seriesIRI/2, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/dcat#theme, https://theme, http://datasetGraph/) [http://datasetGraph/]]".replaceAll("jd1001", nextId), model.getValue().toString());
            Assertions.assertEquals("[(http://recordIRI/jd1000, http://xmlns.com/foaf/0.1/primaryTopic, http://datasetIRI/jd1000, http://datasetGraph/) [http://datasetGraph/], (http://recordIRI/jd1000, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.w3.org/ns/dcat#CatalogRecord, http://datasetGraph/) [http://datasetGraph/], (http://recordIRI/jd1000, http://purl.org/dc/elements/1.1/creator, \"creator\", http://datasetGraph/) [http://datasetGraph/], (http://recordIRI/jd1000, http://purl.org/dc/elements/1.1/contributor, \"contributor\", http://datasetGraph/) [http://datasetGraph/], (http://recordIRI/jd1000, http://purl.org/dc/terms/created, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/], (http://recordIRI/jd1000, http://purl.org/dc/terms/modified, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/]]".replaceAll("jd1000", nextId), model2.getValue().toString());
            Assertions.assertEquals(id, nextId);
        }
    }


    @Test
    void shouldPersistExistingDataset() throws RmesException {
        IRI iri = SimpleValueFactory.getInstance().createIRI("http://datasetIRI/jd1001");
        IRI catalogRecordIri = SimpleValueFactory.getInstance().createIRI("http://recordIRI/jd1001");

        try (
                MockedStatic<DatasetQueries> datasetQueriesMock = Mockito.mockStatic(DatasetQueries.class);
                MockedStatic<RdfUtils> rdfUtilsMock = Mockito.mockStatic(RdfUtils.class);
                MockedStatic<DateUtils> dateUtilsMock = Mockito.mockStatic(DateUtils.class)
        ) {
            rdfUtilsMock.when(() -> RdfUtils.createIRI(any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.toURI(any())).thenCallRealMethod();

            datasetQueriesMock.when(() -> DatasetQueries.lastDatasetId(any())).thenReturn("query");
            dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn("2023-10-19T11:44:23.335590");
            dateUtilsMock.when(() -> DateUtils.parseDateTime(eq("2023-10-19T11:44:23.335590"))).thenReturn(LocalDateTime.parse("2023-10-19T11:44:23.335590"));
            dateUtilsMock.when(() -> DateUtils.parseDateTime(eq("2022-10-19T11:44:23.335590"))).thenReturn(LocalDateTime.parse("2022-10-19T11:44:23.335590"));
            dateUtilsMock.when(() -> DateUtils.parseDate(eq("2022-10-19T11:44:23.335590"))).thenReturn(Date.from(Instant.parse("2022-10-19T00:00:00.000Z")));
            dateUtilsMock.when(() -> DateUtils.parseDate(eq("2022-10-19T11:44:23.335590"))).thenReturn(Date.from(Instant.parse("2022-10-19T00:00:00.000Z")));

            rdfUtilsMock.when(() -> RdfUtils.seriesIRI("2")).thenReturn(SimpleValueFactory.getInstance().createIRI("http://seriesIRI/2"));
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(anyString(), anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralDateTime(any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralDate(any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleString(any(), any(), any(), any(), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleString(any(), any(), any(), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleInt(any(), any(), any(), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleUri(any(), any(), any(String.class), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleDateTime(any(), any(), any(), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralInt(any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleBNode(any(), any(), any(), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(RdfUtils::createBlankNode).thenCallRealMethod();

            JSONObject body = new JSONObject();

            this.generateGeneralInformation(body);
            this.generateGeneralManagment(body);
            this.generateStatisticsINformations(body);

            body.put("descriptionLg1", "descriptionLg1");
            body.put("descriptionLg2", "descriptionLg2");
            body.put("idSerie", "2");
            body.put("themes", new JSONArray().put("http://theme"));

            JSONObject record = new JSONObject();
            record.put("creator", "creator");
            record.put("contributor", "contributor");
            record.put("created", "2023-10-19T11:44:23.335590");

            body.put("catalogRecord", record);

            JSONArray distributions = new JSONArray();
            JSONObject d = new JSONObject();
            d.put("id", "d1000");
            distributions.put(d);
            when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(distributions);

            when(seriesUtils.isSeriesExist(anyString())).thenReturn(true);

            String id = datasetService.update("jd1001", body.toString());

            ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);
            verify(repositoryGestion, times(1)).loadSimpleObject(eq(iri), model.capture(), any());

            ArgumentCaptor<Model> model2 = ArgumentCaptor.forClass(Model.class);
            verify(repositoryGestion, times(1)).loadSimpleObject(eq(catalogRecordIri), model2.capture(), any());

            Assertions.assertEquals("[(http://datasetIRI/jd1001, http://purl.org/dc/terms/identifier, \"jd1001\", http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.w3.org/ns/dcat#Dataset, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/title, \"labelLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/title, \"labelLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#subtitle, \"subTitleLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#subtitle, \"subTitleLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/accrualPeriodicity, https://accrualPeriodicity, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/accessRights, https://accessRights, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#confidentialityStatus, https://confidentialityStatus, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/creator, \"c1\", http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/creator, \"c2\", http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/publisher, \"c3\", http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/dcat#landingPage, \"landingPageLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/dcat#landingPage, \"landingPageLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/modified, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/issued, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#disseminationStatus, https://disseminationStatus, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#processStep, https://disseminationStatus, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#archiveUnit, \"https://archiveUnit\", http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/description, \"descriptionLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/description, \"descriptionLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/type, http://type, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#statisticalUnit, https://statisticalUnit, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#structure, https://dataStructure, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#numObservations, \"2\"^^<http://www.w3.org/2001/XMLSchema#int>, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/spatial, https://spacialCoverage, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/dcat#temporalResolution, https://temporalResolution, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#spatialResolution, http://spacialResolutions, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/prov#wasGeneratedBy, http://seriesIRI/2, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/dcat#theme, http://theme, http://datasetGraph/) [http://datasetGraph/]]", model.getValue().toString());
            Assertions.assertEquals("[(http://recordIRI/jd1001, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.w3.org/ns/dcat#CatalogRecord, http://datasetGraph/) [http://datasetGraph/], (http://recordIRI/jd1001, http://purl.org/dc/elements/1.1/creator, \"creator\", http://datasetGraph/) [http://datasetGraph/], (http://recordIRI/jd1001, http://purl.org/dc/elements/1.1/contributor, \"contributor\", http://datasetGraph/) [http://datasetGraph/], (http://recordIRI/jd1001, http://purl.org/dc/terms/created, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/], (http://recordIRI/jd1001, http://purl.org/dc/terms/modified, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/]]", model2.getValue().toString());
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
