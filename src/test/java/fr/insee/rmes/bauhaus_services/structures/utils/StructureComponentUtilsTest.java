package fr.insee.rmes.bauhaus_services.structures.utils;

import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StructureComponentUtilsTest {

    @Test
    void shouldThrowRmesExceptionWhenUpdateComponent() {
        StructureComponentUtils structureComponentUtils = new StructureComponentUtils();
        RmesException exception = assertThrows(RmesBadRequestException.class, () ->  structureComponentUtils.updateComponent("componentId","{\"id\":\"idExample\",\"creator\":\"creatorExample\"}"));
        assertThat(exception.getDetails()).contains("The id of the component should be the same as the one defined in the request");
    }

    @Test
    void shouldThrowRmesExceptionWhenValidateComponentWithoutIdentifiant() {
        StructureComponentUtils structureComponentUtils = new StructureComponentUtils();
        RmesException exception = assertThrows(RmesBadRequestException.class, () ->  structureComponentUtils.updateComponent("idExample","{\"id\":\"idExample\",\"creator\":\"creatorExample\"}"));
        assertThat(exception.getDetails()).contains("The property identifiant is required");
    }

    @Test
    void shouldThrowRmesExceptionWhenValidateComponentWithoutLabelLg1() {
        StructureComponentUtils structureComponentUtils = new StructureComponentUtils();
        RmesException exception = assertThrows(RmesBadRequestException.class, () ->  structureComponentUtils.updateComponent("idExample","{\"id\":\"idExample\",\"identifiant\":\"identifiantExample\",\"creator\":\"creatorExample\"}"));
        assertThat(exception.getDetails()).contains("{\"message\":\"The property labelLg1 is required\"}");
    }
    
    }




