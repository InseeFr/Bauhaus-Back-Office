package fr.insee.rmes.modules.ddi.physical_instances.webservice.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceParents;
import java.util.List;
import org.junit.jupiter.api.Test;

class PhysicalInstanceParentsResponseTest {

    @Test
    void fromDomain_exposesParentGroupLabel() {
        PhysicalInstanceParents parents = new PhysicalInstanceParents(
                "fr.insee",
                "su-1",
                "Enquête emploi",
                "fr.insee",
                "grp-1",
                "Base permanente des équipements",
                List.of());

        PhysicalInstanceParentsResponse response = PhysicalInstanceParentsResponse.fromDomain(parents);

        assertEquals("grp-1", response.group().id());
        assertEquals("Base permanente des équipements", response.group().label());
    }

    @Test
    void fromDomain_exposesParentStudyUnitLabel() {
        PhysicalInstanceParents parents = new PhysicalInstanceParents(
                "fr.insee",
                "su-1",
                "Enquête emploi",
                "fr.insee",
                "grp-1",
                "Base permanente des équipements",
                List.of());

        PhysicalInstanceParentsResponse response = PhysicalInstanceParentsResponse.fromDomain(parents);

        assertEquals("su-1", response.studyUnit().id());
        assertEquals("Enquête emploi", response.studyUnit().label());
    }
}
