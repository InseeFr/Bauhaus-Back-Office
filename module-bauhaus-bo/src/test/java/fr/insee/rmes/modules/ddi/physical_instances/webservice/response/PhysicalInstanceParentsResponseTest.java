package fr.insee.rmes.modules.ddi.physical_instances.webservice.response;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceParents;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PhysicalInstanceParentsResponseTest {

    @Test
    void fromDomain_exposesParentGroupLabel() {
        PhysicalInstanceParents parents = new PhysicalInstanceParents(
                "fr.insee", "su-1", "fr.insee", "grp-1", "Base permanente des équipements", List.of());

        PhysicalInstanceParentsResponse response = PhysicalInstanceParentsResponse.fromDomain(parents);

        assertEquals("grp-1", response.group().id());
        assertEquals("Base permanente des équipements", response.group().label());
        // le study unit n'a pas de label exposé
        assertNull(response.studyUnit().label());
    }
}
