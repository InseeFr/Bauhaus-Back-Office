package fr.insee.rmes.modules.concepts.collections.webservice;

import fr.insee.rmes.modules.concepts.collections.domain.model.CollectionMember;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CollectionMemberResponseTest {

    @Test
    void should_convert_from_domain_with_all_fields() {
        var domain = new CollectionMember("c00001", "Label FR", "Label EN");

        var response = CollectionMemberResponse.fromDomain(domain);

        assertNotNull(response);
        assertEquals("c00001", response.id());
        assertEquals("Label FR", response.prefLabelLg1());
        assertEquals("Label EN", response.prefLabelLg2());
    }

    @Test
    void should_convert_from_domain_without_second_label() {
        var domain = new CollectionMember("c00001", "Label FR", null);

        var response = CollectionMemberResponse.fromDomain(domain);

        assertNotNull(response);
        assertEquals("c00001", response.id());
        assertEquals("Label FR", response.prefLabelLg1());
        assertNull(response.prefLabelLg2());
    }
}