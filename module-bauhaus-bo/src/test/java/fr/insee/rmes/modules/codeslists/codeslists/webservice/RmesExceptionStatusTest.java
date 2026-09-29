package fr.insee.rmes.modules.codeslists.codeslists.webservice;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.code_list.CodeListKind;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesFileException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.modules.codeslists.partialcodeslists.model.PartialCodesList;
import fr.insee.rmes.utils.Deserializer;
import java.io.IOException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * Une erreur métier garde son statut et son message (ticket 2 de l'audit #1264) : une
 * {@link RmesException} de statut 400 ne sort plus en 500, et aucune réponse n'est vide ni ne
 * contient un nom de classe Java.
 */
class RmesExceptionStatusTest extends AbstractCodesListsResourcesWebMvcTest {

    private static final String MESSAGE = "The code list is not valid";

    @Test
    void a_rmes_exception_keeps_its_bad_request_status_and_message() throws Exception {
        when(codeListService.getDetailedPartialCodesList("CL_TEST"))
                .thenThrow(new RmesException(HttpStatus.BAD_REQUEST.value(), MESSAGE, "details"));

        mockMvc.perform(get("/codeList/partial/{notation}", "CL_TEST"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(MESSAGE));
    }

    @Test
    void a_not_found_rmes_exception_answers_404() throws Exception {
        when(codeListService.getDetailedPartialCodesList("unknown"))
                .thenThrow(new RmesNotFoundException("CodeList not found", "unknown"));

        mockMvc.perform(get("/codeList/partial/{notation}", "unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("CodeList not found"));
    }

    @Test
    void a_bad_request_exception_with_a_json_payload_answers_400_not_403() throws Exception {
        when(codeListService.getDetailedPartialCodesList("CL_TEST"))
                .thenThrow(new RmesBadRequestException(804, MESSAGE, new JSONObject().put("id", "CL_TEST")));

        mockMvc.perform(get("/codeList/partial/{notation}", "CL_TEST"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(804))
                .andExpect(jsonPath("$.message").value(MESSAGE))
                .andExpect(jsonPath("$.id").value("CL_TEST"));
    }

    @Test
    void a_rmes_exception_without_message_still_answers_a_message() throws Exception {
        when(codeListService.getDetailedPartialCodesList("CL_TEST"))
                .thenThrow(new RmesException(HttpStatus.CONFLICT.value(), 812, "CL_TEST"));

        mockMvc.perform(get("/codeList/partial/{notation}", "CL_TEST"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(812))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void a_technical_rmes_exception_does_not_leak_its_cause() throws Exception {
        when(codeListService.getDetailedPartialCodesList("CL_TEST"))
                .thenThrow(new RmesException("Unable to read", new IOException("java.net.ConnectException: refused")));

        mockMvc.perform(get("/codeList/partial/{notation}", "CL_TEST"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(content().string(not(containsString("java."))));
    }

    @Test
    void creating_a_partial_code_list_answers_the_error_message() throws Exception {
        when(codeListService.setCodesList(anyString(), any(CodeListKind.class)))
                .thenThrow(new RmesBadRequestException(MESSAGE, "details"));

        mockMvc.perform(post("/codeList/partial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(MESSAGE));
    }

    @Test
    void updating_a_partial_code_list_answers_the_error_message() throws Exception {
        when(codeListService.setCodesList(anyString(), anyString(), any(CodeListKind.class)))
                .thenThrow(new RmesBadRequestException(MESSAGE, "details"));

        mockMvc.perform(put("/codeList/partial/{id}", "CL_TEST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(MESSAGE));
    }

    @Test
    void deleting_a_partial_code_list_answers_the_error_message() throws Exception {
        doThrow(new RmesNotFoundException("CodeList not found", "unknown"))
                .when(codeListService)
                .deleteCodeList("unknown", CodeListKind.PARTIAL);

        mockMvc.perform(delete("/codeList/partial/{id}", "unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("CodeList not found"));
    }

    @Test
    void an_unreadable_body_answers_400_without_the_jackson_message() throws Exception {
        when(codeListService.setCodesList(anyString(), any(CodeListKind.class)))
                .thenAnswer(invocation -> Deserializer.deserializeJsonString(
                        "{\"id\": {\"not\": \"a string\"}}", PartialCodesList.class));

        mockMvc.perform(post("/codeList/partial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(content().string(not(containsString("java."))))
                .andExpect(content().string(not(containsString("fr.insee"))));
    }

    @Test
    void a_file_error_does_not_answer_the_exception_class_name() throws Exception {
        when(codeListService.getDetailedPartialCodesList("CL_TEST"))
                .thenThrow(new RmesFileException("doc.pdf", "Error reading file", new IOException("minio")));

        mockMvc.perform(get("/codeList/partial/{notation}", "CL_TEST"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(content().string(not(containsString("RmesFileException"))));
    }
}
