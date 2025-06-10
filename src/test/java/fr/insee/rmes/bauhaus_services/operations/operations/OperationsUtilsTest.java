package fr.insee.rmes.bauhaus_services.operations.operations;

import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.persistance.sparql_queries.operations.operations.OperationsQueries;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.DeserializationFeature;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationsUtilsTest {
    @InjectMocks
    OperationsUtils operationsUtils;

    @Mock
    FamOpeSerIndUtils famOpeSerIndUtils;

    @Mock
    ParentUtils parentUtils;

    @Mock
    RepositoryGestion repositoryGestion;

    @Mock
    Config config;

    @Test
    void shouldCheckFromBodyIfObjectExists() throws RmesException, IOException {
        String body ="{\n" +
                "  \"id\": \"s1528\",\n" +
                "  \"prefLabelLg1\": \"Base non-salariés 2006\",\n" +
                "  \"prefLabelLg2\": \"Self-employed database 2006\",\n" +
                "  \"altLabelLg1\": null,\n" +
                "  \"altLabelLg2\": null,\n" +
                "  \"series\": {\n" +
                "    \"id\": \"s1037\",\n" +
                "    \"labelLg1\": \"Base non-salariés\",\n" +
                "    \"labelLg2\": \"Self-employed database\",\n" +
                "    \"creators\": [\n" +
                "      \"DG75-F240\"\n" +
                "    ]\n" +
                "  },\n" +
                "  \"idSims\": \"2124\",\n" +
                "  \"created\": null,\n" +
                "  \"modified\": \"2024-02-19T11:13:54.59554532\",\n" +
                "  \"validationState\": \"Modified\",\n" +
                "  \"year\": null\n" +
                "}";
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String id = famOpeSerIndUtils.createId();
        Operation operation = mapper.readValue(body, Operation.class);
        operation.setId(id);
        String idSeries= operation.getSeries().getId();
        boolean objectExist = !famOpeSerIndUtils.checkIfObjectExists(ObjectType.SERIES,idSeries);
        Assertions.assertTrue(objectExist);
    }

    @Test
    void shouldReturnAnExceptionWhenOneParameterIsNotPresentAtLeast() throws RmesException, IOException {
        String body ="{\n" + "\"id\": \"2025\",\n" + "\"creator\": \"creatorExample\"\n" + "}";
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String id = famOpeSerIndUtils.createId();
        Operation operation = mapper.readValue(body, Operation.class);
        operation.setId(id);
        RmesException exception = assertThrows(RmesBadRequestException.class, () -> operationsUtils.verifyBodyToCreateOperations(operation));
        assertThat(exception.getDetails()).contains("Required title not entered by user.");
    }


    @Test
    void shouldStoreYearProperty() throws RmesException {

        when(repositoryGestion.getResponseAsBoolean("unicity-labelLg1")).thenReturn(false);
        when(repositoryGestion.getResponseAsBoolean("unicity-labelLg2")).thenReturn(false);
        when(config.getLg1()).thenReturn("fr");
        when(config.getLg2()).thenReturn("en");
        when(famOpeSerIndUtils.createId()).thenReturn("1");
        when(famOpeSerIndUtils.checkIfObjectExists(ObjectType.SERIES, "2")).thenReturn(true);
        when(parentUtils.checkIfSeriesHasSims(anyString())).thenReturn(false);

        try (MockedStatic<RdfUtils> mockedFactory = Mockito.mockStatic(RdfUtils.class);
             MockedStatic<OperationsQueries> operationsQueriesMockedStatic = Mockito.mockStatic(OperationsQueries.class)
        ) {
            SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
            IRI operationIRI = valueFactory.createIRI("http://operation/2");
            mockedFactory.when(() -> RdfUtils.setLiteralInt(anyString())).thenCallRealMethod();
            mockedFactory.when(() -> RdfUtils.addTripleInt(any(), any(), any(), any(), any())).thenCallRealMethod();
            mockedFactory.when(() -> RdfUtils.addTripleString(any(), any(), any(), any(), any(), any())).thenCallRealMethod();
            mockedFactory.when(() -> RdfUtils.setLiteralString(anyString(), anyString())).thenCallRealMethod();
            mockedFactory.when(() -> RdfUtils.setLiteralString(anyString(), anyString())).thenCallRealMethod();
            mockedFactory.when(() -> RdfUtils.setLiteralString(anyString())).thenCallRealMethod();
            mockedFactory.when(RdfUtils::operationsGraph).thenReturn(valueFactory.createIRI("http://operations-graph/"));
            mockedFactory.when(() -> RdfUtils.createLiteral(anyString(), eq(XSD.GYEAR))).thenCallRealMethod();
            mockedFactory.when(() -> RdfUtils.operationsGraph()).thenReturn(valueFactory.createIRI("http://operations-graph/"));
            mockedFactory.when(() -> RdfUtils.objectIRI(eq(ObjectType.SERIES), eq("2"))).thenReturn(valueFactory.createIRI("http://series/2"));
            mockedFactory.when(() -> RdfUtils.objectIRI(eq(ObjectType.OPERATION), eq("1"))).thenReturn(operationIRI);
            operationsQueriesMockedStatic.when(() -> OperationsQueries.checkPrefLabelUnicity(eq("1"), eq("prefLabelLg1"), eq("fr"))).thenReturn("unicity-labelLg1");
            operationsQueriesMockedStatic.when(() -> OperationsQueries.checkPrefLabelUnicity(eq("1"), eq("prefLabelLg2"), eq("en"))).thenReturn("unicity-labelLg2");

            JSONObject operation = new JSONObject();
            JSONObject series = new JSONObject()
                    .put("id", "2");
            operation
                    .put("prefLabelLg1", "prefLabelLg1")
                    .put("prefLabelLg2", "prefLabelLg2")
                    .put("altLabelLg1", "altLabelLg1")
                    .put("altLabelLg2", "altLabelLg2")
                    .put("year", 2024)
                    .put("series", series);

            operationsUtils.setOperation(operation.toString());

            ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);

            verify(repositoryGestion, times(1)).loadSimpleObject(eq(operationIRI), model.capture());

            Assertions.assertEquals("[(http://operation/2, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://rdf.insee.fr/def/base#StatisticalOperation) [http://operations-graph/], (http://operation/2, http://www.w3.org/2004/02/skos/core#prefLabel, \"prefLabelLg1\"@fr) [http://operations-graph/], (http://operation/2, http://rdf.insee.fr/def/base#validationState, \"Unpublished\") [http://operations-graph/], (http://operation/2, http://www.w3.org/2004/02/skos/core#prefLabel, \"prefLabelLg2\"@en) [http://operations-graph/], (http://operation/2, http://www.w3.org/2004/02/skos/core#altLabel, \"altLabelLg1\"@fr) [http://operations-graph/], (http://operation/2, http://www.w3.org/2004/02/skos/core#altLabel, \"altLabelLg2\"@en) [http://operations-graph/], (http://operation/2, http://purl.org/dc/terms/temporal, \"2024\"^^<http://www.w3.org/2001/XMLSchema#gYear>) [http://operations-graph/]]", model.getValue().toString());

        }
    }
}