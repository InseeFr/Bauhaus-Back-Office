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
    
    }




