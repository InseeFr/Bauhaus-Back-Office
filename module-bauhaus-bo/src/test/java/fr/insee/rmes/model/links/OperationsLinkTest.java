package fr.insee.rmes.model.links;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OperationsLinkTest {

    @Test
    void shouldCarryTheIdentityAndTheLabelsOfTheLink() {
        OperationsLink link = OperationsLink.of("s1000", "série", "label fr", "label en");

        assertThat(link.getId()).isEqualTo("s1000");
        assertThat(link.getType()).isEqualTo("série");
        assertThat(link.getLabelLg1()).isEqualTo("label fr");
        assertThat(link.getLabelLg2()).isEqualTo("label en");
        assertThat(OperationsLink.getClassOperationsLink()).isEqualTo(OperationsLink.CLASS_NAME);
    }

    /** Un lien sans identifiant ne désigne rien : c'est ce que la conversion XML appelle « vide ». */
    @Test
    void shouldBeEmptyWhenItHasNoId() {
        assertThat(new OperationsLink().isEmpty()).isTrue();
        assertThat(OperationsLink.of("", "série", null, null).isEmpty()).isTrue();
        assertThat(OperationsLink.of("s1000", "série", null, null).isEmpty()).isFalse();
    }

    @Test
    void shouldConsiderTwoLinksEqualWhenAllTheirPropertiesMatch() {
        OperationsLink link = OperationsLink.of("s1000", "série", "label fr", "label en");
        OperationsLink same = OperationsLink.of("s1000", "série", "label fr", "label en");

        assertThat(link).isEqualTo(same).hasSameHashCodeAs(same);
        assertThat(link).isEqualTo(link);
    }

    @Test
    void shouldDistinguishLinksThatDifferByAnyProperty() {
        OperationsLink link = OperationsLink.of("s1000", "série", "label fr", "label en");

        assertThat(link).isNotEqualTo(OperationsLink.of("s1001", "série", "label fr", "label en"));
        assertThat(link).isNotEqualTo(OperationsLink.of("s1000", "indicateur", "label fr", "label en"));
        assertThat(link).isNotEqualTo(OperationsLink.of("s1000", "série", "autre", "label en"));
        assertThat(link).isNotEqualTo(OperationsLink.of("s1000", "série", "label fr", "other"));
        assertThat(link).isNotEqualTo(null).isNotEqualTo("s1000");
    }
}
