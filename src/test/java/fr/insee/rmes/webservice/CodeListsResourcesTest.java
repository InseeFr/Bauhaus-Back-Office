package fr.insee.rmes.webservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.code_list.CodeListServiceImpl;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.code_list.CodeListQueries;


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
    	Config config = new Config();
    	codeListService = Mockito.spy(new CodeListServiceImpl());
    	CodeListQueries.setConfig(config);
        MockitoAnnotations.openMocks(this);
    }

    //getCodeListByNotation//

    @Test
    void givengetCodeListByNotation_whenCorrectRequest_thenResponseIsOk() throws RmesException {
    	when(repoGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject());
    	when(repoGestion.getResponseAsArray(anyString())).thenReturn(new JSONArray());
    	
        ResponseEntity<Object> response = codeListResource.getCodeListByNotation(NOTATION);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("{\"notation\":\"213\"}", response.getBody());
    }


}
