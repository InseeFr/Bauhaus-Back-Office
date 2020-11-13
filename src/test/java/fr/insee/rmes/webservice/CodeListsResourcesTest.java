package fr.insee.rmes.webservice;

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

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.code_list.CodeListServiceImpl;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesException;


class CodeListsResourcesTest {

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
    void givengetCodeListByNotation_whenCorrectRequest_thenResponseIsOk() throws RmesException {
    	when(repoGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject());
    	when(repoGestion.getResponseAsArray(anyString())).thenReturn(new JSONArray());
    	
        Response response = codeListResource.getCodeListByNotation(NOTATION);
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertEquals("{\"notation\":\"213\"}", response.getEntity());
    }


}
