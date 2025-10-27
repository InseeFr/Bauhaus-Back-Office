package fr.insee.rmes.bauhaus_services.structures.utils;

import fr.insee.rmes.Constants;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static fr.insee.rmes.bauhaus_services.structures.utils.StructureComponentUtils.MODIFIED;
import static fr.insee.rmes.bauhaus_services.structures.utils.StructureComponentUtils.VALIDATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StructureComponentUtilsTest {


    StructureComponentUtils structureComponentUtils = new StructureComponentUtils();

    @Test
    void shouldThrowRmesExceptionWhenUpdateComponent() {
        RmesException exception = assertThrows(RmesBadRequestException.class, () ->  structureComponentUtils.updateComponent("componentId","{\"id\":\"idExample\",\"creator\":\"creatorExample\"}"));
        assertThat(exception.getDetails()).contains("The id of the component should be the same as the one defined in the request");
    }

    @Test
    void shouldThrowRmesExceptionWhenValidateComponentWithoutIdentifiant() {
        RmesException exception = assertThrows(RmesBadRequestException.class, () ->  structureComponentUtils.updateComponent("idExample","{\"id\":\"idExample\",\"creator\":\"creatorExample\"}"));
        assertThat(exception.getDetails()).contains("The property identifiant is required");
    }

    @Test
    void shouldThrowRmesExceptionWhenValidateComponentWithoutLabelLg1() {
        RmesException exception = assertThrows(RmesBadRequestException.class, () ->  structureComponentUtils.updateComponent("idExample","{\"id\":\"idExample\",\"identifiant\":\"identifiantExample\",\"creator\":\"creatorExample\"}"));
        assertThat(exception.getDetails()).contains("{\"message\":\"The property labelLg1 is required\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenValidateComponentWithoutLabelLg2() {
        RmesException exception = assertThrows(RmesBadRequestException.class, () ->  structureComponentUtils.updateComponent("idExample","{\"id\":\"idExample\",\"identifiant\":\"identifiantExample\",\"labelLg1\":\"labelLg1Example\"}"));
        assertThat(exception.getDetails()).contains("{\"message\":\"The property labelLg2 is required\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenValidateComponentWithoutType() {
        RmesException exception = assertThrows(RmesBadRequestException.class, () ->  structureComponentUtils.updateComponent("idExample","{\"id\":\"idExample\",\"identifiant\":\"identifiantExample\",\"labelLg1\":\"labelLg1Example\",\"labelLg2\":\"labelLg2Example\"}"));
        assertThat(exception.getDetails()).contains("{\"message\":\"The property type is required\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenValidateComponentWithInvalidateType() {
        RmesException exception = assertThrows(RmesBadRequestException.class, () ->  structureComponentUtils.updateComponent("idExample","{\"id\":\"idExample\",\"identifiant\":\"identifiantExample\",\"labelLg1\":\"labelLg1Example\",\"labelLg2\":\"labelLg2Example\",\"type\":\"typeExample\"}"));
        assertThat(exception.getDetails()).contains("{\"message\":\"The property type is not valid\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenCreateComponent() {
        RmesException exception = assertThrows(RmesBadRequestException.class, () -> structureComponentUtils.createComponent("{\"id\":\"idExample\",\"identifiant\":\"identifiantExample\",\"labelLg1\":\"labelLg1Example\",\"labelLg2\":\"labelLg2Example\",\"type\":\"typeExample\"}"));
        assertThat(exception.getDetails()).contains("{\"message\":\"During the creation of a new component, the id property should be null\"}");
    }

    @ParameterizedTest
    @ValueSource(strings = { VALIDATED, MODIFIED })
    void shouldThrowRmesExceptionWhenDeleteComponent(String value) {
        JSONObject component = new JSONObject().put("validationState",value);
        RmesException exception = assertThrows(RmesException.class, () -> structureComponentUtils.deleteComponent(component,"id","type"));
        assertThat(exception.getDetails()).contains("{\"details\":\"[]\",\"message\":\"You cannot delete a validated component\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenDeleteComponentWithInvalidStructures() {
        JSONObject exampleOne = new JSONObject().put("validationState",VALIDATED);
        JSONObject exampleTwo = new JSONObject().put("validationState","example");
        JSONArray structures = new JSONArray().put(exampleOne).put(exampleTwo);
        JSONObject component = new JSONObject().put("validationState","value").put("structures",structures);

        RmesException exception = assertThrows(RmesException.class, () -> structureComponentUtils.deleteComponent(component,"id","type"));
        assertThat(exception.getDetails()).contains("{\"details\":\"[]\",\"message\":\"You cannot delete a validated component\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishComponentWithInvalidCreator() {
        JSONObject component = new JSONObject().put(Constants.CREATOR,"");
        RmesException exception = assertThrows(RmesException.class, () -> structureComponentUtils.publishComponent(component));
        assertThat(exception.getDetails()).contains("{\"code\":1004,\"details\":\"[]\",\"message\":\"The creator should not be empty\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishComponentWithInvalidDisseminationStatus() {
        JSONObject component = new JSONObject().put(Constants.CREATOR,"creatorExample").put("disseminationStatus","");
        RmesException exception = assertThrows(RmesException.class, () -> structureComponentUtils.publishComponent(component));
        assertThat(exception.getDetails()).contains("{\"code\":1005,\"details\":\"[]\",\"message\":\"The dissemination status should not be empty\"}");
    }

}




