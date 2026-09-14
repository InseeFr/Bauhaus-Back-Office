package fr.insee.rmes.modules.codeslists.codeslists.webservice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.modules.codeslists.codeslists.domain.port.clientside.CodesListsService;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import java.util.List;
import java.util.stream.Stream;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Contrat du corps de requête d'une liste de codes complète : les huit champs que le front exige
 * sont refusés en 400 par Bean Validation, avant que le service ne touche au dépôt.
 * <p>
 * Le contrôle historique (validateCodeList) ne testait que la <em>présence</em> de la clé, avec
 * {@code JSONObject.has()} : {@code {"labelLg1": " "}} passait. Et {@code lastCodeUriSegment},
 * {@code creator} et {@code disseminationStatus} n'étaient contrôlés nulle part, alors qu'ils sont
 * lus sans garde à l'écriture — d'où un 500 au premier ajout de code sur une liste créée sans
 * {@code lastCodeUriSegment}.
 */
@WebMvcTest(
        value = CodesListsResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class CodesListsResourcesValidationTest {

    private static final String ID = "CL_TEST";

    /** Les huit champs que la validation zod du front impose déjà côté client. */
    private static final List<String> MANDATORY_FIELDS = List.of(
            "id",
            "labelLg1",
            "labelLg2",
            "lastListUriSegment",
            "lastClassUriSegment",
            "lastCodeUriSegment",
            "creator",
            "disseminationStatus");

    @MockitoBean
    private CodeListService codeListService;

    @MockitoBean
    private CodesListsService codesListsService;

    @Autowired
    MockMvc mockMvc;

    private static JSONObject validBody() {
        return new JSONObject()
                .put("id", ID)
                .put("labelLg1", "libellé")
                .put("labelLg2", "label")
                .put("lastListUriSegment", "cl-test")
                .put("lastClassUriSegment", "ClTest")
                .put("lastCodeUriSegment", "cl-test-code")
                .put("creator", "http://bauhaus/HIE000000")
                .put("disseminationStatus", "http://id.insee.fr/codes/base/statutDiffusion/Public");
    }

    /** Les trois formes de vide qu'un corps peut prendre : absente, chaîne vide, blancs. */
    static Stream<org.junit.jupiter.params.provider.Arguments> emptyValues() {
        return MANDATORY_FIELDS.stream()
                .flatMap(field -> Stream.of(
                        org.junit.jupiter.params.provider.Arguments.of(field, null),
                        org.junit.jupiter.params.provider.Arguments.of(field, ""),
                        org.junit.jupiter.params.provider.Arguments.of(field, " ")));
    }

    private static String bodyWithout(String field, String emptyValue) {
        JSONObject body = validBody();
        if (emptyValue == null) {
            body.remove(field);
        } else {
            body.put(field, emptyValue);
        }
        return body.toString();
    }

    @ParameterizedTest(name = "POST /codeList : {0} = [{1}]")
    @MethodSource("emptyValues")
    void setCodesList_whenAMandatoryFieldIsEmpty_shouldReturnBadRequest(String field, String emptyValue)
            throws Exception {
        mockMvc.perform(post("/codeList")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithout(field, emptyValue)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value(field));
    }

    @ParameterizedTest(name = "PUT /codeList/ID : {0} = [{1}]")
    @MethodSource("emptyValues")
    void updateCodesList_whenAMandatoryFieldIsEmpty_shouldReturnBadRequest(String field, String emptyValue)
            throws Exception {
        mockMvc.perform(put("/codeList/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithout(field, emptyValue)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value(field));
    }

    @Test
    void setCodesList_whenACarriedCodeIsIncomplete_shouldReturnBadRequest() throws Exception {
        String body = validBody()
                .put("codes", new org.json.JSONArray().put(new JSONObject().put("code", "A")))
                .toString();

        mockMvc.perform(post("/codeList")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
