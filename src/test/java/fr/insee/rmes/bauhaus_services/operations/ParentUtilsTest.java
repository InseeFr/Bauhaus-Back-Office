package fr.insee.rmes.bauhaus_services.operations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.operations.ParentQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.documentations.DocumentationsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.indicators.IndicatorsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.base.InternedIRI;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(properties = { "fr.insee.rmes.bauhaus.lg1=fr", "fr.insee.rmes.bauhaus.lg2=en"})
class ParentUtilsTest {

    @InjectMocks
    ParentUtils parentUtils = new ParentUtils();

    @MockitoBean
    RepositoryGestion repoGestion;

    String id = "2025";

    @Test
    void shouldThrowRmesExceptionWhenGetDocumentationOwnersByIdSims() throws RmesException {
        JSONObject jsonObject = new JSONObject().put(Constants.ID_OPERATION,"").put(Constants.ID_SERIES,"").put(Constants.ID_INDICATOR,"");
        when(repoGestion.getResponseAsObject(DocumentationsQueries.getTargetByIdSims(id))).thenReturn(jsonObject);
        RmesException exception = assertThrows(RmesException.class, () -> parentUtils.getDocumentationOwnersByIdSims(id));
        assertTrue(exception.getDetails().contains("Documentation has no target"));
    }

    @Test
    void shouldCheckIfSeriesHasSims() throws RmesException {
        when(repoGestion.getResponseAsBoolean(OpSeriesQueries.checkIfSeriesHasSims("uriSeries"))).thenReturn(true);
        parentUtils.checkIfSeriesHasSims( "uriSeries");
    }

    @Test
    void shouldCheckIfParentExists() throws RmesException {
        when(repoGestion.getResponseAsBoolean(ParentQueries.checkIfExists("uriParent"))).thenReturn(true);
        parentUtils.checkIfParentExists( "uriParent");
    }

    @Test
    void shouldGetDocumentationOwnersByIdSims() throws RmesException {
        when(repoGestion.getResponseAsObject(DocumentationsQueries.getTargetByIdSims(id))).thenReturn(null);
        assertNull(parentUtils.getDocumentationOwnersByIdSims(id));
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
        IRI iri = new InternedIRI("namespace", "localName");
        when(repoGestion.getResponseAsJSONList(OpSeriesQueries.getCreatorsBySeriesUri(RdfUtils.toString(iri)))).thenReturn(null);
        assertNull(parentUtils.getSeriesCreators(id));
    }

    @Test
    void shouldThrowRmesExceptionWhenCheckIfParentIsASeriesWithOperations() throws RmesException {
        when(repoGestion.getResponseAsBoolean(ParentQueries.checkIfExists(anyString()))).thenReturn(true);
        when(repoGestion.getResponseAsBoolean(OpSeriesQueries.checkIfSeriesHasOperation(anyString()))).thenReturn(true);
        RmesException exception = assertThrows(RmesException.class, () -> parentUtils.checkIfParentIsASeriesWithOperations("id"));
        assertTrue(exception.getDetails().contains("Cannot create Sims for a series which already has operations"));
    }

    @Test
    void shouldGetDocumentationTargetTypeAndId() throws RmesException {
        JSONObject jsonObject = new JSONObject().put(Constants.ID_OPERATION,"A");
        when(repoGestion.getResponseAsObject(DocumentationsQueries.getTargetByIdSims("idSims"))).thenReturn(jsonObject);
        String actual = Arrays.toString(parentUtils.getDocumentationTargetTypeAndId("idSims"));
        assertEquals("[OPERATION, A]",actual);
    }

    @Test
    void shouldGetDocumentationTargetTypeAndWhenOneJsonKeysNull() throws RmesException {
        JSONObject jsonObject = new JSONObject().put(Constants.ID_OPERATION,"").put(Constants.ID_SERIES,"A");
        when(repoGestion.getResponseAsObject(DocumentationsQueries.getTargetByIdSims("idSims"))).thenReturn(jsonObject);
        String actual = Arrays.toString(parentUtils.getDocumentationTargetTypeAndId("idSims"));
        assertEquals("[SERIES, A]",actual);
    }

    @Test
    void shouldGetDocumentationTargetTypeAndWhenTwoJsonKeysNull() throws RmesException {
        JSONObject jsonObject = new JSONObject().put(Constants.ID_OPERATION,"").put(Constants.ID_SERIES,"").put(Constants.ID_INDICATOR,"A").put(Constants.INDICATOR_UP,"B");
        when(repoGestion.getResponseAsObject(DocumentationsQueries.getTargetByIdSims("idSims"))).thenReturn(jsonObject);
        String actual = Arrays.toString(parentUtils.getDocumentationTargetTypeAndId("idSims"));
        assertEquals("[INDICATOR, A]",actual);
    }

    @Test
    void shouldGetDocumentationTargetTypeAndWhenAllJsonKeysNull() throws RmesException {
        JSONObject jsonObject =null;
        when(repoGestion.getResponseAsObject(DocumentationsQueries.getTargetByIdSims("idSims"))).thenReturn(jsonObject);
        String actual = Arrays.toString(parentUtils.getDocumentationTargetTypeAndId("idSims"));
        assertEquals("[null, null]",actual);
    }
}

