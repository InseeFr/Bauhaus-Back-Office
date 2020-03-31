package fr.insee.rmes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;
import org.openrdf.repository.RepositoryConnection;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryUtils;
import fr.insee.rmes.webservice.CodeListsResources;
import mockit.Mock;
import mockit.MockUp;
import mockit.Tested;

public class CodeListsResourcesTest {

    private final static String NOTATION = "213";
    
    @Tested
    private CodeListsResources codeListResource;

    
 	
    //getCodeListByNotation//
    
    @Test
    public void givengetCodeListByNotation_whenCorrectRequest_thenResponseIsOk() throws RmesException {
    	new MockUp<RepositoryUtils>() {
    		@Mock
    		public String executeQuery(RepositoryConnection c,String b) {
    			return "toto";
    		}
    	};


        Response response = codeListResource.getCodeListByNotation(NOTATION);
//    	new Verifications() {
//    
//    	}

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertEquals("EXPECTED_RESPONSE_GET_JSON", response.getEntity());
    }


}
