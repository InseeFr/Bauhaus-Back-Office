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
import fr.insee.rmes.modules.users.domain.port.serverside.RbacFetcher;
import fr.insee.rmes.modules.users.infrastructure.UserProvider;
import java.util.List;
import java.util.stream.Stream;
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
 * Contrat du corps de la duplication d'une PhysicalInstance : le libellé de la copie et son
 * rattachement (Groupe, Étude) sont refusés vides en 400, avant tout appel au service.
 */
@WebMvcTest(
        value = DdiResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class DdiResourcesDuplicationValidationTest {

    private static final List<String> MANDATORY_FIELDS =
            List.of("physicalInstanceLabel", "groupId", "groupAgency", "studyUnitId", "studyUnitAgency");

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
    private RbacFetcher rbacFetcher;

    @MockitoBean
    private BauhausUriBuilder bauhausUriBuilder;

    @MockitoBean
    private Ddi4SchemaService ddi4SchemaService;

    @Autowired
    MockMvc mockMvc;

    private static JSONObject validBody() {
        return new JSONObject()
                .put("physicalInstanceLabel", "PI (copy)")
                .put("dataRelationshipLabel", "Structure : PI (copy)")
                .put("logicalRecordLabel", "Enregistrement logique : PI (copy)")
                .put("groupId", "group-1")
                .put("groupAgency", "fr.insee")
                .put("studyUnitId", "su-1")
                .put("studyUnitAgency", "fr.insee");
    }

    /** Les trois formes de vide qu'un corps peut prendre : absente, chaîne vide, blancs. */
    static Stream<Arguments> emptyValues() {
        return MANDATORY_FIELDS.stream()
                .flatMap(field ->
                        Stream.of(Arguments.of(field, null), Arguments.of(field, ""), Arguments.of(field, " ")));
    }

    @ParameterizedTest(name = "POST duplicate : {0} = [{1}]")
    @MethodSource("emptyValues")
    void duplicatePhysicalInstance_whenAMandatoryFieldIsEmpty_shouldReturnBadRequest(String field, String emptyValue)
            throws Exception {
        JSONObject body = validBody();
        if (emptyValue == null) {
            body.remove(field);
        } else {
            body.put(field, emptyValue);
        }

        mockMvc.perform(post("/ddi/physical-instance/fr.insee/pi-src/duplicate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value(field));

        verify(ddiService, never()).duplicatePhysicalInstance(any(), any(), any());
    }
}
