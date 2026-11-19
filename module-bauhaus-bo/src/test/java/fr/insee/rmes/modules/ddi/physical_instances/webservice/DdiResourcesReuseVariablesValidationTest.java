package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIItemConvertService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.Ddi4SchemaService;
import fr.insee.rmes.modules.users.domain.port.clientside.UserService;
import fr.insee.rmes.modules.users.infrastructure.UserProvider;
import java.util.List;
import java.util.stream.Stream;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
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
 * Contrat du corps de la réutilisation de variables (#1674) : le libellé de la nouvelle instance, son
 * Groupe et la liste des variables sont refusés vides en 400, avant tout appel au service.
 */
@WebMvcTest(
        value = DdiResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class DdiResourcesReuseVariablesValidationTest {

    private static final List<String> MANDATORY_FIELDS = List.of("physicalInstanceLabel", "groupId", "groupAgency");

    @MockitoBean
    private DDIService ddiService;

    @MockitoBean
    private DDI4toDDI3ConverterService ddi4toDdi3ConverterService;

    @MockitoBean
    private DDI3toDDI4ConverterService ddi3toDdi4ConverterService;

    @MockitoBean
    private DDIItemConvertService ddiItemConvertService;

    @MockitoBean
    private UserProvider userProvider;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private BauhausUriBuilder bauhausUriBuilder;

    @MockitoBean
    private Ddi4SchemaService ddi4SchemaService;

    @Autowired
    MockMvc mockMvc;

    private static JSONObject validBody() {
        return new JSONObject()
                .put("physicalInstanceLabel", "New PI")
                .put("groupId", "group-1")
                .put("groupAgency", "fr.insee")
                .put("variableIds", new JSONArray(List.of("VAR_1")));
    }

    /** Les trois formes de vide qu'un corps peut prendre : absente, chaîne vide, blancs. */
    static Stream<Arguments> emptyValues() {
        return MANDATORY_FIELDS.stream()
                .flatMap(field ->
                        Stream.of(Arguments.of(field, null), Arguments.of(field, ""), Arguments.of(field, " ")));
    }

    @ParameterizedTest(name = "POST reuse-variables : {0} = [{1}]")
    @MethodSource("emptyValues")
    void reuseVariables_whenAMandatoryFieldIsEmpty_shouldReturnBadRequest(String field, String emptyValue)
            throws Exception {
        JSONObject body = validBody();
        if (emptyValue == null) {
            body.remove(field);
        } else {
            body.put(field, emptyValue);
        }

        mockMvc.perform(post("/ddi/physical-instance/fr.insee/pi-src/reuse-variables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value(field));

        verify(ddiService, never()).reuseVariables(any(), any(), any());
    }

    @ParameterizedTest(name = "POST reuse-variables : variableIds = {0}")
    @MethodSource("emptyVariableIds")
    void reuseVariables_withoutVariable_shouldReturnBadRequest(Object variableIds) throws Exception {
        JSONObject body = validBody().put("variableIds", variableIds);

        mockMvc.perform(post("/ddi/physical-instance/fr.insee/pi-src/reuse-variables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("variableIds"));

        verify(ddiService, never()).reuseVariables(any(), any(), any());
    }

    static Stream<Arguments> emptyVariableIds() {
        return Stream.of(Arguments.of(JSONObject.NULL), Arguments.of(new JSONArray()));
    }
}
