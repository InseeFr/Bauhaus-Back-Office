package fr.insee.rmes.webservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.code_list.CodeList;
import fr.insee.rmes.bauhaus_services.code_list.CodeListItem;
import fr.insee.rmes.config.swagger.model.code_list.CodeListResponse;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.webservice.codesLists.CodeListsResources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ExtendWith(MockitoExtension.class)
class CodeListsResourcesTest {
    @Mock
    CodeListService codeListService;

    @InjectMocks
    CodeListsResources codeListsResources;

    @Test
    void shouldReturn200WithGetAllCodesLists() throws RmesException, JsonProcessingException {
        when(codeListService.getAllCodesLists(false)).thenReturn("[{\"notation\":\"id1\"},{\"notation\":\"id2\"}]");
        ResponseEntity<List<CodeListResponse>> response = codeListsResources.getAllCodesLists();
        assertEquals("id1", response.getBody().get(0).getNotation());
        assertEquals("id2", response.getBody().get(1).getNotation());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldThrowErrorWithGetAllCodesLists() throws RmesException, JsonProcessingException {
        when(codeListService.getAllCodesLists(false)).thenThrow(new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "", ""));
        RmesException exception = assertThrows(RmesException.class, () -> codeListsResources.getAllCodesLists());
        Assertions.assertEquals(500, exception.getStatus());
    }

    @Test
    void shouldReturn200WithGetPaginatedCodesForCodeList() throws RmesException {
        when(codeListService.getCodesForCodeList("notation", List.of("search"), 1, null, "code")).thenReturn("{\"notation\":\"id1\"}");
        ResponseEntity<CodeList> response = codeListsResources.getPaginatedCodesForCodeList("notation", List.of("search"), 1, null, "code");
        assertEquals("id1", response.getBody().getNotation());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    @Test
    void shouldThrowErrorWithGetPaginatedCodesForCodeList() throws RmesException {
        when(codeListService.getCodesForCodeList("notation", List.of("search"), 1, null, "code")).thenThrow(new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "", ""));
        RmesException exception = assertThrows(RmesException.class, () -> codeListsResources.getPaginatedCodesForCodeList("notation", List.of("search"), 1, null, "code"));
        Assertions.assertEquals(500, exception.getStatus());
    }

    @Test
    void shouldReturn200WithGetCodesForCodeList() throws RmesException {
        when(codeListService.getCodesJson("notation", 1, null)).thenReturn("{\"notation\":\"id1\"}");
        ResponseEntity<CodeList> response = codeListsResources.getCodesForCodeList("notation", 1, null);
        assertEquals("id1", response.getBody().getNotation());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldThrowErrorWithGetCodesForCodeList() throws RmesException {
        when(codeListService.getCodesJson("notation", 1, null)).thenThrow(new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "", ""));
        RmesException exception = assertThrows(RmesException.class, () -> codeListsResources.getCodesForCodeList("notation", 1, null));
        Assertions.assertEquals(500, exception.getStatus());
    }

    @Test
    void shouldReturn200WithDeleteCodeForCodeList() throws RmesException {
        when(codeListService.deleteCodeFromCodeList("notation", "1")).thenReturn("body");
        ResponseEntity<Void> response = codeListsResources.deleteCodeForCodeList("notation", "1");
        assertEquals(null, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    @Test
    void shouldThrowErrorWithDeleteCodeForCodeList() throws RmesException {
        when(codeListService.deleteCodeFromCodeList("notation", "1")).thenThrow(new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "", ""));
        RmesException exception = assertThrows(RmesException.class, () -> codeListsResources.deleteCodeForCodeList("notation", "1"));
        Assertions.assertEquals(500, exception.getStatus());
    }

    @Test
    void shouldReturn200WithUpdateCodeForCodeList() throws RmesException {
        when(codeListService.updateCodeFromCodeList("notation", "1", "body")).thenReturn("code1");
        ResponseEntity<CodeListItem> response = codeListsResources.updateCodeForCodeList("notation", "1", "body");
        assertEquals("code1", response.getBody().getCode());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldThrowErrorWithUpdateCodeForCodeList() throws RmesException {
        when(codeListService.updateCodeFromCodeList("notation", "1", "body")).thenThrow(new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "", ""));
        RmesException exception = assertThrows(RmesException.class, () -> codeListsResources.updateCodeForCodeList("notation", "1", "body"));
        Assertions.assertEquals(500, exception.getStatus());
    }

    @Test
    void shouldReturn201WithAddCodeForCodeList() throws RmesException {
        when(codeListService.addCodeFromCodeList("notation", "body")).thenReturn("id1");
        ResponseEntity<CodeListItem> response = codeListsResources.addCodeForCodeList("notation", "body");
        assertEquals("id1", response.getBody().getCode());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void shouldThrowErrorWithAddCodeForCodeList() throws RmesException {
        when(codeListService.addCodeFromCodeList("notation", "body")).thenThrow(new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "", ""));
        RmesException exception = assertThrows(RmesException.class, () -> codeListsResources.addCodeForCodeList("notation","body"));
        Assertions.assertEquals(500, exception.getStatus());
    }
}