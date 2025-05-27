package fr.insee.rmes.webservice.codesLists;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.config.swagger.model.Id;
import fr.insee.rmes.exceptions.RmesException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(properties = { "fr.insee.rmes.bauhaus.lg1=fr", "fr.insee.rmes.bauhaus.lg2=en"})
class CodeListsResourcesTest {

    @MockitoBean
    CodeListService codeListService;

    @Test
    void shouldReturnResponseWhenSetCodesList()  throws RmesException {
        CodeListsResources codeListsResources= new CodeListsResources(codeListService);
        when(codeListService.setCodesList("mocked body", false)).thenReturn("mocked result");
        String actual = codeListsResources.setCodesList("mocked body").toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenUpdateCodesList()  throws RmesException {
        CodeListsResources codeListsResources= new CodeListsResources(codeListService);
        when(codeListService.setCodesList("mocked id", "mocked body", false)).thenReturn("mocked result");
        String actual = codeListsResources.updateCodesList("mocked id", "mocked body").toString();
        Assertions.assertEquals("<200 OK OK,mocked id,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenDeleteCodeList()  throws RmesException {
        doNothing().when(codeListService).deleteCodeList("notation", false);
        CodeListsResources codeListsResources= new CodeListsResources(codeListService);
        String actual = codeListsResources.deleteCodeList("notation").toString();
        Assertions.assertEquals("<200 OK OK,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetDetailedCodesLisForSearch() throws RmesException, JsonProcessingException {
        CodeListsResources codeListsResources= new CodeListsResources(codeListService);
        when(codeListService.getDetailedCodesListForSearch(false)).thenReturn(null);
        String actual = codeListsResources.getDetailedCodesLisForSearch().toString();
        Assertions.assertEquals("<200 OK OK,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetDetailedCodesListByNotation() throws RmesException {
        CodeListsResources codeListsResources= new CodeListsResources(codeListService);
        when(codeListService.getDetailedCodesList("mocked notation")).thenReturn(null);
        String actual = codeListsResources.getDetailedCodesListByNotation("mocked notation").toString();
        Assertions.assertEquals("<200 OK OK,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetPaginatedCodesForCodeList() throws RmesException {
        CodeListsResources codeListsResources= new CodeListsResources(codeListService);
        when(codeListService.getCodesForCodeList("mocked notation",null,5,5,"mocked sort")).thenReturn(null);
        String actual = codeListsResources.getPaginatedCodesForCodeList("mocked notation",null,5,5,"mocked sort").toString();
        Assertions.assertEquals("<200 OK OK,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenDeleteCodeForCodeList() throws RmesException {
        CodeListsResources codeListsResources= new CodeListsResources(codeListService);
        when(codeListService.deleteCodeFromCodeList("mocked notation", "mocked ")).thenReturn(null);
        String actual = codeListsResources.deleteCodeForCodeList("mocked notation", "mocked ").toString();
        Assertions.assertEquals("<200 OK OK,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenUpdateCodeForCodeList() throws RmesException{
        CodeListsResources codeListsResources= new CodeListsResources(codeListService);
        when(codeListService.updateCodeFromCodeList("mocked notation", "mocked code", "mocked body")).thenReturn("mocked result");
        String actual = codeListsResources.updateCodeForCodeList("mocked notation", "mocked code", "mocked body").toString();
        Assertions.assertTrue(actual.startsWith("<200 OK OK"));
    }

    @Test
    void shouldReturnResponseWhenAddCodeForCodeList() throws RmesException {
        CodeListsResources codeListsResources= new CodeListsResources(codeListService);
        when(codeListService.addCodeFromCodeList("mocked notation", "mocked body")).thenReturn("mocked result");
        String actual = codeListsResources.addCodeForCodeList("mocked notation", "mocked body").toString();
        Assertions.assertTrue(actual.startsWith("<201 CREATED"));
    }


    @Test
    void shouldReturnResponseWhenPublishFullCodeList() throws RmesException {
        Id id = new Id("mocked Id");
        doNothing().when(codeListService).publishCodeList("mocked Id", false);
        CodeListsResources codeListsResources= new CodeListsResources(codeListService);
        String actual = codeListsResources.publishFullCodeList(id).toString();
        Assertions.assertEquals("<200 OK OK,Id[identifier=mocked Id],[]>",actual);
    }





}