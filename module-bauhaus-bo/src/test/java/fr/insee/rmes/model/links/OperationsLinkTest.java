package fr.insee.rmes.model.links;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
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
    }

    /**
     * Les candidats passent par une liste d'{@code Object} : {@code equals} doit répondre faux
     * à {@code null} comme à un objet d'un autre type, et c'est bien {@code equals} qu'on
     * interroge, pas un rapprochement de types que le compilateur saurait déjà refuser.
     */
    @Test
    void shouldNotBeEqualToNullNorToAnObjectOfAnotherType() {
        OperationsLink link = OperationsLink.of("s1000", "série", "label fr", "label en");

        List<Object> others = new ArrayList<>();
        others.add(null);
        others.add("s1000");
        others.add(1000);

        assertThat(others).noneMatch(link::equals);
    }
}
