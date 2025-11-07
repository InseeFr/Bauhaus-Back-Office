package fr.insee.rmes.bauhaus_services.distribution;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.persistance.sparql_queries.datasets.DatasetDistributionQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.model.dataset.Distribution;
import fr.insee.rmes.model.dataset.PatchDistribution;
import fr.insee.rmes.domain.exceptions.RmesException;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@AppSpringBootTest
class DistributionServiceImplTest {
    @MockitoBean
    RepositoryGestion repositoryGestion;

    @MockitoBean
    PublicationUtils publicationUtils;
    @MockitoBean
    IdGenerator idGenerator;
    @Autowired
    DistributionServiceImpl distributionService;

    private static final String EMPTY_JSON_OBJECT = "{}";
    private static final String DISTRIB = "{\"id\":\"d1000\"}";
    private static final String DISTRIB_A_PATCHER = "{\"byteSize\":\"3\",\"labelLg2\":\"test_patch\",\"labelLg1\":\"test_patch\",\"created\":\"2024-04-10T16:34:09.651166561\",\"idDataset\":\"jd1004\",\"id\":\"d1004\",\"updated\":\"2024-04-07T16:34:09.651166561\",\"url\":\"http://test\", \"validationState\": \"Unpublished\"}";
    private static final String DISTRIB_PATCHEE = "[(http://datasetIRI/jd1004, http://www.w3.org/ns/dcat#distribution, http://distributionIRI/d1004) [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1004, http://rdf.insee.fr/def/base#validationState, \"Unpublished\") [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1004, http://purl.org/dc/terms/identifier, \"d1004\") [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1004, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.w3.org/ns/dcat#Distribution) [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1004, http://purl.org/dc/terms/title, \"test_patch\"@fr) [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1004, http://purl.org/dc/terms/title, \"test_patch\"@en) [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1004, http://purl.org/dc/terms/created, \"2024-04-10T16:34:09.651166561\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1004, http://purl.org/dc/terms/modified, \"2024-04-05T16:34:09.651166561\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1004, http://www.w3.org/ns/dcat#byteSize, \"5\") [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1004, http://www.w3.org/ns/dcat#downloadURL, \"http://test2\") [http://rdf.insee.fr/graphes/datasetGraph/]]";


    @Test
    void shouldReturnDistributions() throws RmesException {
        JSONArray array = new JSONArray();
        array.put(new JSONObject().put("id", "1").put("labelLg1", "value"));

        when(repositoryGestion.getResponseAsArray("query")).thenReturn(array);
        try (MockedStatic<DatasetDistributionQueries> mockedFactory = Mockito.mockStatic(DatasetDistributionQueries.class)) {
            mockedFactory.when(() -> DatasetDistributionQueries.getDistributions(any())).thenReturn("query");
            var distributions = distributionService.getDistributions();
            Assertions.assertEquals("1", distributions.getFirst().id());
            Assertions.assertEquals("value", distributions.getFirst().labelLg1());
        }
    }

    @Test
    void shouldReturnDistributionsForSearch() throws RmesException {
        JSONArray array = new JSONArray();
        array.put(new JSONObject()
                .put("distributionId", "id")
                .put("distributionLabelLg1", "labelLg1")
                .put("distributionValidationStatus", "validationStatus")
                .put("distributionCreated", "created")
                .put("distributionUpdated", "updated")
                .put("altIdentifier", "altIdentifier")
                .put("id", "id")
                .put("labelLg1", "labelLg1")
                .put("creator", "creator")
                .put("disseminationStatus", "disseminationStatus")
                .put("validationStatus", "validationStatus")
                .put("wasGeneratedIRIs", "wasGeneratedIRIs")
                .put("created", "created")
                .put("updated", "updated"));

        when(repositoryGestion.getResponseAsArray("query")).thenReturn(array);
        try (MockedStatic<DatasetDistributionQueries> mockedFactory = Mockito.mockStatic(DatasetDistributionQueries.class)) {
            mockedFactory.when(() -> DatasetDistributionQueries.getDistributionsForSearch(any(), any())).thenReturn("query");
            var distributions = distributionService.getDistributionsForSearch();
            Assertions.assertEquals("id", distributions.getFirst().distributionId());
            Assertions.assertEquals("labelLg1", distributions.getFirst().distributionLabelLg1());
        }
    }

    @Test
    void shouldReturnDataset() throws RmesException, JSONException {
        JSONObject object = new JSONObject();
        object.put("id", "1");

        when(repositoryGestion.getResponseAsObject("query")).thenReturn(object);
        try (MockedStatic<DatasetDistributionQueries> mockedFactory = Mockito.mockStatic(DatasetDistributionQueries.class)) {
            mockedFactory.when(() -> DatasetDistributionQueries.getDistribution(eq("1"), any())).thenReturn("query");
            Distribution distribution = distributionService.getDistributionByID("1");
            Assertions.assertEquals("1", distribution.getId());
        }
    }

    @Test
    void shouldReturnAnErrorIfIdDatasetNotDefinedWhenCreating() throws RmesException, JSONException {
        try (MockedStatic<DatasetDistributionQueries> mockedFactory = Mockito.mockStatic(DatasetDistributionQueries.class)) {
            mockedFactory.when(() -> DatasetDistributionQueries.lastDatasetId(any())).thenReturn("query");
            JSONObject body = new JSONObject();


            when(repositoryGestion.getResponseAsObject(anyString())).then(invocationOnMock -> {
                JSONObject lastId = new JSONObject();
                lastId.put("id", "1000");
                return lastId;
            });
            RmesException exception = assertThrows(RmesBadRequestException.class, () -> distributionService.create(body.toString()));
            Assertions.assertEquals("{\"message\":\"The property idDataset is required\"}", exception.getDetails());
        }
    }

    @Test
    void shouldReturnAnErrorIfLabelLg1NotDefinedWhenCreating() throws RmesException, JSONException {
        try (MockedStatic<DatasetDistributionQueries> mockedFactory = Mockito.mockStatic(DatasetDistributionQueries.class)) {
            mockedFactory.when(() -> DatasetDistributionQueries.lastDatasetId(any())).thenReturn("query");
            JSONObject body = new JSONObject();
            body.put("idDataset", "idDataset");

            when(repositoryGestion.getResponseAsObject(anyString())).then(invocationOnMock -> {
                JSONObject lastId = new JSONObject();
                lastId.put("id", "1000");
                return lastId;
            });
            RmesException exception = assertThrows(RmesBadRequestException.class, () -> distributionService.create(body.toString()));
            Assertions.assertEquals("{\"message\":\"The property labelLg1 is required\"}", exception.getDetails());
        }
    }

    @Test
    void shouldReturnAnErrorIfLabelLg2NotDefinedWhenCreating() throws RmesException, JSONException {
        try (MockedStatic<DatasetDistributionQueries> mockedFactory = Mockito.mockStatic(DatasetDistributionQueries.class)) {
            mockedFactory.when(() -> DatasetDistributionQueries.lastDatasetId(any())).thenReturn("query");
            JSONObject body = new JSONObject();
            body.put("idDataset", "idDataset");
            body.put("labelLg1", "labelLg1");

            when(repositoryGestion.getResponseAsObject(anyString())).then(invocationOnMock -> {
                JSONObject lastId = new JSONObject();
                lastId.put("id", "1000");
                return lastId;
            });
            RmesException exception = assertThrows(RmesBadRequestException.class, () -> distributionService.create(body.toString()));
            Assertions.assertEquals("{\"message\":\"The property labelLg2 is required\"}", exception.getDetails());
        }
    }

    @Test
    void shouldPersistNewDistributionWithTheDefaultId() throws RmesException {
        createANewDistribution("d1000");
    }

    private void createANewDistribution(String nextId) throws RmesException {
        JSONObject mockJSON = new JSONObject(DISTRIB);
        try (
                MockedStatic<DatasetDistributionQueries> datasetQueriesMock = Mockito.mockStatic(DatasetDistributionQueries.class);
                MockedStatic<RdfUtils> rdfUtilsMock = Mockito.mockStatic(RdfUtils.class);
                MockedStatic<DateUtils> dateUtilsMock = Mockito.mockStatic(DateUtils.class)
        ) {
            when(idGenerator.generateNextId()).thenReturn(nextId);
            IRI iri = SimpleValueFactory.getInstance().createIRI("http://distributionIRI/" + nextId);

            rdfUtilsMock.when(() -> RdfUtils.createIRI(any())).thenCallRealMethod();

            datasetQueriesMock.when(() -> DatasetDistributionQueries.lastDatasetId(any())).thenReturn("query");
            datasetQueriesMock.when(() -> DatasetDistributionQueries.getDistribution(eq(nextId), any())).thenReturn("query " + nextId);
            when(repositoryGestion.getResponseAsObject("query " + nextId)).thenReturn(mockJSON);

            dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn("2023-10-19T11:44:23.335590");
            dateUtilsMock.when(() -> DateUtils.parseDateTime(anyString())).thenReturn(LocalDateTime.parse("2023-10-19T11:44:23.335590"));
            rdfUtilsMock.when(() -> RdfUtils.seriesIRI("2")).thenReturn(SimpleValueFactory.getInstance().createIRI("http://seriesIRI/2"));
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
            body.put("byteSize", "byteSize");
            body.put("url", "url");

            String id = distributionService.create(body.toString());

            ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);
            verify(repositoryGestion, times(1)).loadSimpleObject(eq(iri), model.capture(), any());

            Assertions.assertEquals("[(http://datasetIRI/idDataset, http://www.w3.org/ns/dcat#distribution, http://distributionIRI/d1000) [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1000, http://rdf.insee.fr/def/base#validationState, \"Unpublished\") [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1000, http://purl.org/dc/terms/identifier, \"d1000\") [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1000, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.w3.org/ns/dcat#Distribution) [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1000, http://purl.org/dc/terms/title, \"labelLg1\"@fr) [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1000, http://purl.org/dc/terms/title, \"labelLg2\"@en) [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1000, http://purl.org/dc/terms/description, \"descriptionLg1\"@fr) [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1000, http://purl.org/dc/terms/description, \"descriptionLg2\"@en) [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1000, http://purl.org/dc/terms/created, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1000, http://purl.org/dc/terms/modified, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1000, http://purl.org/dc/terms/format, \"format\") [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1000, http://www.w3.org/ns/dcat#byteSize, \"byteSize\") [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1000, http://www.w3.org/ns/dcat#downloadURL, \"url\") [http://rdf.insee.fr/graphes/datasetGraph/]]".replaceAll("d1000", nextId), model.getValue().toString());
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
                MockedStatic<DatasetDistributionQueries> datasetQueriesMock = Mockito.mockStatic(DatasetDistributionQueries.class);
                MockedStatic<RdfUtils> rdfUtilsMock = Mockito.mockStatic(RdfUtils.class);
                MockedStatic<DateUtils> dateUtilsMock = Mockito.mockStatic(DateUtils.class)
        ) {
            datasetQueriesMock.when(() -> DatasetDistributionQueries.lastDatasetId(any())).thenReturn("query");
            datasetQueriesMock.when(() -> DatasetDistributionQueries.getDistribution(eq("d1001"), any())).thenReturn("query d1001");

            if (!withDataSetRemoval) {
                JSONObject mockJSON = new JSONObject(DISTRIB);
                when(repositoryGestion.getResponseAsObject("query d1001")).thenReturn(mockJSON);
            } else {
                JSONObject distribution = new JSONObject();
                distribution.put("idDataset", "idDataset2");
                distribution.put("id", "id");
                when(repositoryGestion.getResponseAsObject("query d1001")).thenReturn(distribution);
            }

            rdfUtilsMock.when(() -> RdfUtils.createIRI(any())).thenCallRealMethod();

            dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn("2023-10-19T11:44:23.335590");
            dateUtilsMock.when(() -> DateUtils.parseDateTime(eq("2022-10-19T11:44:23.335590"))).thenReturn(LocalDateTime.parse("2022-10-19T11:44:23.335590"));
            dateUtilsMock.when(() -> DateUtils.parseDateTime(eq("2023-10-19T11:44:23.335590"))).thenReturn(LocalDateTime.parse("2023-10-19T11:44:23.335590"));
            rdfUtilsMock.when(() -> RdfUtils.seriesIRI("2")).thenReturn(SimpleValueFactory.getInstance().createIRI("http://seriesIRI/2"));
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(anyString(), anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralDateTime(any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleString(any(), any(), any(), any(), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleString(any(), any(), any(), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleUri(any(IRI.class), any(), any(IRI.class), any(), any())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleDateTime(any(), any(), any(), any(), any())).thenCallRealMethod();

            JSONObject body = new JSONObject();
            body.put("idDataset", "d1001");
            body.put("labelLg1", "labelLg1");
            body.put("labelLg2", "labelLg2");
            body.put("descriptionLg1", "descriptionLg1");
            body.put("descriptionLg2", "descriptionLg2");
            body.put("format", "format");
            body.put("byteSize", "byteSize");
            body.put("url", "url");
            body.put("created", "2022-10-19T11:44:23.335590");


            String id = distributionService.update("d1001", body.toString());

            ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);
            verify(repositoryGestion, times(1)).loadSimpleObject(eq(iri), model.capture(), any());

            if (withDataSetRemoval) {
                verify(repositoryGestion, times(1)).deleteTripletByPredicateAndValue(any(), any(), any(), any(), any());
            }
            Assertions.assertEquals("[(http://datasetIRI/d1001, http://www.w3.org/ns/dcat#distribution, http://distributionIRI/d1001) [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1001, http://rdf.insee.fr/def/base#validationState, \"Modified\") [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1001, http://purl.org/dc/terms/identifier, \"d1001\") [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1001, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.w3.org/ns/dcat#Distribution) [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1001, http://purl.org/dc/terms/title, \"labelLg1\"@fr) [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1001, http://purl.org/dc/terms/title, \"labelLg2\"@en) [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1001, http://purl.org/dc/terms/description, \"descriptionLg1\"@fr) [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1001, http://purl.org/dc/terms/description, \"descriptionLg2\"@en) [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1001, http://purl.org/dc/terms/created, \"2022-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1001, http://purl.org/dc/terms/modified, \"2023-10-19T11:44:23.33559\"^^<http://www.w3.org/2001/XMLSchema#dateTime>) [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1001, http://purl.org/dc/terms/format, \"format\") [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1001, http://www.w3.org/ns/dcat#byteSize, \"byteSize\") [http://rdf.insee.fr/graphes/datasetGraph/], (http://distributionIRI/d1001, http://www.w3.org/ns/dcat#downloadURL, \"url\") [http://rdf.insee.fr/graphes/datasetGraph/]]", model.getValue().toString());
            Assertions.assertEquals("d1001", id);
        }
    }

    @Test
    void shouldThrowAnExceptionIfTheBodyIsNotAJSONDuringCreation() {
        Assertions.assertThrows(RmesException.class, () -> distributionService.create(""));
    }

    @Test
    void shouldThrowAnExceptionIfTheBodyIsNotAJSONDuringUpdate() {
        Assertions.assertThrows(RmesException.class, () -> distributionService.update("d1000", ""));
    }

    @Test
    void shouldPublishADistribution() throws RmesException {
        IRI iri = SimpleValueFactory.getInstance().createIRI("http://distributionIRI/1");

        doNothing().when(publicationUtils).publishResource(iri, Set.of());
        String id = distributionService.publishDistribution("1");
        ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);

        verify(repositoryGestion, times(1)).objectValidation(eq(iri), model.capture());
        Assertions.assertEquals("[(http://distributionIRI/1, http://rdf.insee.fr/def/base#validationState, \"Validated\") [http://rdf.insee.fr/graphes/datasetGraph/]]", model.getValue().toString());
        Assertions.assertEquals("1", id);
    }

    @Test
    void getDistributionByID_shouldReturn404IfInexistentId() throws RmesException {
        JSONObject mockJSON = new JSONObject(EMPTY_JSON_OBJECT);
        when(repositoryGestion.getResponseAsObject(Mockito.anyString())).thenReturn(mockJSON);
        RmesException exception = assertThrows(RmesNotFoundException.class, () -> distributionService.getDistributionByID("1"));
        Assertions.assertEquals("{\"details\":\"Not found\",\"message\":\"This distribution does not exist\"}", exception.getDetails());
    }


    @Test
    void shouldPatchDistributionReturn400IfNoOneOfRequiredAttributes() throws RmesException {
        PatchDistribution patch = new PatchDistribution();
        JSONObject getDistrib = new JSONObject(DISTRIB_A_PATCHER);
        when(repositoryGestion.getResponseAsObject(Mockito.anyString())).thenReturn(getDistrib);
        RmesException exception = assertThrows(RmesBadRequestException.class, () ->distributionService.patchDistribution("d1004", patch));
        Assertions.assertEquals("{\"code\":1201,\"message\":\"One of these attributes is required : updated, byteSize or url\"}", exception.getDetails());
    }

    @Test
    void shouldPatchByteSizeDistribution() throws RmesException {
        IRI iri = SimpleValueFactory.getInstance().createIRI("http://distributionIRI/d1004");
        PatchDistribution patch = new PatchDistribution("2024-04-05T16:34:09.651166561", "5","http://test2");
        JSONObject getDistrib = new JSONObject(DISTRIB_A_PATCHER);
        when(repositoryGestion.getResponseAsObject(Mockito.anyString())).thenReturn(getDistrib);
        distributionService.patchDistribution("d1004", patch);
        ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);
        verify(repositoryGestion, times(1)).loadSimpleObject(eq(iri), model.capture(), any());
        Assertions.assertEquals(DISTRIB_PATCHEE,model.getValue().toString());
    }


    @Test
    void shouldNotDeleteNotUnpublishedDistributionAndReturn400() throws RmesException {
        JSONObject mockJSON = new JSONObject("""
                {
                  "id": "idTest",
                  "validationState": "Not Unpublished"
                }""");
        when(repositoryGestion.getResponseAsObject(Mockito.anyString())).thenReturn(mockJSON);
        RmesException exception = assertThrows(RmesBadRequestException.class, () -> distributionService.deleteDistributionId("idTest"));
        Assertions.assertEquals("{\"code\":1203,\"message\":\"Only unpublished distributions can be deleted\"}", exception.getDetails());

    }

    @Test
    void shouldDeleteDistribution() throws RmesException{
        JSONObject mockJSON = new JSONObject("{\"id\":\"idtest\",\"validationState\":\"Unpublished\"}");
        String stringDistributionIri = "http://bauhaus/catalogues/distribution/idtest";
        IRI distributionUri = RdfUtils.toURI(stringDistributionIri);
        try(
                MockedStatic<DatasetDistributionQueries> distributionQueriesMock = Mockito.mockStatic(DatasetDistributionQueries.class);
                MockedStatic<RdfUtils> rdfUtilsMock = Mockito.mockStatic(RdfUtils.class)
                )
        {
            distributionQueriesMock.when(() -> DatasetDistributionQueries.getDistribution(any(), any())).thenReturn("query1 ");
            when(repositoryGestion.getResponseAsObject("query1 ")).thenReturn(mockJSON);
            rdfUtilsMock.when(() -> RdfUtils.createIRI(any(String.class))).thenReturn(distributionUri);
            rdfUtilsMock.when(() -> RdfUtils.toURI(any(String.class))).thenReturn(distributionUri);

            // Capture the argument passed to deleteObject
            ArgumentCaptor<IRI> uriCaptor = ArgumentCaptor.forClass(IRI.class);
            distributionService.deleteDistributionId("idTest");
            verify(repositoryGestion, times(1)).deleteObject(uriCaptor.capture());
            Assertions.assertEquals(distributionUri, uriCaptor.getValue());
            verify(repositoryGestion, times(1)).deleteObject(distributionUri);
            verify(repositoryGestion, times(1)).deleteTripletByPredicate(any(IRI.class), eq(DCAT.DISTRIBUTION), any(IRI.class));
        }

    }



}
