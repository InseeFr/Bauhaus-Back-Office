package fr.insee.rmes.bauhaus_services.operations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.operations.ParentQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.documentations.DocumentationsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
    void shouldGetDocumentationOwnersByIdSims() throws RmesException {
        when(repoGestion.getResponseAsObject(DocumentationsQueries.getTargetByIdSims(id))).thenReturn(null);
        assertNull(parentUtils.getDocumentationOwnersByIdSims(id));
    }

    @Test
    void shouldThrowRmesExceptionWhenCheckIfParentIsASeriesWithOperations() throws RmesException {
        when(repoGestion.getResponseAsBoolean(ParentQueries.checkIfExists(anyString()))).thenReturn(true);
        when(repoGestion.getResponseAsBoolean(OpSeriesQueries.checkIfSeriesHasOperation(anyString()))).thenReturn(true);
        RmesException exception = assertThrows(RmesException.class, () -> parentUtils.checkIfParentIsASeriesWithOperations("id"));
        assertTrue(exception.getDetails().contains("Cannot create Sims for a series which already has operations"));
    }

}

