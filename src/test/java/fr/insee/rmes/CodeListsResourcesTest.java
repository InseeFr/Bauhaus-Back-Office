package fr.insee.rmes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.webservice.CodeListsResources;

public class CodeListsResourcesTest {

    private final static String NOTATION = "213";
    
    private static CodeListsResources codeListResource;
    
 	@Mock
 	TupleQuery tq;

    @BeforeEach
    public void init() {
  	 	codeListResource = new CodeListsResources();
        MockitoAnnotations.initMocks(this);
    }

    //getCodeListByNotation//
 	
 	//tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
	//tupleQuery.evaluate(new SPARQLResultsJSONWriter(stream));
    
    @Test
    public void givengetCodeListByNotation_whenCorrectRequest_thenResponseIsOk() throws RmesException, TupleQueryResultHandlerException, QueryEvaluationException {
    	
    	Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				SPARQLResultsJSONWriter result = (SPARQLResultsJSONWriter) invocation.getArguments()[0];
				
	    		return null;
			}
    	}).when(tq).evaluate(any());
    	
    	
    	
//    	try {
//			Mockito.doNothing().when(tq).evaluate(any());
//		} catch (TupleQueryResultHandlerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (QueryEvaluationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	        Response response = codeListResource.getCodeListByNotation(NOTATION);
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertEquals("EXPECTED_RESPONSE_GET_JSON", response.getEntity());
    }


}
