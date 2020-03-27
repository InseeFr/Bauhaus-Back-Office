package fr.insee.rmes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryUtils;
import fr.insee.rmes.webservice.CodeListsResources;

@RunWith(PowerMockRunner.class)
@PrepareForTest( RepositoryUtils.class )
public class CodeListsResourcesTest {

    private final static String NOTATION = "213";
    
    private CodeListsResources codeListResource;
    
 	@Before
 	public void setUp() {
 		codeListResource = new CodeListsResources();
 	}


//    @Before
//    public void init() {
//    	
//        MockitoAnnotations.initMocks(this);
//    }

    //getCodeListByNotation//
    
    @Test
    @Ignore
    public void givengetCodeListByNotation_whenCorrectRequest_thenResponseIsOk() throws RmesException {
    	PowerMockito.mockStatic(RepositoryUtils.class);		
    when(RepositoryUtils.executeQuery(any(),anyString())).thenReturn("toto");
	        Response response = codeListResource.getCodeListByNotation(NOTATION);
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertEquals("EXPECTED_RESPONSE_GET_JSON", response.getEntity());
    }


}
