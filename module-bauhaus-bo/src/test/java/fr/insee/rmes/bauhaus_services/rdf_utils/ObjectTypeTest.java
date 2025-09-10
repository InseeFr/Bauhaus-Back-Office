package fr.insee.rmes.bauhaus_services.rdf_utils;

import org.eclipse.rdf4j.model.vocabulary.ORG;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObjectTypeTest {
    @Test
    void testOrganizationEnumValues() {
        assertEquals("organization", ObjectType.getEnum(ORG.ORGANIZATION).labelType());
    }
}