package fr.insee.rmes.bauhaus_services.operations.operations;

import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.UriUtils;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.graphdb.ontologies.ADMS;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationsOperationQueries;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationsUtilsTest {
    @InjectMocks
    OperationsUtils operationsUtils;

    @Spy
    BauhausLanguagesProperties languages = new BauhausLanguagesProperties("fr", "en");

    @Mock
    FamOpeSerIndUtils famOpeSerIndUtils;

    @Mock
    ParentUtils parentUtils;


    @Mock
    RepositoryGestion repositoryGestion;


    @Mock
    OperationsOperationQueries operationsOperationQueries;

    @BeforeAll
    static void initRdfUtils() {
        RdfUtils.setGraphs(GraphsPropertiesStub.stub());
        RdfUtils.setUriUtils(new UriUtils("http://bauhaus/publication/", "http://bauhaus/", p -> Optional.of("/operations")));
    }

    @Test
    void createRdfOperation_addsAdmsIdentifierTriple() throws RmesException {
        when(repositoryGestion.getResponseAsBoolean(any())).thenReturn(false);
        Operation operation = new Operation("o1500");
        operation.setPrefLabelLg1("Opération de test");

        operationsUtils.createRdfOperation(operation, null, ValidationStatus.UNPUBLISHED);

        ArgumentCaptor<Model> captor = ArgumentCaptor.forClass(Model.class);
        verify(repositoryGestion).loadSimpleObject(any(), captor.capture());
        IRI operationURI = RdfUtils.objectIRI(ObjectType.OPERATION, "o1500");
        assertThat(captor.getValue().filter(operationURI, ADMS.HAS_IDENTIFIER, null).objects())
                .containsExactly(SimpleValueFactory.getInstance().createLiteral("o1500"));
    }

    @Test
    void shouldStoreYearProperty() throws RmesException {

        when(repositoryGestion.getResponseAsBoolean("unicity-labelLg1")).thenReturn(false);
        when(repositoryGestion.getResponseAsBoolean("unicity-labelLg2")).thenReturn(false);
        when(famOpeSerIndUtils.createId()).thenReturn("1");
        when(famOpeSerIndUtils.checkIfObjectExists(ObjectType.SERIES, "2")).thenReturn(true);

        when(operationsOperationQueries.checkPrefLabelUnicity(eq("1"), eq("prefLabelLg1"), eq("fr"))).thenReturn("unicity-labelLg1");
        when(operationsOperationQueries.checkPrefLabelUnicity(eq("1"), eq("prefLabelLg2"), eq("en"))).thenReturn("unicity-labelLg2");

        try (MockedStatic<RdfUtils> mockedFactory = Mockito.mockStatic(RdfUtils.class)) {
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
            mockedFactory.when(RdfUtils::operationsGraph).thenReturn(valueFactory.createIRI("http://operations-graph/"));
            mockedFactory.when(() -> RdfUtils.objectIRI(eq(ObjectType.SERIES), eq("2"))).thenReturn(valueFactory.createIRI("http://series/2"));
            mockedFactory.when(() -> RdfUtils.objectIRI(eq(ObjectType.OPERATION), eq("1"))).thenReturn(operationIRI);
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

            Assertions.assertEquals("[(http://operation/2, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://rdf.insee.fr/def/base#StatisticalOperation) [http://operations-graph/], (http://operation/2, http://www.w3.org/ns/adms#identifier, \"1\") [http://operations-graph/], (http://operation/2, http://www.w3.org/2004/02/skos/core#prefLabel, \"prefLabelLg1\"@fr) [http://operations-graph/], (http://operation/2, http://rdf.insee.fr/def/base#validationState, \"Unpublished\") [http://operations-graph/], (http://operation/2, http://www.w3.org/2004/02/skos/core#prefLabel, \"prefLabelLg2\"@en) [http://operations-graph/], (http://operation/2, http://www.w3.org/2004/02/skos/core#altLabel, \"altLabelLg1\"@fr) [http://operations-graph/], (http://operation/2, http://www.w3.org/2004/02/skos/core#altLabel, \"altLabelLg2\"@en) [http://operations-graph/], (http://operation/2, http://purl.org/dc/terms/temporal, \"2024\"^^<http://www.w3.org/2001/XMLSchema#gYear>) [http://operations-graph/]]", model.getValue().toString());

        }


    }

    @Test
    void setOperation_shouldNotRejectWith406_whenSeriesAlreadyHasSims() throws RmesException {
        // Une série déjà documentée par un SIMS doit pouvoir accueillir une opération (cf. ticket #1452).
        when(famOpeSerIndUtils.createId()).thenReturn("1");
        when(famOpeSerIndUtils.checkIfObjectExists(ObjectType.SERIES, "2")).thenReturn(true);

        try (MockedStatic<RdfUtils> mockedFactory = Mockito.mockStatic(RdfUtils.class)) {
            SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
            mockedFactory.when(() -> RdfUtils.objectIRI(eq(ObjectType.SERIES), eq("2")))
                    .thenReturn(valueFactory.createIRI("http://series/2"));
            mockedFactory.when(() -> RdfUtils.objectIRI(eq(ObjectType.OPERATION), eq("1")))
                    .thenReturn(valueFactory.createIRI("http://operation/1"));
            mockedFactory.when(RdfUtils::operationsGraph)
                    .thenReturn(valueFactory.createIRI("http://operations-graph/"));
            mockedFactory.when(() -> RdfUtils.setLiteralString(anyString())).thenCallRealMethod();
            mockedFactory.when(() -> RdfUtils.setLiteralString(anyString(), anyString())).thenCallRealMethod();
            mockedFactory.when(() -> RdfUtils.addTripleString(any(), any(), any(), any(), any(), any())).thenCallRealMethod();

            JSONObject series = new JSONObject().put("id", "2");
            JSONObject operation = new JSONObject()
                    .put("prefLabelLg1", "prefLabelLg1")
                    .put("prefLabelLg2", "prefLabelLg2")
                    .put("series", series);

            try {
                operationsUtils.setOperation(operation.toString());
            } catch (RmesNotAcceptableException e) {
                if (e.getDetails().contains("A series cannot have both a Sims and Operation(s)")) {
                    fail("La création d'une opération sur une série avec SIMS ne devrait plus lever 406 : " + e.getDetails());
                }
            } catch (Exception ignored) {
                // d'autres exceptions sont attendues car les mocks ne couvrent pas tout le flow
            }
        }
    }
}