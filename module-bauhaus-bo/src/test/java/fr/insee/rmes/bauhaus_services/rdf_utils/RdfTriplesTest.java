package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Contrat de {@link RdfTriples}, successeur sans état de {@code RdfUtils} (déprécié pour
 * suppression) pour tout ce qui ne dépend pas de la configuration : littéraux, IRI et ajout
 * de triplets dans un {@link Model}.
 */
class RdfTriplesTest {

    private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();
    private static final IRI SUBJECT = VF.createIRI("http://bauhaus/composants/dimension/d1000");
    private static final Resource GRAPH = VF.createIRI("http://rdf.insee.fr/graphes/composants");

    private final Model model = new LinkedHashModel();

    @Test
    void iri_builds_an_iri_from_a_trimmed_string() {
        assertThat(RdfTriples.iri("  http://bauhaus/concept/c1  "))
                .isEqualTo(VF.createIRI("http://bauhaus/concept/c1"));
    }

    @Test
    void iri_rejects_a_value_that_is_not_an_iri() {
        assertThatThrownBy(() -> RdfTriples.iri("pas une IRI"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void xsdIri_builds_an_iri_in_the_xml_schema_namespace() {
        assertThat(RdfTriples.xsdIri("maxLength"))
                .isEqualTo(VF.createIRI("http://www.w3.org/2001/XMLSchema#maxLength"));
    }

    @Test
    void string_literal_is_trimmed() {
        assertThat(RdfTriples.string("  libellé  ")).isEqualTo(VF.createLiteral("libellé"));
    }

    @Test
    void string_literal_carries_its_language() {
        assertThat(RdfTriples.string("  libellé  ", "fr")).isEqualTo(VF.createLiteral("libellé", "fr"));
    }

    @Test
    void string_literal_of_a_validation_status_is_its_value() {
        assertThat(RdfTriples.string(ValidationStatus.UNPUBLISHED)).isEqualTo(VF.createLiteral("Unpublished"));
    }

    @Test
    void dateTime_literal_is_normalized_to_iso_and_typed() {
        assertThat(RdfTriples.dateTime("2024-01-15T10:00:00.000"))
                .isEqualTo(VF.createLiteral("2024-01-15T10:00:00", XSD.DATETIME));
    }

    @Test
    void addString_adds_a_plain_literal() {
        RdfTriples.addString(SUBJECT, SKOS.NOTATION, "  NOTATION  ", model, GRAPH);

        assertThat(model).containsExactly(
                VF.createStatement(SUBJECT, SKOS.NOTATION, VF.createLiteral("NOTATION"), GRAPH));
    }

    @Test
    void addString_adds_a_localized_literal() {
        RdfTriples.addString(SUBJECT, RDFS.LABEL, "libellé", "fr", model, GRAPH);

        assertThat(model).containsExactly(
                VF.createStatement(SUBJECT, RDFS.LABEL, VF.createLiteral("libellé", "fr"), GRAPH));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "")
    void addString_ignores_a_missing_value(String value) {
        RdfTriples.addString(SUBJECT, SKOS.NOTATION, value, model, GRAPH);
        RdfTriples.addString(SUBJECT, RDFS.LABEL, value, "fr", model, GRAPH);

        assertThat(model).isEmpty();
    }

    @Test
    void addUri_adds_an_iri_object() {
        IRI concept = VF.createIRI("http://bauhaus/concept/c1");

        RdfTriples.addUri(SUBJECT, RDFS.SEEALSO, concept, model, GRAPH);

        assertThat(model).containsExactly(VF.createStatement(SUBJECT, RDFS.SEEALSO, concept, GRAPH));
    }

    @Test
    void addUri_ignores_a_null_iri() {
        RdfTriples.addUri(SUBJECT, RDFS.SEEALSO, (IRI) null, model, GRAPH);

        assertThat(model).isEmpty();
    }

    @Test
    void addUri_converts_a_string_value_into_an_iri() {
        RdfTriples.addUri(SUBJECT, RDFS.SEEALSO, "http://bauhaus/concept/c1", model, GRAPH);

        assertThat(model).containsExactly(VF.createStatement(SUBJECT, RDFS.SEEALSO,
                VF.createIRI("http://bauhaus/concept/c1"), GRAPH));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "")
    void addUri_ignores_a_missing_string_value(String value) {
        RdfTriples.addUri(SUBJECT, RDFS.SEEALSO, value, model, GRAPH);

        assertThat(model).isEmpty();
    }

    /**
     * Le repository des composants s'appuie sur cette levée d'exception : une valeur d'attribut
     * libre qui n'est pas une IRI doit repartir en littéral, pas casser l'enregistrement.
     */
    @Test
    void addUri_throws_when_the_string_value_is_not_an_iri() {
        assertThatThrownBy(() -> RdfTriples.addUri(SUBJECT, RDFS.SEEALSO, "valeur libre", model, GRAPH))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
