package fr.insee.rmes.bauhaus_services.datasets;

import fr.insee.rmes.bauhaus_services.distribution.DistributionQueries;
import fr.insee.rmes.bauhaus_services.operations.series.SeriesUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.model.dataset.PatchDataset;
import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.IdGenerator;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCAT;
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
import java.util.Set;

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
        "fr.insee.rmes.bauhaus.adms.graph=adms",
        "fr.insee.rmes.bauhaus.adms.identifiantsAlternatifs.baseURI=identifiantsAlternatifs",
        "fr.insee.rmes.bauhaus.lg1=fr",
        "fr.insee.rmes.bauhaus.lg2=en"
})
class DatasetServiceImplTest {

    @MockBean
    SeriesUtils seriesUtils;

    @MockBean
    PublicationUtils publicationUtils;
    @MockBean
    RepositoryGestion repositoryGestion;
    @Autowired
    DatasetServiceImpl datasetService;
    public static final IdGenerator idGenerator = new IdGenerator();


    public static final String DATASET_WITH_THEME = """
            [
               {
                  "observationNumber":"1000",
                  "id":"jd1000",
                  "issued":"2024-03-01T00:00:00",
                  "timeSeriesNumber":"10",
                  "temporalCoverageEndDate":"2024-01-01",
                  "temporalCoverageStartDate":"2024-01-01",
                  "updated":"2024-04-01T00:00:00",
               },
               {
                  "observationNumber":"1000",
                  "id":"jd1000",
                  "issued":"2024-03-01T00:00:00",
                  "timeSeriesNumber":"10",
                  "temporalCoverageEndDate":"2024-01-01",
                  "temporalCoverageStartDate":"2024-01-01",
                  "updated":"2024-04-01T00:00:00",
               }
            ]""";
    public static final String EMPTY_ARRAY = "[]";

    @Test
    void shouldReturnDatasets() throws RmesException {
        JSONArray array = new JSONArray();
        array.put("result");

        when(repositoryGestion.getResponseAsArray("query")).thenReturn(array);
        try (MockedStatic<DatasetQueries> mockedFactory = Mockito.mockStatic(DatasetQueries.class)) {
            mockedFactory.when(() -> DatasetQueries.getDatasets(anyString(), eq(null))).thenReturn("query");
            String query = datasetService.getDatasets();
            Assertions.assertEquals("[\"result\"]", query);
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
            Assertions.assertEquals("[\"result\"]", query);
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
            mockedFactory.when(() -> DatasetQueries.getDataset(eq("1"), any(), any())).thenReturn("query");
            mockedFactory.when(() -> DatasetQueries.getDatasetCreators(eq("1"), any())).thenReturn("query-creators");
            mockedFactory.when(() -> DatasetQueries.getDatasetSpacialResolutions(eq("1"), any())).thenReturn("query-spacialResolutions");
            mockedFactory.when(() -> DatasetQueries.getDatasetStatisticalUnits(eq("1"), any())).thenReturn("query-statisticalUnits");
            String query = datasetService.getDatasetByID("1");
            Assertions.assertEquals("{\"themes\":[],\"catalogRecord\":{},\"creators\":[\"creator-1\"],\"statisticalUnit\":[\"statisticalUnit-1\"],\"id\":\"1\",\"spacialResolutions\":[\"spacialResolutions-1\"]}", query);
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
            Assertions.assertEquals("[\"1\"]", query);
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
            Assertions.assertEquals("{\"message\":\"The property labelLg1 is required\"}", exception.getDetails());
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
            Assertions.assertEquals("{\"message\":\"The property labelLg2 is required\"}", exception.getDetails());
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
            Assertions.assertEquals("{\"message\":\"The property creator is required\"}", exception.getDetails());
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
            Assertions.assertEquals("{\"message\":\"The property contributor is required\"}", exception.getDetails());
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
            Assertions.assertEquals("{\"message\":\"The property disseminationStatus is required\"}", exception.getDetails());
        }
    }

    @Test
    void shouldReturnAnErrorBadFormattedAltIdentifierWhenCreating() throws RmesException {
        try (MockedStatic<DatasetQueries> mockedFactory = Mockito.mockStatic(DatasetQueries.class)) {
            mockedFactory.when(() -> DatasetQueries.lastDatasetId(any())).thenReturn("query");
            JSONObject body = new JSONObject();
            body.put("labelLg1", "labelLg1");
            body.put("labelLg2", "labelLg2R");
            body.put("disseminationStatus", "disseminationStatus");
            body.put("altIdentifier", "%abc");
            body.put("catalogRecord", this.generateCatalogRecord());

            when(repositoryGestion.getResponseAsObject(anyString())).then(invocationOnMock -> {
                JSONObject lastId = new JSONObject();
                lastId.put("id", "1000");
                return lastId;
            });
            RmesException exception = assertThrows(RmesBadRequestException.class, () -> datasetService.create(body.toString()));
            Assertions.assertEquals("{\"message\":\"The property altIdentifier contains forbidden characters\"}", exception.getDetails());
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
            body.put("altIdentifier", "abc");
            body.put("catalogRecord", this.generateCatalogRecord());

            when(seriesUtils.isSeriesExist(any())).thenReturn(false);

            when(repositoryGestion.getResponseAsObject(anyString())).then(invocationOnMock -> {
                JSONObject lastId = new JSONObject();
                lastId.put("id", "1000");
                return lastId;
            });
            RmesException exception = assertThrows(RmesBadRequestException.class, () -> datasetService.create(body.toString()));
            Assertions.assertEquals("{\"message\":\"The series does not exist\"}", exception.getDetails());
        }
    }

    @Test
    void shouldReturnAnErrorIfUnknownSeriesNotDefinedWhenCreatingEvenIfAltIdentifierMissing() throws RmesException {
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
            Assertions.assertEquals("{\"message\":\"The series does not exist\"}", exception.getDetails());
        }
    }

    @Test
    void shouldPersistNewDatasetWithAndIncrementedId() throws RmesException {
        createANewDataset("jd1001");
    }

    @Test
    void shouldPatchDataset() throws RmesException {
        IRI iri = SimpleValueFactory.getInstance().createIRI("http://datasetIRI/jd1001");

        String datasetId = "jd1001";

        JSONObject object = new JSONObject();
        object.put("id", datasetId);
        generateGeneralInformation(object);
        object.put("disseminationStatus", "http://disseminationStatus");
        object.put("catalogRecordCreator", "creator");
        object.put("catalogRecordContributor", List.of("contributor"));

        JSONArray distributions = new JSONArray();
        JSONObject d = new JSONObject();
        d.put("id", "d1000");
        distributions.put(d);
        when(repositoryGestion.getResponseAsArray(anyString())).thenReturn(distributions);

        JSONArray array = new JSONArray().put(object);
        when(seriesUtils.isSeriesExist(any())).thenReturn(true);
        when(repositoryGestion.getResponseAsArray("query")).thenReturn(array);
        when(repositoryGestion.getResponseAsArray("query-creators")).thenReturn(new JSONArray().put(new JSONObject().put("creator", "http://creator-1")));
        when(repositoryGestion.getResponseAsArray("query-contributor")).thenReturn(new JSONArray().put(new JSONObject().put("contributor", "contributor")));
        when(repositoryGestion.getResponseAsArray("query-spacialResolutions")).thenReturn(new JSONArray().put(new JSONObject().put("spacialResolution", "http://spacialResolutions-1")));
        when(repositoryGestion.getResponseAsArray("query-statisticalUnits")).thenReturn(new JSONArray().put(new JSONObject().put("statisticalUnit", "http://statisticalUnit-1")));
        try (MockedStatic<DatasetQueries> mockedFactory = Mockito.mockStatic(DatasetQueries.class)) {
            mockedFactory.when(() -> DatasetQueries.getDataset(eq(datasetId), any(), any())).thenReturn("query");
            mockedFactory.when(() -> DatasetQueries.getDatasetCreators(eq(datasetId), any())).thenReturn("query-creators");
            mockedFactory.when(() -> DatasetQueries.getDatasetSpacialResolutions(eq(datasetId), any())).thenReturn("query-spacialResolutions");
            mockedFactory.when(() -> DatasetQueries.getDatasetContributors(any(), any())).thenReturn("query-contributor");
            mockedFactory.when(() -> DatasetQueries.getDatasetStatisticalUnits(eq(datasetId), any())).thenReturn("query-statisticalUnits");

            PatchDataset dataset = new PatchDataset();
            dataset.setNumObservations(5);
            datasetService.patchDataset(datasetId, dataset);


            ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);
            verify(repositoryGestion, times(1)).loadSimpleObject(eq(iri), model.capture(), any());

            Assertions.assertEquals("[(http://datasetIRI/jd1001, http://purl.org/dc/terms/identifier, \"jd1001\", http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.w3.org/ns/dcat#Dataset, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/title, \"labelLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/title, \"labelLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#subtitle, \"subTitleLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#subtitle, \"subTitleLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/accrualPeriodicity, https://accrualPeriodicity, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/accessRights, https://accessRights, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#confidentialityStatus, https://confidentialityStatus, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/creator, http://creator-1, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/publisher, http://c3, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/dcat#landingPage, \"landingPageLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/dcat#landingPage, \"landingPageLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/modified, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/issued, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#disseminationStatus, http://disseminationStatus, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#statisticalUnit, http://statisticalUnit-1, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#numObservations, \"5\"^^<http://www.w3.org/2001/XMLSchema#int>, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#spatialResolution, http://spacialResolutions-1, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/dcat#distribution, http://distributionIRI/d1000, http://datasetGraph/) [http://datasetGraph/]]", model.getValue().toString());
        }
    }

    private void generateGeneralInformation(JSONObject body) {
        body.put("labelLg1", "labelLg1");
        body.put("labelLg2", "labelLg2");
        body.put("subTitleLg1", "subTitleLg1");
        body.put("subTitleLg2", "subTitleLg2");
        body.put("accrualPeriodicity", "https://accrualPeriodicity");
        body.put("accessRights", "https://accessRights");
        body.put("confidentialityStatus", "https://confidentialityStatus");
        body.put("creators", List.of("http://c1", "http://c2"));
        body.put("publisher", "http://c3");
        body.put("landingPageLg1", "landingPageLg1");
        body.put("landingPageLg2", "landingPageLg2");
        body.put("updated", "2023-10-19T11:44:23.335590");
        body.put("issued", "2023-10-19T11:44:23.335590");
    }

    private void generateGeneralManagment(JSONObject body) {
        body.put("disseminationStatus", "https://disseminationStatus");
        body.put("processStep", "https://disseminationStatus");
        body.put("archiveUnit", "https://archiveUnit");
    }

    private void generateStatisticsINformations(JSONObject body) {
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
        record.put("contributor", List.of("contributor"));
        return record;
    }

    private void createANewDataset(String nextId) throws RmesException {
        try (
                MockedStatic<DatasetQueries> datasetQueriesMock = Mockito.mockStatic(DatasetQueries.class);
                MockedStatic<RdfUtils> rdfUtilsMock = Mockito.mockStatic(RdfUtils.class);
                MockedStatic<DateUtils> dateUtilsMock = Mockito.mockStatic(DateUtils.class);
                MockedStatic<IdGenerator> idGeneratorMock = Mockito.mockStatic(IdGenerator.class)
        ) {
            idGeneratorMock.when(idGenerator::generateNextId).thenReturn(nextId);

            IRI iri = SimpleValueFactory.getInstance().createIRI("http://datasetIRI/" + nextId);
            IRI catalogRecordIri = SimpleValueFactory.getInstance().createIRI("http://recordIRI/" + nextId);

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

            Assertions.assertEquals("[(http://datasetIRI/jd1001, http://purl.org/dc/terms/identifier, \"jd1001\", http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.w3.org/ns/dcat#Dataset, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/title, \"labelLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/title, \"labelLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#subtitle, \"subTitleLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#subtitle, \"subTitleLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/accrualPeriodicity, https://accrualPeriodicity, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/accessRights, https://accessRights, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#confidentialityStatus, https://confidentialityStatus, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/creator, http://c1, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/creator, http://c2, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/publisher, http://c3, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/dcat#landingPage, \"landingPageLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/dcat#landingPage, \"landingPageLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/modified, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/issued, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#disseminationStatus, https://disseminationStatus, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#processStep, https://disseminationStatus, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#archiveUnit, https://archiveUnit, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/description, \"descriptionLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/type, http://type, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#statisticalUnit, https://statisticalUnit, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#structure, https://dataStructure, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#numObservations, \"2\"^^<http://www.w3.org/2001/XMLSchema#int>, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/spatial, https://spacialCoverage, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/dcat#temporalResolution, https://temporalResolution, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#spatialResolution, http://spacialResolutions, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#validationState, \"Unpublished\", http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/prov#wasGeneratedBy, http://seriesIRI/2, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/dcat#theme, https://theme, http://datasetGraph/) [http://datasetGraph/]]".replaceAll("jd1001", nextId), model.getValue().toString());
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
            rdfUtilsMock.when(() -> RdfUtils.addTripleUri(any(), any(), any(IRI.class), any(), any())).thenCallRealMethod();
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
            body.put("altIdentifier", "1");

            JSONObject record = new JSONObject();
            record.put("creator", "creator");
            record.put("contributor", List.of("contributor"));
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

            Assertions.assertEquals("[(http://datasetIRI/jd1001, http://purl.org/dc/terms/identifier, \"jd1001\", http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.w3.org/ns/dcat#Dataset, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/title, \"labelLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/title, \"labelLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#subtitle, \"subTitleLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#subtitle, \"subTitleLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/accrualPeriodicity, https://accrualPeriodicity, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/accessRights, https://accessRights, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#confidentialityStatus, https://confidentialityStatus, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/creator, http://c1, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/creator, http://c2, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/publisher, http://c3, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/dcat#landingPage, \"landingPageLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/dcat#landingPage, \"landingPageLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/modified, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/issued, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#disseminationStatus, https://disseminationStatus, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#processStep, https://disseminationStatus, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#archiveUnit, https://archiveUnit, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/adms#identifier, http://identifiantsAlternatifs/datasetIRI/jd1001, http://datasetGraph/) [http://datasetGraph/], (http://identifiantsAlternatifs/datasetIRI/jd1001, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.w3.org/ns/adms#Identifier, http://adms) [http://adms], (http://identifiantsAlternatifs/datasetIRI/jd1001, http://www.w3.org/2004/02/skos/core#notation, \"1\", http://adms) [http://adms], (http://datasetIRI/jd1001, http://purl.org/dc/terms/description, \"descriptionLg1\"@fr, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/description, \"descriptionLg2\"@en, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/type, http://type, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#statisticalUnit, https://statisticalUnit, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#structure, https://dataStructure, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#numObservations, \"2\"^^<http://www.w3.org/2001/XMLSchema#int>, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://purl.org/dc/terms/spatial, https://spacialCoverage, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/dcat#temporalResolution, https://temporalResolution, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://rdf.insee.fr/def/base#spatialResolution, http://spacialResolutions, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/prov#wasGeneratedBy, http://seriesIRI/2, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/dcat#theme, http://theme, http://datasetGraph/) [http://datasetGraph/], (http://datasetIRI/jd1001, http://www.w3.org/ns/dcat#distribution, http://distributionIRI/d1000, http://datasetGraph/) [http://datasetGraph/]]", model.getValue().toString());
            Assertions.assertEquals("[(http://recordIRI/jd1001, http://xmlns.com/foaf/0.1/primaryTopic, http://datasetIRI/jd1001, http://datasetGraph/) [http://datasetGraph/], (http://recordIRI/jd1001, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.w3.org/ns/dcat#CatalogRecord, http://datasetGraph/) [http://datasetGraph/], (http://recordIRI/jd1001, http://purl.org/dc/elements/1.1/creator, \"creator\", http://datasetGraph/) [http://datasetGraph/], (http://recordIRI/jd1001, http://purl.org/dc/elements/1.1/contributor, \"contributor\", http://datasetGraph/) [http://datasetGraph/], (http://recordIRI/jd1001, http://purl.org/dc/terms/created, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/], (http://recordIRI/jd1001, http://purl.org/dc/terms/modified, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>, http://datasetGraph/) [http://datasetGraph/]]", model2.getValue().toString());
            Assertions.assertEquals("jd1001", id);
        }
    }

    @Test
    void shouldPublishADataset() throws RmesException {
        IRI iri = SimpleValueFactory.getInstance().createIRI("http://datasetIRI/1");
        IRI catalogRecordIri = SimpleValueFactory.getInstance().createIRI("http://catalogRecordIRI/1");

        doNothing().when(publicationUtils).publishResource(iri, Set.of());
        doNothing().when(publicationUtils).publishResource(catalogRecordIri, Set.of("creator", "contributor"));

        String id = datasetService.publishDataset("1");
        ArgumentCaptor<Model> modelIri = ArgumentCaptor.forClass(Model.class);

        verify(repositoryGestion, times(1)).objectValidation(eq(iri), modelIri.capture());
        Assertions.assertEquals("[(http://datasetIRI/1, http://rdf.insee.fr/def/base#validationState, \"Validated\", http://datasetGraph/) [http://datasetGraph/]]", modelIri.getValue().toString());
        Assertions.assertEquals("1", id);
    }

    @Test
    void shouldThrowAnExceptionIfTheBodyIsNotAJSONDuringCreation() {
        Assertions.assertThrows(RmesException.class, () -> datasetService.create(""));
    }

    @Test
    void shouldThrowAnExceptionIfTheBodyIsNotAJSONDuringUpdate() {
        Assertions.assertThrows(RmesException.class, () -> datasetService.update("d1000", ""));
    }

    @Test
    void shouldPatchDatasetReturn400IfNoOneOfRequiredAttributesPatchEmpty() throws RmesException {
        PatchDataset patch = new PatchDataset();
        JSONArray datasetWithTheme = new JSONArray(DATASET_WITH_THEME);
        JSONArray empty_array = new JSONArray(EMPTY_ARRAY);

        try (
                MockedStatic<DatasetQueries> datasetQueriesMock = Mockito.mockStatic(DatasetQueries.class);
        ) {

            datasetQueriesMock.when(() -> DatasetQueries.getDataset(any(), any(), any())).thenReturn("query1 ");
            when(repositoryGestion.getResponseAsArray("query1 ")).thenReturn(datasetWithTheme);

            datasetQueriesMock.when(() -> DatasetQueries.getDatasetCreators(any(), any())).thenReturn("query2 ");
            when(repositoryGestion.getResponseAsArray("query2 ")).thenReturn(empty_array);

            datasetQueriesMock.when(() -> DatasetQueries.getDatasetSpacialResolutions(any(), any())).thenReturn("query3 ");
            when(repositoryGestion.getResponseAsArray("query3 ")).thenReturn(empty_array);

            datasetQueriesMock.when(() -> DatasetQueries.getDatasetStatisticalUnits(any(), any())).thenReturn("query4 ");
            when(repositoryGestion.getResponseAsArray("query4 ")).thenReturn(empty_array);

            RmesException exception = assertThrows(RmesBadRequestException.class, () -> datasetService.patchDataset("jd0001", patch));
            Assertions.assertEquals("{\"code\":1202,\"message\":\"One of these attributes is required : updated, issued, numObservations, numSeries, temporal\"}", exception.getDetails());
        }
    }

    @Test
    void shouldNotDeleteNotExistingDatasetReturn404() throws RmesException {
        JSONArray mockJSON = new JSONArray("[]");
        when(repositoryGestion.getResponseAsArray(Mockito.anyString())).thenReturn(mockJSON);
        RmesNotFoundException exception = assertThrows(RmesNotFoundException.class, () -> datasetService.deleteDatasetId("idTest"));
        Assertions.assertEquals("{\"details\":\"Not found\",\"message\":\"This dataset does not exist\"}", exception.getDetails());
    }

    @Test
    void shouldNotDeleteNotUnpublishedDatasetAndReturn406() throws RmesException {
        JSONArray mockJSON = new JSONArray("[{\n" +
                "  \"id\": idTest,\n" +
                "  \"validationState\": \"Not Unpublished\",\n" +
                "  \"catalogRecordCreator\": \"DG57-C003\"\n" +
                "}\n" +
                "]");

        JSONArray empty_array = new JSONArray(EMPTY_ARRAY);
        try (
                MockedStatic<DatasetQueries> datasetQueriesMock = Mockito.mockStatic(DatasetQueries.class);
        ) {

            datasetQueriesMock.when(() -> DatasetQueries.getDataset(any(), any(), any())).thenReturn("query1 ");
            when(repositoryGestion.getResponseAsArray("query1 ")).thenReturn(mockJSON);

            datasetQueriesMock.when(() -> DatasetQueries.getDatasetCreators(any(), any())).thenReturn("query2 ");
            when(repositoryGestion.getResponseAsArray("query2 ")).thenReturn(empty_array);

            RmesBadRequestException exception = assertThrows(RmesBadRequestException.class, () -> datasetService.deleteDatasetId("idTest"));
            Assertions.assertEquals("{\"code\":1203,\"message\":\"Only unpublished datasets can be deleted\"}", exception.getDetails());
        }
    }

    @Test
    void shouldNotDeleteDataSetWithDistributionAndReturn400() throws RmesException {
        JSONArray mockJSON = new JSONArray("[{\n" +
                "  \"id\": idTest,\n" +
                "  \"validationState\": \"Unpublished\",\n" +
                "  \"catalogRecordCreator\": \"DG57-C003\"\n" +
                "}\n" +
                "]");
        JSONArray empty_array = new JSONArray(EMPTY_ARRAY);
        JSONArray mockDistrib = new JSONArray("[{\"idDataset\":\"idTest\",\"id\":\"distrib1\"}]");
        try (
                MockedStatic<DatasetQueries> datasetQueriesMock = Mockito.mockStatic(DatasetQueries.class);
                MockedStatic<DistributionQueries> distributionQueriesMock = Mockito.mockStatic(DistributionQueries.class);
        )
        {

            datasetQueriesMock.when(() -> DatasetQueries.getDataset(any(), any(), any())).thenReturn("query1 ");
            when(repositoryGestion.getResponseAsArray("query1 ")).thenReturn(mockJSON);

            datasetQueriesMock.when(() -> DatasetQueries.getDatasetCreators(any(), any())).thenReturn("query2 ");
            when(repositoryGestion.getResponseAsArray("query2 ")).thenReturn(empty_array);

            distributionQueriesMock.when(() -> DistributionQueries.getDatasetDistributions(any(), any())).thenReturn("query3 ");
            when(repositoryGestion.getResponseAsArray("query3 ")).thenReturn(mockDistrib);

            RmesBadRequestException exception = assertThrows(RmesBadRequestException.class, () -> datasetService.deleteDatasetId("idTest"));
            Assertions.assertEquals("{\"code\":1204,\"message\":\"Only dataset without any distribution can be deleted\"}", exception.getDetails());
        }
    }

    @Test
    void shouldDeleteDataSet() throws RmesException{
        JSONArray mockJSON = new JSONArray("[{\n" +
                "  \"id\": idTest,\n" +
                "  \"validationState\": \"Unpublished\",\n" +
                "  \"catalogRecordCreator\": \"DG57-C003\"\n" +
                "}\n" +
                "]");
        JSONArray empty_array = new JSONArray(EMPTY_ARRAY);

        String stringDatasetURI="http://bauhaus/catalogues/entreeCatalogue/idtest";
        IRI datasetUri= RdfUtils.toURI(stringDatasetURI);
        try (
                MockedStatic<DatasetQueries> datasetQueriesMock = Mockito.mockStatic(DatasetQueries.class);
                MockedStatic<DistributionQueries> distributionQueriesMock = Mockito.mockStatic(DistributionQueries.class);
                MockedStatic<RdfUtils> rdfUtilsMock = Mockito.mockStatic(RdfUtils.class);

        )
        {

            datasetQueriesMock.when(() -> DatasetQueries.getDataset(any(), any(), any())).thenReturn("query1 ");
            when(repositoryGestion.getResponseAsArray("query1 ")).thenReturn(mockJSON);

            datasetQueriesMock.when(() -> DatasetQueries.getDatasetCreators(any(), any())).thenReturn("query2 ");
            when(repositoryGestion.getResponseAsArray("query2 ")).thenReturn(empty_array);

            distributionQueriesMock.when(() -> DistributionQueries.getDatasetDistributions(any(), any())).thenReturn("query3 ");
            when(repositoryGestion.getResponseAsArray("query3 ")).thenReturn(empty_array);

            rdfUtilsMock.when(() -> RdfUtils.createIRI(any(String.class))).thenReturn(datasetUri);
            rdfUtilsMock.when(() -> RdfUtils.toURI(any(String.class))).thenReturn(datasetUri);

            // Capture the argument passed to deleteObject
            ArgumentCaptor<IRI> uriCaptor = ArgumentCaptor.forClass(IRI.class);

            datasetService.deleteDatasetId("idTest");

            verify(repositoryGestion, times(1)).deleteObject(uriCaptor.capture());
            Assertions.assertEquals(datasetUri, uriCaptor.getValue());

            verify(repositoryGestion, times(1)).deleteObject(datasetUri);
            verify(repositoryGestion, times(1)).deleteTripletByPredicate(any(IRI.class), eq(DCAT.DATASET), any(IRI.class));
        }
    }

}