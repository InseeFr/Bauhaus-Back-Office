package fr.insee.rmes.model.operations.documentations;

import org.eclipse.rdf4j.model.vocabulary.ORG;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RangeTypeTest {

    @Test
    void testOrganizationEnumValues() {
        assertEquals("ORGANIZATION", RangeType.getEnumByRdfType(ORG.ORGANIZATION).getJsonType());
    }
}