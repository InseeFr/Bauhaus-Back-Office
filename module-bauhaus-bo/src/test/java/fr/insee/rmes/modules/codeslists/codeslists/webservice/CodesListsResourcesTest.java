package fr.insee.rmes.modules.codeslists.codeslists.webservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.code_list.CodeListItem;
import fr.insee.rmes.bauhaus_services.code_list.CodeListKind;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.codeslists.codeslists.domain.model.CodesListId;
import fr.insee.rmes.modules.codeslists.codeslists.domain.port.clientside.CodesListsService;
import fr.insee.rmes.modules.commons.configuration.swagger.model.Id;
import fr.insee.rmes.modules.commons.configuration.swagger.model.code_list.Page;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class CodesListsResourcesTest {

    private static final CodeRequest MOCKED_CODE =
            new CodeRequest("mocked code", "mocked labelLg1", "mocked labelLg2", null, null);

    private static final CodesListRequest MOCKED_CODES_LIST = new CodesListRequest(
            "mocked id",
            "mocked labelLg1",
            "mocked labelLg2",
            null,
            null,
            "http://bauhaus/HIE000000",
            List.of(),
            "http://disseminationStatus",
            "mocked-list-segment",
            "MockedClass",
            "mocked-code-segment",
            null);

    @Mock
    CodeListService codeListService;

    @Mock
    CodesListsService codesListsService;

    @InjectMocks
    CodesListsResources codeListsResources;

    @Test
    void shouldReturnResponseWhenSetCodesList() throws Exception {
        // Given
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/codeList");
        req.setServerName("localhost");
        req.setServerPort(80);
        req.setScheme("http");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));

        CodesListsResources myCodeListsResources = new CodesListsResources(codeListService, codesListsService);
        String expectedId = "mocked-result";
        when(codesListsService.create(any())).thenReturn(new CodesListId(expectedId));

        // When
        ResponseEntity<String> response = myCodeListsResources.setCodesList(MOCKED_CODES_LIST);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedId, response.getBody());
        assertEquals(
                "/codeList/" + expectedId,
                Objects.requireNonNull(response.getHeaders().getLocation()).getPath());
    }

    @Test
    void shouldReturnResponseWhenUpdateCodesList() throws Exception {
        CodesListsResources myCodeListsResources = new CodesListsResources(codeListService, codesListsService);
        when(codesListsService.update(any(), any())).thenReturn(new CodesListId("mocked id"));
        String actual = myCodeListsResources
                .updateCodesList("mocked id", MOCKED_CODES_LIST)
                .toString();
        Assertions.assertEquals("<200 OK OK,mocked id,[]>", actual);
    }

    @Test
    void shouldReturnResponseWhenDeleteCodeList() throws RmesException {
        doNothing().when(codeListService).deleteCodeList("notation", CodeListKind.FULL);
        CodesListsResources myCodeListsResources = new CodesListsResources(codeListService, codesListsService);
        String actual = myCodeListsResources.deleteCodeList("notation").toString();
        Assertions.assertEquals("<200 OK OK,[]>", actual);
    }

    @Test
    void shouldReturnResponseWhenGetDetailedCodesLisForSearch() throws RmesException, JsonProcessingException {
        CodesListsResources myCodeListsResources = new CodesListsResources(codeListService, codesListsService);
        when(codeListService.getDetailedCodesListForSearch(CodeListKind.FULL)).thenReturn(null);
        String actual = myCodeListsResources.getDetailedCodesLisForSearch().toString();
        Assertions.assertEquals("<200 OK OK,[]>", actual);
    }

    @Test
    void shouldReturnResponseWhenGetDetailedCodesListByNotation() throws RmesException {
        CodesListsResources myCodeListsResources = new CodesListsResources(codeListService, codesListsService);
        when(codeListService.getDetailedCodesList("mocked notation")).thenReturn(null);
        String actual = myCodeListsResources
                .getDetailedCodesListByNotation("mocked notation")
                .toString();
        Assertions.assertEquals("<200 OK OK,[]>", actual);
    }

    @Test
    void shouldReturnResponseWhenGetPaginatedCodesForCodeList() throws RmesException {
        CodesListsResources myCodeListsResources = new CodesListsResources(codeListService, codesListsService);
        when(codeListService.getCodesForCodeList("mocked notation", null, 5, 5, "mocked sort"))
                .thenReturn(null);
        String actual = myCodeListsResources
                .getPaginatedCodesForCodeList("mocked notation", null, 5, 5, "mocked sort")
                .toString();
        Assertions.assertEquals("<200 OK OK,[]>", actual);
    }

    @Test
    void shouldReturnResponseWhenDeleteCodeForCodeList() throws RmesException {
        CodesListsResources myCodeListsResources = new CodesListsResources(codeListService, codesListsService);
        when(codeListService.deleteCodeFromCodeList("mocked notation", "mocked "))
                .thenReturn(null);
        String actual = myCodeListsResources
                .deleteCodeForCodeList("mocked notation", "mocked ")
                .toString();
        Assertions.assertEquals("<200 OK OK,[]>", actual);
    }

    @Test
    void shouldReturnResponseWhenUpdateCodeForCodeList() throws RmesException {
        CodesListsResources myCodeListsResources = new CodesListsResources(codeListService, codesListsService);
        when(codeListService.updateCodeFromCodeList("mocked notation", "mocked code", MOCKED_CODE))
                .thenReturn("mocked result");
        String actual = myCodeListsResources
                .updateCodeForCodeList("mocked notation", "mocked code", MOCKED_CODE)
                .toString();
        Assertions.assertTrue(actual.startsWith("<200 OK OK"));
    }

    @Test
    void shouldReturnResponseWhenAddCodeForCodeList() throws RmesException {
        CodesListsResources myCodeListsResources = new CodesListsResources(codeListService, codesListsService);
        when(codeListService.addCodeFromCodeList("mocked notation", MOCKED_CODE))
                .thenReturn("mocked result");
        String actual = myCodeListsResources
                .addCodeForCodeList("mocked notation", MOCKED_CODE)
                .toString();
        Assertions.assertTrue(actual.startsWith("<201 CREATED"));
    }

    @Test
    void shouldReturnResponseWhenPublishFullCodeList() throws RmesException {
        Id id = new Id("mocked Id");
        doNothing().when(codeListService).publishCodeList("mocked Id", CodeListKind.FULL);
        CodesListsResources myCodeListsResources = new CodesListsResources(codeListService, codesListsService);
        String actual = myCodeListsResources.publishFullCodeList(id).toString();
        Assertions.assertEquals("<200 OK OK,Id[identifier=mocked Id],[]>", actual);
    }

    @Test
    void shouldThrowErrorWithGetAllCodesLists() throws RmesException, JsonProcessingException {
        when(codeListService.getAllCodesLists(CodeListKind.FULL))
                .thenThrow(new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "", ""));
        RmesException exception = assertThrows(RmesException.class, () -> codeListsResources.getAllCodesLists());
        Assertions.assertEquals(500, exception.getStatus());
    }

    @Test
    void shouldReturn200WithGetPaginatedCodesForCodeList() throws RmesException {
        Page page = new Page();
        page.page = 1;
        when(codeListService.getCodesForCodeList("notation", List.of("search"), 1, null, "code"))
                .thenReturn(page);
        ResponseEntity<Page> response =
                codeListsResources.getPaginatedCodesForCodeList("notation", List.of("search"), 1, null, "code");
        assertEquals(1, response.getBody().getPage());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldThrowErrorWithGetPaginatedCodesForCodeList() throws RmesException {
        when(codeListService.getCodesForCodeList("notation", List.of("search"), 1, null, "code"))
                .thenThrow(new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "", ""));
        RmesException exception = assertThrows(
                RmesException.class,
                () -> codeListsResources.getPaginatedCodesForCodeList("notation", List.of("search"), 1, null, "code"));
        Assertions.assertEquals(500, exception.getStatus());
    }

    @Test
    void shouldReturn200WithGetCodesForCodeList() throws RmesException {
        when(codeListService.getCodesJson("notation", 1, null)).thenReturn("{\"total\": 1}");
        ResponseEntity<Page> response = codeListsResources.getCodesForCodeList("notation", 1, null);
        assertEquals(1, response.getBody().getTotal());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldThrowErrorWithGetCodesForCodeList() throws RmesException {
        when(codeListService.getCodesJson("notation", 1, null))
                .thenThrow(new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "", ""));
        RmesException exception =
                assertThrows(RmesException.class, () -> codeListsResources.getCodesForCodeList("notation", 1, null));
        Assertions.assertEquals(500, exception.getStatus());
    }

    @Test
    void shouldReturn200WithDeleteCodeForCodeList() throws RmesException {
        when(codeListService.deleteCodeFromCodeList("notation", "1")).thenReturn("body");
        ResponseEntity<Void> response = codeListsResources.deleteCodeForCodeList("notation", "1");
        assertNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldThrowErrorWithDeleteCodeForCodeList() throws RmesException {
        when(codeListService.deleteCodeFromCodeList("notation", "1"))
                .thenThrow(new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "", ""));
        RmesException exception =
                assertThrows(RmesException.class, () -> codeListsResources.deleteCodeForCodeList("notation", "1"));
        Assertions.assertEquals(500, exception.getStatus());
    }

    @Test
    void shouldReturn200WithUpdateCodeForCodeList() throws RmesException {
        when(codeListService.updateCodeFromCodeList("notation", "1", MOCKED_CODE))
                .thenReturn("code1");
        ResponseEntity<CodeListItem> response = codeListsResources.updateCodeForCodeList("notation", "1", MOCKED_CODE);
        assertEquals("code1", response.getBody().getCode());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldThrowErrorWithUpdateCodeForCodeList() throws RmesException {
        when(codeListService.updateCodeFromCodeList("notation", "1", MOCKED_CODE))
                .thenThrow(new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "", ""));
        RmesException exception = assertThrows(
                RmesException.class, () -> codeListsResources.updateCodeForCodeList("notation", "1", MOCKED_CODE));
        Assertions.assertEquals(500, exception.getStatus());
    }

    @Test
    void shouldReturn201WithAddCodeForCodeList() throws RmesException {
        when(codeListService.addCodeFromCodeList("notation", MOCKED_CODE)).thenReturn("id1");
        ResponseEntity<CodeListItem> response = codeListsResources.addCodeForCodeList("notation", MOCKED_CODE);
        assertEquals("id1", response.getBody().getCode());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void shouldThrowErrorWithAddCodeForCodeList() throws RmesException {
        when(codeListService.addCodeFromCodeList("notation", MOCKED_CODE))
                .thenThrow(new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "", ""));
        RmesException exception =
                assertThrows(RmesException.class, () -> codeListsResources.addCodeForCodeList("notation", MOCKED_CODE));
        Assertions.assertEquals(500, exception.getStatus());
    }
}
