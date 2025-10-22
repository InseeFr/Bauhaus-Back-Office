package fr.insee.rmes.bauhaus_services.operations;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.onion.infrastructure.graphdb.operations.queries.DocumentationQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.ParentQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.indicators.IndicatorsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@AppSpringBootTest
class ParentUtilsTest {

    @InjectMocks
    ParentUtils parentUtils = new ParentUtils();

    @MockitoBean
    RepositoryGestion repoGestion;

    String id = "2025";

    @Test
    void shouldThrowRmesExceptionWhenGetDocumentationOwnersByIdSims() throws RmesException {
        JSONObject jsonObject = new JSONObject().put(Constants.ID_OPERATION,"").put(Constants.ID_SERIES,"").put(Constants.ID_INDICATOR,"");
        try (MockedStatic<DocumentationQueries> mockedQueries = Mockito.mockStatic(DocumentationQueries.class)) {
            mockedQueries.when(() -> DocumentationQueries.getTargetByIdSims(id)).thenReturn("mock-target-query");
            when(repoGestion.getResponseAsObject("mock-target-query")).thenReturn(jsonObject);
            RmesException exception = assertThrows(RmesException.class, () -> parentUtils.getDocumentationOwnersByIdSims(id));
            assertTrue(exception.getDetails().contains("Documentation has no target"));
        }
    }

    @Test
    void shouldCheckIfSeriesHasSims() throws RmesException {
        try (MockedStatic<OpSeriesQueries> mockedQueries = Mockito.mockStatic(OpSeriesQueries.class)) {
            mockedQueries.when(() -> OpSeriesQueries.checkIfSeriesHasSims("uriSeries")).thenReturn("mock-sims-query");
            when(repoGestion.getResponseAsBoolean("mock-sims-query")).thenReturn(true);
            parentUtils.checkIfSeriesHasSims("uriSeries");
        }
    }

    @Test
    void shouldCheckIfParentExists() throws RmesException {
        try (MockedStatic<ParentQueries> mockedQueries = Mockito.mockStatic(ParentQueries.class)) {
            mockedQueries.when(() -> ParentQueries.checkIfExists("uriParent")).thenReturn("mock-parent-exists-query");
            when(repoGestion.getResponseAsBoolean("mock-parent-exists-query")).thenReturn(true);
            parentUtils.checkIfParentExists("uriParent");
        }
    }

    @Test
    void shouldGetDocumentationOwnersByIdSims() throws RmesException {
        try (MockedStatic<DocumentationQueries> mockedQueries = Mockito.mockStatic(DocumentationQueries.class)) {
            mockedQueries.when(() -> DocumentationQueries.getTargetByIdSims(id)).thenReturn("mock-target-query");
            when(repoGestion.getResponseAsObject("mock-target-query")).thenReturn(null);
            assertNull(parentUtils.getDocumentationOwnersByIdSims(id));
        }
    }

    @Test
    void shouldGetIndicatorCreators() throws RmesException {
        var creators = new JSONArray().put(new JSONObject("creators", "stamp"));
        try (MockedStatic<IndicatorsQueries> mockedFactory = Mockito.mockStatic(IndicatorsQueries.class)) {
            mockedFactory.when(() -> IndicatorsQueries.getCreatorsById(id)).thenReturn("query");
            when(repoGestion.getResponseAsJSONList("query")).thenReturn(creators);
            assertEquals(creators, parentUtils.getIndicatorCreators(id));
        }
    }

    @Test
    void shouldGetSeriesCreatorsWithIri() throws RmesException {
        try (MockedStatic<OpSeriesQueries> mockedQueries = Mockito.mockStatic(OpSeriesQueries.class)) {
            mockedQueries.when(() -> OpSeriesQueries.getCreatorsById(id)).thenReturn("mock-query");
            when(repoGestion.getResponseAsJSONList("mock-query")).thenReturn(null);
            assertNull(parentUtils.getSeriesCreators(id));
        }
    }

    @Test
    void shouldThrowRmesExceptionWhenCheckIfParentIsASeriesWithOperations() throws RmesException {
        String testId = "id";
        String uriParent = "http://bauhaus/operations/series/id";
        ValueFactory factory = SimpleValueFactory.getInstance();
        IRI seriesIRI = factory.createIRI(uriParent);
        
        try (MockedStatic<RdfUtils> mockedRdfUtils = Mockito.mockStatic(RdfUtils.class);
             MockedStatic<ParentQueries> mockedParentQueries = Mockito.mockStatic(ParentQueries.class);
             MockedStatic<OpSeriesQueries> mockedOpSeriesQueries = Mockito.mockStatic(OpSeriesQueries.class)) {
            
            mockedRdfUtils.when(() -> RdfUtils.objectIRI(ObjectType.SERIES, testId)).thenReturn(seriesIRI);
            mockedRdfUtils.when(() -> RdfUtils.toString(seriesIRI)).thenReturn(uriParent);
            
            mockedParentQueries.when(() -> ParentQueries.checkIfExists(uriParent)).thenReturn("mock-parent-query");
            mockedOpSeriesQueries.when(() -> OpSeriesQueries.checkIfSeriesHasOperation(uriParent)).thenReturn("mock-operation-query");
            
            when(repoGestion.getResponseAsBoolean("mock-parent-query")).thenReturn(true);
            when(repoGestion.getResponseAsBoolean("mock-operation-query")).thenReturn(true);
            
            RmesException exception = assertThrows(RmesException.class, () -> parentUtils.checkIfParentIsASeriesWithOperations(testId));
            assertTrue(exception.getDetails().contains("Cannot create Sims for a series which already has operations"));
        }
    }

    @Test
    void shouldGetDocumentationTargetTypeAndId() throws RmesException {
        JSONObject jsonObject = new JSONObject().put(Constants.ID_OPERATION,"A");
        try (MockedStatic<DocumentationQueries> mockedQueries = Mockito.mockStatic(DocumentationQueries.class)) {
            mockedQueries.when(() -> DocumentationQueries.getTargetByIdSims("idSims")).thenReturn("mock-target-query");
            when(repoGestion.getResponseAsObject("mock-target-query")).thenReturn(jsonObject);
            String actual = Arrays.toString(parentUtils.getDocumentationTargetTypeAndId("idSims"));
            assertEquals("[OPERATION, A]",actual);
        }
    }

    @Test
    void shouldGetDocumentationTargetTypeAndWhenOneJsonKeysNull() throws RmesException {
        JSONObject jsonObject = new JSONObject().put(Constants.ID_OPERATION,"").put(Constants.ID_SERIES,"A");
        try (MockedStatic<DocumentationQueries> mockedQueries = Mockito.mockStatic(DocumentationQueries.class)) {
            mockedQueries.when(() -> DocumentationQueries.getTargetByIdSims("idSims")).thenReturn("mock-target-query");
            when(repoGestion.getResponseAsObject("mock-target-query")).thenReturn(jsonObject);
            String actual = Arrays.toString(parentUtils.getDocumentationTargetTypeAndId("idSims"));
            assertEquals("[SERIES, A]",actual);
        }
    }

    @Test
    void shouldGetDocumentationTargetTypeAndWhenTwoJsonKeysNull() throws RmesException {
        JSONObject jsonObject = new JSONObject().put(Constants.ID_OPERATION,"").put(Constants.ID_SERIES,"").put(Constants.ID_INDICATOR,"A").put(Constants.INDICATOR_UP,"B");
        try (MockedStatic<DocumentationQueries> mockedQueries = Mockito.mockStatic(DocumentationQueries.class)) {
            mockedQueries.when(() -> DocumentationQueries.getTargetByIdSims("idSims")).thenReturn("mock-target-query");
            when(repoGestion.getResponseAsObject("mock-target-query")).thenReturn(jsonObject);
            String actual = Arrays.toString(parentUtils.getDocumentationTargetTypeAndId("idSims"));
            assertEquals("[INDICATOR, A]",actual);
        }
    }

    @Test
    void shouldGetDocumentationTargetTypeAndWhenAllJsonKeysNull() throws RmesException {
        JSONObject jsonObject = null;
        try (MockedStatic<DocumentationQueries> mockedQueries = Mockito.mockStatic(DocumentationQueries.class)) {
            mockedQueries.when(() -> DocumentationQueries.getTargetByIdSims("idSims")).thenReturn("mock-target-query");
            when(repoGestion.getResponseAsObject("mock-target-query")).thenReturn(jsonObject);
            String actual = Arrays.toString(parentUtils.getDocumentationTargetTypeAndId("idSims"));
            assertEquals("[null, null]",actual);
        }
    }
}

