package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.model.ValidationStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicationUtilsTest {
    @Test
    void testIsUnpublished() {
        String unpublishedStatus = ValidationStatus.UNPUBLISHED.getValue();
        assertTrue(PublicationUtils.isUnublished(unpublishedStatus));

        String undefinedStatus = Constants.UNDEFINED;
        assertTrue(PublicationUtils.isUnublished(undefinedStatus));

        String otherStatus = "OTHER_STATUS";
        assertFalse(PublicationUtils.isUnublished(otherStatus));

        assertFalse(PublicationUtils.isUnublished(null));
    }
}