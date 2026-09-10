package fr.insee.rmes.model.operations.documentations;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.rdf4j.model.vocabulary.ORG;
import org.junit.jupiter.api.Test;

class RangeTypeTest {

    @Test
    void testOrganizationEnumValues() {
        assertEquals(
                "ORGANIZATION", RangeType.getEnumByRdfType(ORG.ORGANIZATION).getJsonType());
    }
}
