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
 * Contrat du corps de la nouvelle période (#1672) : le libellé de la nouvelle instance, son Étude, son
 * Groupe et la liste des variables sont refusés vides en 400, avant tout appel au service.
 */
@WebMvcTest(
        value = DdiResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class DdiResourcesRolloverValidationTest {

    private static final List<String> MANDATORY_FIELDS =
            List.of("physicalInstanceLabel", "studyUnitId", "studyUnitAgency", "groupId", "groupAgency");

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
                .put("physicalInstanceLabel", "PI 2025")
                .put("studyUnitId", "su-2025")
                .put("studyUnitAgency", "fr.insee")
                .put("groupId", "group-1")
                .put("groupAgency", "fr.insee")
                .put("variables", new JSONArray().put(new JSONObject().put("sourceId", "VAR_1")));
    }

    /** Les trois formes de vide qu'un corps peut prendre : absente, chaîne vide, blancs. */
    static Stream<Arguments> emptyValues() {
        return MANDATORY_FIELDS.stream()
                .flatMap(field ->
                        Stream.of(Arguments.of(field, null), Arguments.of(field, ""), Arguments.of(field, " ")));
    }

    @ParameterizedTest(name = "POST rollover : {0} = [{1}]")
    @MethodSource("emptyValues")
    void rollover_whenAMandatoryFieldIsEmpty_shouldReturnBadRequest(String field, String emptyValue) throws Exception {
        JSONObject body = validBody();
        if (emptyValue == null) {
            body.remove(field);
        } else {
            body.put(field, emptyValue);
        }

        mockMvc.perform(post("/ddi/physical-instance/fr.insee/pi-src/rollover")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value(field));

        verify(ddiService, never()).rollover(any(), any(), any());
    }

    @ParameterizedTest(name = "POST rollover : variables = {0}")
    @MethodSource("emptyVariables")
    void rollover_withoutVariable_shouldReturnBadRequest(Object variables) throws Exception {
        JSONObject body = validBody().put("variables", variables);

        mockMvc.perform(post("/ddi/physical-instance/fr.insee/pi-src/rollover")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("variables"));

        verify(ddiService, never()).rollover(any(), any(), any());
    }

    @ParameterizedTest(name = "POST rollover : sourceId = [{0}]")
    @MethodSource("blankSourceIds")
    void rollover_withAVariableWithoutSourceId_shouldReturnBadRequest(Object sourceId) throws Exception {
        JSONObject body = validBody().put("variables", new JSONArray().put(new JSONObject().put("sourceId", sourceId)));

        mockMvc.perform(post("/ddi/physical-instance/fr.insee/pi-src/rollover")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("variables[0].sourceId"));

        verify(ddiService, never()).rollover(any(), any(), any());
    }

    static Stream<Arguments> blankSourceIds() {
        return Stream.of(Arguments.of(JSONObject.NULL), Arguments.of(""), Arguments.of(" "));
    }

    static Stream<Arguments> emptyVariables() {
        return Stream.of(Arguments.of(JSONObject.NULL), Arguments.of(new JSONArray()));
    }
}
