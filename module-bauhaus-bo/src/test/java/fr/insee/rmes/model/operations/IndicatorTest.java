package fr.insee.rmes.model.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import fr.insee.rmes.model.links.OperationsLink;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class IndicatorTest {

    @Test
    void shouldTestEqualsForDifferentObjects() {

        Indicator indicator = Indicator.of("id");

        List<Object> objects = new ArrayList<>();
        objects.add(indicator);
        objects.add(Indicator.of("id"));
        objects.add(Indicator.of("idExample"));
        objects.add(null);
        objects.add("This is an example of string.");
        objects.add(2025);

        List<Boolean> result = new ArrayList<>();

        objects.forEach(object -> result.add(indicator.equals(object)));

        assertEquals(List.of(true, true, false, false, false, false), result);
    }

    /**
     * Chaque propriété participe à l'égalité : le test fait varier les propriétés une par une
     * depuis un indicateur complètement renseigné, pour qu'aucune ne puisse être oubliée de
     * {@code equals} sans que le test le voie.
     */
    @ParameterizedTest
    @MethodSource("oneDifferentPropertyEach")
    void shouldNotBeEqualToAnIndicatorDifferingByASingleProperty(Consumer<Indicator> change) {
        Indicator other = fullyPopulated();
        change.accept(other);

        assertNotEquals(fullyPopulated(), other);
    }

    static Stream<Consumer<Indicator>> oneDifferentPropertyEach() {
        return Stream.of(
                indicator -> indicator.setAbstractLg1("autre"),
                indicator -> indicator.setAbstractLg2("autre"),
                indicator -> indicator.setAccrualPeriodicityCode("autre"),
                indicator -> indicator.setAccrualPeriodicityList("autre"),
                indicator -> indicator.setAltLabelLg1("autre"),
                indicator -> indicator.setAltLabelLg2("autre"),
                indicator -> indicator.setContributors(List.of("autre")),
                indicator -> indicator.setCreators(List.of("autre")),
                indicator -> indicator.setHistoryNoteLg1("autre"),
                indicator -> indicator.setHistoryNoteLg2("autre"),
                indicator -> indicator.setId("autre"),
                indicator -> indicator.setIdSims("autre"),
                indicator -> indicator.setIsReplacedBy(List.of(OperationsLink.of("autre", "indicateur", null, null))),
                indicator -> indicator.setPrefLabelLg1("autre"),
                indicator -> indicator.setPrefLabelLg2("autre"),
                indicator -> indicator.setPublishers(List.of("autre")),
                indicator -> indicator.setReplaces(List.of(OperationsLink.of("autre", "indicateur", null, null))),
                indicator -> indicator.setSeeAlso(List.of(OperationsLink.of("autre", "indicateur", null, null))),
                indicator -> indicator.setWasGeneratedBy(List.of(OperationsLink.of("autre", "série", null, null))),
                indicator -> indicator.setValidationState("Validated"));
    }

    @Test
    void shouldShareItsHashCodeWithAnIdenticalIndicator() {
        assertEquals(fullyPopulated().hashCode(), fullyPopulated().hashCode());
    }

    private static Indicator fullyPopulated() {
        Indicator indicator = Indicator.of("p1000");
        indicator.setAbstractLg1("résumé fr");
        indicator.setAbstractLg2("abstract en");
        indicator.setAccrualPeriodicityCode("A");
        indicator.setAccrualPeriodicityList("http://liste");
        indicator.setAltLabelLg1("alt fr");
        indicator.setAltLabelLg2("alt en");
        indicator.setContributors(List.of("DG75-F302"));
        indicator.setCreators(List.of("DG75-F301"));
        indicator.setHistoryNoteLg1("historique fr");
        indicator.setHistoryNoteLg2("history en");
        indicator.setIdSims("1500");
        indicator.setIsReplacedBy(List.of(OperationsLink.of("p1001", "indicateur", null, null)));
        indicator.setPrefLabelLg1("label fr");
        indicator.setPrefLabelLg2("label en");
        indicator.setPublishers(List.of("http://publisher"));
        indicator.setReplaces(List.of(OperationsLink.of("p0999", "indicateur", null, null)));
        indicator.setSeeAlso(List.of(OperationsLink.of("p1002", "indicateur", null, null)));
        indicator.setWasGeneratedBy(List.of(OperationsLink.of("s1000", "série", null, null)));
        indicator.setValidationState("Unpublished");
        return indicator;
    }
}
