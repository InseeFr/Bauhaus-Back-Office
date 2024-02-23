package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.webservice.codesLists.CodeListsResources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeListsResourcesTest {
    @Mock
    CodeListService codeListService;

    @InjectMocks
    CodeListsResources codeListsResources;

    @Test
    void shouldReturn200WithGetAllCodesLists() throws RmesException {
        when(codeListService.getAllCodesLists(false)).thenReturn("body");
        ResponseEntity<Object> response = codeListsResources.getAllCodesLists();
        assertEquals("body", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    @Test
    void shouldThrowErrorWithGetAllCodesLists() throws RmesException {
        when(codeListService.getAllCodesLists(false)).thenThrow(new RmesException(500, "", ""));
        ResponseEntity<Object> response = codeListsResources.getAllCodesLists();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
    @Test
    void shouldReturn200WithGetPaginatedCodesForCodeList() throws RmesException {
        when(codeListService.getCodesForCodeList("notation", 1, null)).thenReturn("body");
        ResponseEntity<Object> response = codeListsResources.getPaginatedCodesForCodeList("notation", 1, null);
        assertEquals("body", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    @Test
    void shouldThrowErrorWithGetPaginatedCodesForCodeList() throws RmesException {
        when(codeListService.getCodesForCodeList("notation", 1, null)).thenThrow(new RmesException(500, "", ""));
        ResponseEntity<Object> response = codeListsResources.getPaginatedCodesForCodeList("notation", 1, null);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shouldReturn200WithGetCodesForCodeList() throws RmesException {
        when(codeListService.getCodesJson("notation", 1, null)).thenReturn("body");
        ResponseEntity<Object> response = codeListsResources.getCodesForCodeList("notation", 1, null);
        assertEquals("body", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    @Test
    void shouldThrowErrorWithGetCodesForCodeList() throws RmesException {
        when(codeListService.getCodesJson("notation", 1, null)).thenThrow(new RmesException(500, "", ""));
        ResponseEntity<Object> response = codeListsResources.getCodesForCodeList("notation", 1, null);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shouldReturn200WithDeleteCodeForCodeList() throws RmesException {
        when(codeListService.deleteCodeFromCodeList("notation", "1")).thenReturn("body");
        ResponseEntity<Object> response = codeListsResources.deleteCodeForCodeList("notation", "1");
        assertEquals("body", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    @Test
    void shouldThrowErrorWithDeleteCodeForCodeList() throws RmesException {
        when(codeListService.deleteCodeFromCodeList("notation", "1")).thenThrow(new RmesException(500, "", ""));
        ResponseEntity<Object> response = codeListsResources.deleteCodeForCodeList("notation", "1");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shouldReturn200WithUpdateCodeForCodeList() throws RmesException {
        when(codeListService.updateCodeFromCodeList("notation", "1", "body")).thenReturn("body");
        ResponseEntity<Object> response = codeListsResources.updateCodeForCodeList("notation", "1", "body");
        assertEquals("body", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    @Test
    void shouldThrowErrorWithUpdateCodeForCodeList() throws RmesException {
        when(codeListService.updateCodeFromCodeList("notation", "1", "body")).thenThrow(new RmesException(500, "", ""));
        ResponseEntity<Object> response = codeListsResources.updateCodeForCodeList("notation", "1", "body");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shouldReturn201WithAddCodeForCodeList() throws RmesException {
        when(codeListService.addCodeFromCodeList("notation", "body")).thenReturn("body");
        ResponseEntity<Object> response = codeListsResources.addCodeForCodeList("notation", "body");
        assertEquals("body", response.getBody());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
    @Test
    void shouldThrowErrorWithAddCodeForCodeList() throws RmesException {
        when(codeListService.addCodeFromCodeList("notation", "body")).thenThrow(new RmesException(500, "", ""));
        ResponseEntity<Object> response = codeListsResources.addCodeForCodeList("notation", "body");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}