package fr.insee.rmes.bauhaus_services.structures.utils;

import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import org.junit.jupiter.api.Test;
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

}




