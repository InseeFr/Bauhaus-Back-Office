package fr.insee.rmes.webservice.codelist;

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

class PartialCodeListsResourcesTest {

    @Mock
    CodeListService codeListService;

    @InjectMocks
    PartialCodeListsResources codeListsResources;


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

}