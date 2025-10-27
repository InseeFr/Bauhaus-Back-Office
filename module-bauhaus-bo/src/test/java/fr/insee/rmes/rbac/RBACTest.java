package fr.insee.rmes.rbac;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RBACTest {

    @Test
    void shouldCheckModuleEnumValuesAreUnique() {
        List<String> enums = List.of(RBAC.Module.CONCEPT_CONCEPT.toString(),
                RBAC.Module.CONCEPT_COLLECTION.toString(),
                RBAC.Module.CLASSIFICATION_FAMILY.toString(),
                RBAC.Module.CLASSIFICATION_SERIES.toString(),
                RBAC.Module.CLASSIFICATION_CLASSIFICATION.toString(),
                RBAC.Module.OPERATION_FAMILY.toString(),
                RBAC.Module.OPERATION_SERIES.toString(),
                RBAC.Module.OPERATION_OPERATION.toString(),
                RBAC.Module.OPERATION_INDICATOR.toString(),
                RBAC.Module.OPERATION_SIMS.toString(),
                RBAC.Module.OPERATION_DOCUMENT.toString(),
                RBAC.Module.STRUCTURE_STRUCTURE.toString(),
                RBAC.Module.STRUCTURE_COMPONENT.toString(),
                RBAC.Module.CODESLIST_CODESLIST.toString(),
                RBAC.Module.CODESLIST_PARTIALCODESLIST.toString(),
                RBAC.Module.DATASET_DATASET.toString(),
                RBAC.Module.DATASET_DISTRIBUTION.toString(),
                RBAC.Module.GEOGRAPHY.toString(),
                RBAC.Module.UNKNOWN.toString()
        );

        Set<String> set = new HashSet<>(enums);
        assertEquals(set.size(),enums.size());

    }

    @Test
    void shouldCheckPrivilegeEnumValuesAreUnique() {
        List<String> enums = List.of(RBAC.Privilege.CREATE.toString(),
                RBAC.Privilege.READ.toString(),
                RBAC.Privilege.UPDATE.toString(),
                RBAC.Privilege.DELETE.toString(),
                RBAC.Privilege.PUBLISH.toString(),
                RBAC.Privilege.UNKNOWN.toString()
        );

        Set<String> set = new HashSet<>(enums);
        assertEquals(set.size(),enums.size());
    }

    @Test
    void shouldCheckStrategyEnumValuesAreUnique() {
        List<String> enums = List.of(RBAC.Strategy.ALL.toString(),
                RBAC.Strategy.STAMP.toString(),
                RBAC.Strategy.NONE.toString()
        );
        Set<String> set = new HashSet<>(enums);
        assertEquals(set.size(),enums.size());
    }

}