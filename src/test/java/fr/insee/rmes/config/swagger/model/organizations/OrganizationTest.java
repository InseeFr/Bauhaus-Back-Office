package fr.insee.rmes.config.swagger.model.organizations;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OrganizationTest {

    @Test
    void shouldCheckAttributesDefaultValuesAreNull() {
        Organization organization = new Organization();
        List<Boolean> actual = List.of(organization.id == null,
                organization.labelLg1 == null,
                organization.labelLg2 == null,
                organization.altLabel == null,
                organization.type == null,
                organization.motherOrganization == null,
                organization.linkedTo == null,
                organization.seeAlso == null
        );
        List<Boolean> expected = List.of(true, true, true, true,true,true,true,true);
        assertEquals(expected, actual);
    }

}