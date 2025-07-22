package fr.insee.rmes.config.auth.roles;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class RolesTest {

    @Test
    void shouldCheckRolesAreUniqueness(){
        List<String> actualRoles= List.of(
                Roles.ADMIN,
                Roles.CONCEPTS_CONTRIBUTOR,
                Roles.COLLECTION_CREATOR,
                Roles.CONCEPT_CREATOR,
                Roles.CONCEPT_CONTRIBUTOR,
                Roles.SERIES_CONTRIBUTOR,
                Roles.CODESLIST_CONTRIBUTOR,
                Roles.INDICATOR_CONTRIBUTOR,
                Roles.DATASET_CONTRIBUTOR,
                Roles.STRUCTURES_CONTRIBUTOR,
                Roles.CNIS);
        SortedSet<String> expectedRoles  = new TreeSet<>();
        expectedRoles.addAll(actualRoles);
        assertEquals(expectedRoles.size(),actualRoles.size());

}
}