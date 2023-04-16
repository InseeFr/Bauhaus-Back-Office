package fr.insee.rmes.webservice;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.code_list.CodeListServiceImpl;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.code_list.CodeListQueries;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


class CodeListsResourcesTest {

    private final static String NOTATION = "213";
        
    @InjectMocks //CLASS TO TEST
    private CodeListsResources codeListResource;
    

 	@Mock
 	CodeListService codeListService ;

    @BeforeEach
    public void init() {
    	Config config = new Config();
    	codeListService = Mockito.spy(new CodeListServiceImpl());
    	CodeListQueries.setConfig(config);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void givengetCodeListByNotation_whenCorrectRequest_thenResponseIsOk() throws RmesException {
    	when(codeListService.getCodeListJson(anyString())).thenReturn("codelist");

        ResponseEntity<Object> response = codeListResource.getCodeListByNotation(NOTATION);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("codelist", response.getBody());
    }


}
