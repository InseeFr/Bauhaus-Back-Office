package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CreatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UpdatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIItemConvertService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.Ddi4SchemaService;
import fr.insee.rmes.modules.users.domain.port.clientside.UserService;
import fr.insee.rmes.modules.users.infrastructure.UserProvider;
import java.util.List;
import java.util.stream.Stream;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
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
 * Contrat des corps de création ({@code POST}), de modification partielle ({@code PATCH}) et de
 * remplacement ({@code PUT}) d'une PhysicalInstance : un corps invalide répond 400 avec le champ en
 * cause, avant tout appel au service.
 */
@WebMvcTest(
        value = DdiResources.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class DdiResourcesPhysicalInstanceValidationTest {

    private static final List<String> CREATION_MANDATORY_FIELDS =
            List.of("physicalInstanceLabel", "groupId", "groupAgency", "studyUnitId", "studyUnitAgency");

    private static final List<String> UPDATE_LABELS =
            List.of("physicalInstanceLabel", "dataRelationshipLabel", "logicalRecordLabel");

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

    /** Corps tel que l'envoient le dialogue de création, l'écran de l'opération et l'édition. */
    private static JSONObject validBody() {
        return new JSONObject()
                .put("physicalInstanceLabel", "PI")
                .put("dataRelationshipLabel", "Structure : PI")
                .put("logicalRecordLabel", "Enregistrement logique : PI")
                .put("groupId", "group-1")
                .put("groupAgency", "fr.insee")
                .put("studyUnitId", "su-1")
                .put("studyUnitAgency", "fr.insee");
    }

    private static JSONObject withValue(String field, String value) {
        JSONObject body = validBody();
        if (value == null) {
            body.remove(field);
        } else {
            body.put(field, value);
        }
        return body;
    }

    /** Les trois formes de vide qu'un corps peut prendre : absente, chaîne vide, blancs. */
    static Stream<Arguments> emptyCreationValues() {
        return CREATION_MANDATORY_FIELDS.stream()
                .flatMap(field ->
                        Stream.of(Arguments.of(field, null), Arguments.of(field, ""), Arguments.of(field, " ")));
    }

    @ParameterizedTest(name = "POST : {0} = [{1}]")
    @MethodSource("emptyCreationValues")
    void createPhysicalInstance_whenAMandatoryFieldIsEmpty_shouldReturnBadRequest(String field, String emptyValue)
            throws Exception {
        mockMvc.perform(post("/ddi/physical-instance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withValue(field, emptyValue).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value(field));

        verify(ddiService, never()).createPhysicalInstance(any());
    }

    @Test
    void createPhysicalInstance_whenTheBodyIsValid_shouldPassItToTheServiceUnchanged() throws Exception {
        mockMvc.perform(post("/ddi/physical-instance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody().toString()))
                .andExpect(status().isOk());

        verify(ddiService)
                .createPhysicalInstance(new CreatePhysicalInstanceRequest(
                        "PI",
                        "Structure : PI",
                        "Enregistrement logique : PI",
                        "su-1",
                        "fr.insee",
                        "group-1",
                        "fr.insee"));
    }

    /** Un PATCH ne réécrit que les libellés présents : présents, ils ne peuvent pas être vides. */
    static Stream<Arguments> blankUpdateLabels() {
        return UPDATE_LABELS.stream().flatMap(field -> Stream.of(Arguments.of(field, ""), Arguments.of(field, " ")));
    }

    @ParameterizedTest(name = "PATCH : {0} = [{1}]")
    @MethodSource("blankUpdateLabels")
    void updatePhysicalInstance_whenALabelIsBlank_shouldReturnBadRequest(String field, String blankValue)
            throws Exception {
        mockMvc.perform(patch("/ddi/physical-instance/fr.insee/pi-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withValue(field, blankValue).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value(field));

        verify(ddiService, never()).updatePhysicalInstance(any(), any(), any());
    }

    /** Le rattachement à une Étude n'a de sens qu'avec ses deux moitiés : seul, il serait ignoré. */
    @ParameterizedTest(name = "PATCH sans {0}")
    @ValueSource(strings = {"studyUnitId", "studyUnitAgency"})
    void updatePhysicalInstance_whenTheStudyUnitIsHalfGiven_shouldReturnBadRequest(String missingField)
            throws Exception {
        mockMvc.perform(patch("/ddi/physical-instance/fr.insee/pi-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withValue(missingField, null).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("studyUnitComplete"));

        verify(ddiService, never()).updatePhysicalInstance(any(), any(), any());
    }

    @Test
    void updatePhysicalInstance_withOnlyALabel_shouldPassItToTheServiceUnchanged() throws Exception {
        mockMvc.perform(patch("/ddi/physical-instance/fr.insee/pi-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new JSONObject()
                                .put("physicalInstanceLabel", "Nouveau libellé")
                                .toString()))
                .andExpect(status().isOk());

        verify(ddiService)
                .updatePhysicalInstance(
                        "fr.insee", "pi-1", new UpdatePhysicalInstanceRequest("Nouveau libellé", null, null));
    }

    @Test
    void updatePhysicalInstance_whenTheBodyIsValid_shouldPassItToTheServiceUnchanged() throws Exception {
        mockMvc.perform(patch("/ddi/physical-instance/fr.insee/pi-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody().toString()))
                .andExpect(status().isOk());

        verify(ddiService)
                .updatePhysicalInstance(
                        "fr.insee",
                        "pi-1",
                        new UpdatePhysicalInstanceRequest(
                                "PI",
                                "Structure : PI",
                                "Enregistrement logique : PI",
                                "su-1",
                                "fr.insee",
                                "group-1",
                                "fr.insee"));
    }

    @ParameterizedTest(name = "PUT : {0}")
    @ValueSource(strings = {"{}", "{\"topLevelReferences\": [], \"items\": []}", """
                {"items": [{"$type": "DataRelationship", "Agency": "fr.insee", "ID": "dr-1", "Version": "1"}]}
                """})
    void replacePhysicalInstance_withoutAPhysicalInstance_shouldReturnBadRequest(String body) throws Exception {
        mockMvc.perform(put("/ddi/physical-instance/fr.insee/pi-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("body"))
                .andExpect(jsonPath("$.errors[0].message").value("physicalInstance is required"));

        verify(ddiService, never()).updateFullPhysicalInstance(any(), any(), any());
    }

    @Test
    void replacePhysicalInstance_withAPhysicalInstance_shouldReachTheService() throws Exception {
        mockMvc.perform(put("/ddi/physical-instance/fr.insee/pi-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items": [{"$type": "PhysicalInstance", "Agency": "fr.insee", "ID": "pi-1", "Version": "1"}]}
                                """))
                .andExpect(status().isNoContent());

        verify(ddiService).updateFullPhysicalInstance(any(), any(), any());
    }
}
