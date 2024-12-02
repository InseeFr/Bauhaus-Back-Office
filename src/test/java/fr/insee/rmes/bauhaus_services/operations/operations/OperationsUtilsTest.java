package fr.insee.rmes.bauhaus_services.operations.operations;

import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.operations.operations.OperationsQueries;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
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
    StampsRestrictionsService stampsRestrictionsService;

    @Mock
    RepositoryGestion repositoryGestion;

    @Mock
    Config config;

    @Test
    void shouldStoreYearProperty() throws RmesException {

        when(repositoryGestion.getResponseAsBoolean("unicity-labelLg1")).thenReturn(false);
        when(repositoryGestion.getResponseAsBoolean("unicity-labelLg2")).thenReturn(false);
        when(config.getLg1()).thenReturn("fr");
        when(config.getLg2()).thenReturn("en");
        when(famOpeSerIndUtils.createId()).thenReturn("1");
        when(famOpeSerIndUtils.checkIfObjectExists(ObjectType.SERIES, "2")).thenReturn(true);
        when(parentUtils.checkIfSeriesHasSims(anyString())).thenReturn(false);
        when(stampsRestrictionsService.canCreateOperation(any(IRI.class))).thenReturn(true);

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

            Assertions.assertEquals("[(http://operation/2, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://rdf.insee.fr/def/base#StatisticalOperation, http://operations-graph/) [http://operations-graph/], (http://operation/2, http://www.w3.org/2004/02/skos/core#prefLabel, \"prefLabelLg1\"@fr, http://operations-graph/) [http://operations-graph/], (http://operation/2, http://rdf.insee.fr/def/base#validationState, \"Unpublished\", http://operations-graph/) [http://operations-graph/], (http://operation/2, http://www.w3.org/2004/02/skos/core#prefLabel, \"prefLabelLg2\"@en, http://operations-graph/) [http://operations-graph/], (http://operation/2, http://www.w3.org/2004/02/skos/core#altLabel, \"altLabelLg1\"@fr, http://operations-graph/) [http://operations-graph/], (http://operation/2, http://www.w3.org/2004/02/skos/core#altLabel, \"altLabelLg2\"@en, http://operations-graph/) [http://operations-graph/], (http://operation/2, http://purl.org/dc/terms/temporal, \"2024\"^^<http://www.w3.org/2001/XMLSchema#int>, http://operations-graph/) [http://operations-graph/]]", model.getValue().toString());

        }


    }
}