package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.bauhaus_services.keycloak.KeycloakServices;
import fr.insee.rmes.exceptions.RmesException;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.http.HTTPUpdateExecutionException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Optional;
import static fr.insee.rmes.bauhaus_services.Constants.VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.rdf4j.query.resultio.sparqljson.AbstractSPARQLJSONParser.BINDINGS;
import static org.eclipse.rdf4j.query.resultio.sparqljson.AbstractSPARQLJSONParser.RESULTS;
import static org.junit.jupiter.api.Assertions.*;

class RepositoryUtilsTest {

    @Mock
    KeycloakServices keycloakServices;
    Resource structure;

    RepositoryUtils repositoryUtils = new RepositoryUtils(keycloakServices,RepositoryInitiator.Type.ENABLED);
    Repository repositoryHTTP = new HTTPRepository("serverURL",null);
    String updateQuery="updateQuery";


    public JSONObject createTestJsonObject(){
        JSONObject river = new JSONObject();
        river.put(VALUE, "Tamise").put("length","unknown");

        JSONObject monument = new JSONObject();
        monument.put(VALUE, "Tower of London").put("high","unknown");

        JSONObject description = new JSONObject();
        description.put("monument", monument).put("river",river);

        JSONArray countries = new JSONArray();
        countries.put(description);

        JSONObject world = new JSONObject();
        world.put(BINDINGS,countries);

        JSONObject jsonSparql = new JSONObject();
        jsonSparql.put(RESULTS, world);

        return jsonSparql;

    }

    @Test
    void shouldReturnResultArrayValuesFromSparqlJSON () {
        JSONObject jsonSparql = createTestJsonObject();
        JSONArray actual = RepositoryUtils.sparqlJSONToResultArrayValues(jsonSparql);
        boolean actualCorrectValue = "[{\"monument\":\"Tower of London\",\"river\":\"Tamise\"}]".equals(actual.toString());
        Assertions.assertTrue(actualCorrectValue);
    }

    @Test
    void shouldReturnResultListValuesFromSparqlJSON () {
        JSONObject jsonSparql = createTestJsonObject();
        JSONArray actual = RepositoryUtils.sparqlJSONToResultListValues(jsonSparql);
        boolean actualCorrectValue = "[\"Tower of London\",\"Tamise\"]".equals(actual.toString());
        Assertions.assertTrue(actualCorrectValue);
    }

    @Test
    void shouldNotInitRepository(){
        Repository actualRepositoryNull = repositoryUtils.initRepository(null,"repositoryID");
        Repository actualRepositoryEmpty = repositoryUtils.initRepository("","repositoryID");
        Repository actualRepositoryCompleted= repositoryUtils.initRepository("rdfServer",null);
        Assertions.assertTrue(actualRepositoryNull==null && actualRepositoryEmpty==null && actualRepositoryCompleted==null);
    }

    @Test
    void shouldGetConnection() throws RmesException {
        RepositoryConnection actualGetConnection =repositoryUtils.getConnection(repositoryHTTP);
        Assertions.assertNotNull(actualGetConnection);
    }

    @Test
    void shouldNotExecuteUpdate() throws RmesException {
        Repository repository = null;
        HttpStatus actualHttpStatus =RepositoryUtils.executeUpdate(updateQuery,repository);
        Assertions.assertEquals(HttpStatus.EXPECTATION_FAILED,actualHttpStatus);
    }

    @Test
    void shouldThrowAnExceptionWhenExecuteUpdate() {
        HTTPUpdateExecutionException exception = assertThrows(HTTPUpdateExecutionException.class, () ->  RepositoryUtils.executeUpdate(updateQuery,repositoryHTTP));
        Assertions.assertTrue(exception.toString().contains("could not read protocol version from server"));
    }

    @Test
    void shouldThrowAnRmesExceptionWhenGetResponse() {
        RmesException exception = assertThrows(RmesException.class, () -> RepositoryUtils.getResponse(updateQuery,repositoryHTTP));
        assertThat(exception.getDetails()).contains("Execute query failed :");
    }

    @Test
    void shouldThrowAnRmesExceptionWhenGetResponseForAskQuery()  {
        RmesException exception = assertThrows(RmesException.class, () -> RepositoryUtils.getResponseForAskQuery(updateQuery,repositoryHTTP));
        assertThat(exception.getDetails()).contains("Execute query failed :");
    }

    @Test
    void shouldConvertToArrayValuesWhenSparqlJSONIsNull(){
        String fakeValue=null;
        JSONObject jsonActual = new JSONObject().put("Result", "ResultExample").put(RESULTS,fakeValue);
        JSONException exception = assertThrows(JSONException.class, () -> jsonActual.get(RESULTS));
        JSONObject jsonImproved = new JSONObject().put("Result", "ResultExample").put(RESULTS,Optional.ofNullable(null));

        Boolean jsonActualCanContainResult =  jsonActual.keySet().contains(RESULTS);
        Boolean jsonActualResultKeyContentBeNull = exception.getMessage().contains("JSONObject[\"results\"] not found");
        Boolean jsonImprovedRespectStandards = jsonImproved.keySet().contains(RESULTS) && jsonImproved.get(RESULTS)==null;

        assertEquals(List.of(false,false,true),List.of(jsonImprovedRespectStandards,jsonActualCanContainResult,jsonActualResultKeyContentBeNull));
    }

    @Test
    void shouldThrowRmesExceptionWhenClearStructureAndComponents(){
        RmesException exception = assertThrows(RmesException.class, () -> RepositoryUtils.clearStructureAndComponents(structure,repositoryHTTP));
        assertThat(exception.getDetails()).contains("Failure deletion : ");
    }

}