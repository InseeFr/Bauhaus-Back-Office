package fr.insee.rmes.bauhaus_services.rdf_utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Contrat de {@link RdfTriples}, pour tout ce qui ne dépend pas de la configuration :
 * littéraux, IRI et ajout de triplets dans un {@link Model}.
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
        assertThatThrownBy(() -> RdfTriples.iri("pas une IRI")).isInstanceOf(IllegalArgumentException.class);
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

        assertThat(model)
                .containsExactly(VF.createStatement(SUBJECT, SKOS.NOTATION, VF.createLiteral("NOTATION"), GRAPH));
    }

    @Test
    void addString_adds_a_localized_literal() {
        RdfTriples.addString(SUBJECT, RDFS.LABEL, "libellé", "fr", model, GRAPH);

        assertThat(model)
                .containsExactly(VF.createStatement(SUBJECT, RDFS.LABEL, VF.createLiteral("libellé", "fr"), GRAPH));
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

        assertThat(model)
                .containsExactly(
                        VF.createStatement(SUBJECT, RDFS.SEEALSO, VF.createIRI("http://bauhaus/concept/c1"), GRAPH));
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

    @Test
    void bool_literal_is_typed_as_xsd_boolean() {
        assertThat(RdfTriples.bool(true)).isEqualTo(VF.createLiteral(true));
    }

    @Test
    void integer_literal_is_typed_as_xsd_int() {
        assertThat(RdfTriples.integer("3")).isEqualTo(VF.createLiteral("3", XSD.INT));
    }

    @Test
    void date_literal_keeps_only_the_day_and_is_typed() {
        assertThat(RdfTriples.date("2024-01-15T10:00:00.000")).isEqualTo(VF.createLiteral("2024-01-15", XSD.DATE));
    }

    @Test
    void xml_literal_is_trimmed_and_typed_as_rdf_xml_literal() {
        assertThat(RdfTriples.xmlLiteral("  <p>note</p>  ")).isEqualTo(VF.createLiteral("<p>note</p>", RDF.XMLLITERAL));
    }

    @Test
    void language_literal_is_trimmed_and_typed_as_xsd_language() {
        assertThat(RdfTriples.language("  fr  ")).isEqualTo(VF.createLiteral("fr", XSD.LANGUAGE));
    }

    @Test
    void add_date_time_adds_a_typed_date_time() {
        RdfTriples.addDateTime(SUBJECT, DCTERMS.CREATED, "2024-01-15T10:00:00.000", model, GRAPH);

        assertThat(model)
                .containsExactly(VF.createStatement(
                        SUBJECT, DCTERMS.CREATED, VF.createLiteral("2024-01-15T10:00:00", XSD.DATETIME), GRAPH));
    }

    @Test
    void add_date_adds_a_typed_date() {
        RdfTriples.addDate(SUBJECT, DCTERMS.VALID, "2024-01-15T10:00:00.000", model, GRAPH);

        assertThat(model)
                .containsExactly(
                        VF.createStatement(SUBJECT, DCTERMS.VALID, VF.createLiteral("2024-01-15", XSD.DATE), GRAPH));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "")
    void addDateTime_and_addDate_ignore_a_missing_value(String value) {
        RdfTriples.addDateTime(SUBJECT, DCTERMS.CREATED, value, model, GRAPH);
        RdfTriples.addDate(SUBJECT, DCTERMS.VALID, value, model, GRAPH);

        assertThat(model).isEmpty();
    }

    /**
     * Contrairement à {@link RdfTriples#iri(String)}, la valeur est reprise sans retouche, pour
     * les appelants qui en dépendent.
     */
    @Test
    void iri_as_is_builds_an_iri_without_trimming() {
        assertThat(RdfTriples.iriAsIs(" http://bauhaus/concept/c1"))
                .isEqualTo(VF.createIRI(" http://bauhaus/concept/c1"));
    }

    @Test
    void literal_keeps_its_value_and_datatype() {
        assertThat(RdfTriples.literal(" 2024 ", XSD.GYEAR)).isEqualTo(VF.createLiteral(" 2024 ", XSD.GYEAR));
    }

    @Test
    void blank_node_creates_a_fresh_blank_node_each_time() {
        assertThat(RdfTriples.blankNode()).isNotNull().isNotEqualTo(RdfTriples.blankNode());
    }

    @Test
    void add_int_adds_an_xsd_int_literal() {
        RdfTriples.addInt(SUBJECT, DCTERMS.VALID, "3", model, GRAPH);

        assertThat(model)
                .containsExactly(VF.createStatement(SUBJECT, DCTERMS.VALID, VF.createLiteral("3", XSD.INT), GRAPH));
    }

    @Test
    void add_string_md_to_xhtml_adds_the_markdown_converted_to_xhtml() {
        RdfTriples.addStringMdToXhtml(SUBJECT, DCTERMS.ABSTRACT, "**gras**", "fr", model, GRAPH);

        assertThat(model)
                .containsExactly(VF.createStatement(
                        SUBJECT,
                        DCTERMS.ABSTRACT,
                        VF.createLiteral(
                                XhtmlToMarkdownUtils.markdownToXhtml("**gras**").trim(), "fr"),
                        GRAPH));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "")
    void add_int_and_add_string_md_to_xhtml_ignore_a_missing_value(String value) {
        RdfTriples.addInt(SUBJECT, DCTERMS.VALID, value, model, GRAPH);
        RdfTriples.addStringMdToXhtml(SUBJECT, DCTERMS.ABSTRACT, value, "fr", model, GRAPH);

        assertThat(model).isEmpty();
    }

    @Test
    void year_literal_keeps_only_the_day_and_is_typed_as_xsd_g_year() {
        assertThat(RdfTriples.year("2024-03-15T10:00:00")).isEqualTo(VF.createLiteral("2024-03-15", XSD.GYEAR));
    }

    @Test
    void add_b_node_adds_a_blank_node_object() {
        BNode node = VF.createBNode();

        RdfTriples.addBNode(SUBJECT, DCTERMS.TEMPORAL, node, model, GRAPH);

        assertThat(model).containsExactly(VF.createStatement(SUBJECT, DCTERMS.TEMPORAL, node, GRAPH));
    }

    @Test
    void add_b_node_ignores_a_null_node() {
        RdfTriples.addBNode(SUBJECT, DCTERMS.TEMPORAL, null, model, GRAPH);

        assertThat(model).isEmpty();
    }
}
