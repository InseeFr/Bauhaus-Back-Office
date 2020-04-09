package fr.insee.rmes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.CodeListService;
import fr.insee.rmes.persistance.service.sesame.code_list.CodeListServiceImpl;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.webservice.CodeListsResources;


public class CodeListsResourcesTest {

    private final static String NOTATION = "213";
        
    @InjectMocks //CLASS TO TEST
    private CodeListsResources codeListResource;
    
    @Mock
 	RepositoryGestion repoGestion;
 	
 	//Spy  -> Normal class, with Mock inside (repoGestion)
 	@InjectMocks
 	CodeListService codeListService ;
 	


    @BeforeEach
    public void init() {
    	codeListService = Mockito.spy(new CodeListServiceImpl());
        MockitoAnnotations.initMocks(this);
    }

    //getCodeListByNotation//

    @Test
    public void givengetCodeListByNotation_whenCorrectRequest_thenResponseIsOk() throws RmesException, TupleQueryResultHandlerException, QueryEvaluationException {
    	when(repoGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject());
    	when(repoGestion.getResponseAsArray(anyString())).thenReturn(new JSONArray());
    	
        Response response = codeListResource.getCodeListByNotation(NOTATION);
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"notation\":\"213\"}", response.getEntity());
    }


}
