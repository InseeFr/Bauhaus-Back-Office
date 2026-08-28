package fr.insee.rmes.bauhaus_services.operations;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.utils.OrganisationLookup;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.operations.msd.infrastructure.graphdb.DocumentationQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationIndicatorsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.ParentQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationsOperationQueries;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationsParentRepositoryTest {

    @InjectMocks
    OperationsParentRepository operationsParentRepository;

    @Mock
    RepositoryGestion repoGestion;

    @Mock
    OperationIndicatorsQueries operationIndicatorsQueries;

    @Mock
    DocumentationQueries documentationQueries;

    @Mock
    ParentQueries parentQueries;

    @Mock
    OperationQueries operationQueries;

    @Mock
    OperationSeriesQueries operationSeriesQueries;

    @Mock
    OperationsOperationQueries operationsOperationQueries;

    @Mock
    OrganisationLookup organisationLookup;

    String id = "2025";

    @Test
    void shouldThrowRmesExceptionWhenGetDocumentationOwnersByIdSims() throws RmesException {
        JSONObject jsonObject = new JSONObject().put(Constants.ID_OPERATION,"").put(Constants.ID_SERIES,"").put(Constants.ID_INDICATOR,"");
        when(documentationQueries.getTargetByIdSims(id)).thenReturn("mock-target-query");
        when(repoGestion.getResponseAsObject("mock-target-query")).thenReturn(jsonObject);
        RmesException exception = assertThrows(RmesException.class, () -> operationsParentRepository.getDocumentationOwnersByIdSims(id));
        assertTrue(exception.getDetails().contains("Documentation has no target"));
    }

    @Test
    void shouldCheckIfParentExists() throws RmesException {
        when(parentQueries.checkIfExists("uriParent")).thenReturn("mock-parent-exists-query");
        when(repoGestion.getResponseAsBoolean("mock-parent-exists-query")).thenReturn(true);
        operationsParentRepository.checkIfParentExists("uriParent");
    }

    @Test
    void shouldGetDocumentationOwnersByIdSims() throws RmesException {
        when(documentationQueries.getTargetByIdSims(id)).thenReturn("mock-target-query");
        when(repoGestion.getResponseAsObject("mock-target-query")).thenReturn(null);
        assertNull(operationsParentRepository.getDocumentationOwnersByIdSims(id));
    }

    @Test
    void getIndicatorCreators_canonicalisesLegacyStampsToIris() throws RmesException {
        var raw = new JSONArray().put("stamp");
        var canonicalized = new JSONArray().put("http://bauhaus/organisations/stamp");
        when(operationIndicatorsQueries.getCreatorsById(id)).thenReturn("query");
        when(repoGestion.getResponseAsJSONList("query")).thenReturn(raw);
        when(organisationLookup.canonicalize(raw)).thenReturn(canonicalized);

        assertEquals(canonicalized, operationsParentRepository.getIndicatorCreators(id));
    }

    @Test
    void shouldGetSeriesCreatorsWithIri() throws RmesException {
        when(operationSeriesQueries.getCreatorsById(id)).thenReturn("mock-query");
        when(repoGestion.getResponseAsJSONList("mock-query")).thenReturn(null);
        assertNull(operationsParentRepository.getSeriesCreators(id));
    }

    @Test
    void shouldGetDocumentationTargetTypeAndId() throws RmesException {
        JSONObject jsonObject = new JSONObject().put(Constants.ID_OPERATION,"A");
        when(documentationQueries.getTargetByIdSims("idSims")).thenReturn("mock-target-query");
        when(repoGestion.getResponseAsObject("mock-target-query")).thenReturn(jsonObject);
        String actual = Arrays.toString(operationsParentRepository.getDocumentationTargetTypeAndId("idSims"));
        assertEquals("[OPERATION, A]",actual);
    }

    @Test
    void shouldGetDocumentationTargetTypeAndWhenOneJsonKeysNull() throws RmesException {
        JSONObject jsonObject = new JSONObject().put(Constants.ID_OPERATION,"").put(Constants.ID_SERIES,"A");
        when(documentationQueries.getTargetByIdSims("idSims")).thenReturn("mock-target-query");
        when(repoGestion.getResponseAsObject("mock-target-query")).thenReturn(jsonObject);
        String actual = Arrays.toString(operationsParentRepository.getDocumentationTargetTypeAndId("idSims"));
        assertEquals("[SERIES, A]",actual);
    }

    @Test
    void shouldGetDocumentationTargetTypeAndWhenTwoJsonKeysNull() throws RmesException {
        JSONObject jsonObject = new JSONObject().put(Constants.ID_OPERATION,"").put(Constants.ID_SERIES,"").put(Constants.ID_INDICATOR,"A").put(Constants.INDICATOR_UP,"B");
        when(documentationQueries.getTargetByIdSims("idSims")).thenReturn("mock-target-query");
        when(repoGestion.getResponseAsObject("mock-target-query")).thenReturn(jsonObject);
        String actual = Arrays.toString(operationsParentRepository.getDocumentationTargetTypeAndId("idSims"));
        assertEquals("[INDICATOR, A]",actual);
    }

    @Test
    void shouldGetDocumentationTargetTypeAndWhenAllJsonKeysNull() throws RmesException {
        JSONObject jsonObject = null;
        when(documentationQueries.getTargetByIdSims("idSims")).thenReturn("mock-target-query");
        when(repoGestion.getResponseAsObject("mock-target-query")).thenReturn(jsonObject);
        String actual = Arrays.toString(operationsParentRepository.getDocumentationTargetTypeAndId("idSims"));
        assertEquals("[null, null]",actual);
    }
}