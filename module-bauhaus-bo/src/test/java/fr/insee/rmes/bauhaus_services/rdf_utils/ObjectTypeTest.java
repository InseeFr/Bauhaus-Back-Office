package fr.insee.rmes.bauhaus_services.rdf_utils;

import static org.junit.jupiter.api.Assertions.*;

import fr.insee.rmes.graphdb.ObjectType;
import org.eclipse.rdf4j.model.vocabulary.ORG;
import org.junit.jupiter.api.Test;

class ObjectTypeTest {
    @Test
    void testOrganizationEnumValues() {
        assertEquals("organization", ObjectType.getEnum(ORG.ORGANIZATION).labelType());
    }
}
