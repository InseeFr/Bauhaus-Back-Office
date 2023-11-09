package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.exceptions.RmesException;
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
    void shouldReturn200WithGetAllPartialCodesLists() throws RmesException {
        when(codeListService.getAllCodesLists(true)).thenReturn("body");
        ResponseEntity<Object> response = codeListsResources.getAllPartialCodesLists();
        assertEquals("body", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    @Test
    void shouldThrowErrorWithGetAllPartialCodesLists() throws RmesException {
        when(codeListService.getAllCodesLists(true)).thenThrow(new RmesException(500, "", ""));
        ResponseEntity<Object> response = codeListsResources.getAllPartialCodesLists();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shouldReturn200WithGetDetailedCodesListByNotation() throws RmesException {
        when(codeListService.getDetailedCodesList("notation", false)).thenReturn("body");
        ResponseEntity<Object> response = codeListsResources.getDetailedCodesListByNotation("notation");
        assertEquals("body", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    @Test
    void shouldThrowErrorWithGetDetailedCodesListByNotation() throws RmesException {
        when(codeListService.getDetailedCodesList("notation", false)).thenThrow(new RmesException(500, "", ""));
        ResponseEntity<Object> response = codeListsResources.getDetailedCodesListByNotation("notation");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shouldReturn200WithGetPaginatedCodesForCodeList() throws RmesException {
        when(codeListService.getCodesForCodeList("notation", 1)).thenReturn("body");
        ResponseEntity<Object> response = codeListsResources.getPaginatedCodesForCodeList("notation", 1);
        assertEquals("body", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    @Test
    void shouldThrowErrorWithGetPaginatedCodesForCodeList() throws RmesException {
        when(codeListService.getCodesForCodeList("notation", 1)).thenThrow(new RmesException(500, "", ""));
        ResponseEntity<Object> response = codeListsResources.getPaginatedCodesForCodeList("notation", 1);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shouldReturn200WithGetCodesForCodeList() throws RmesException {
        when(codeListService.getCodesJson("notation", 1)).thenReturn("body");
        ResponseEntity<Object> response = codeListsResources.getCodesForCodeList("notation", 1);
        assertEquals("body", response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    @Test
    void shouldThrowErrorWithGetCodesForCodeList() throws RmesException {
        when(codeListService.getCodesJson("notation", 1)).thenThrow(new RmesException(500, "", ""));
        ResponseEntity<Object> response = codeListsResources.getCodesForCodeList("notation", 1);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}