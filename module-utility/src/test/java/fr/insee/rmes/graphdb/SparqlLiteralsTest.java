package fr.insee.rmes.graphdb;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.eclipse.rdf4j.rio.helpers.NTriplesUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SparqlLiteralsTest {

    private static final ValueFactory FACTORY = SimpleValueFactory.getInstance();

    private static final String INJECTION_PAYLOAD = "x\" . } DELETE { ?s ?p ?o } # ";

    @Test
    void shouldWrapAPlainValueInDoubleQuotes() {
        assertEquals("\"simple\"", SparqlLiterals.literal("simple"));
    }

    @Test
    void shouldEscapeTheDoubleQuotesThatWouldCloseTheLiteral() {
        assertEquals("\"a\\\"b\"", SparqlLiterals.literal("a\"b"));
    }

    @Test
    void shouldEscapeBackslashes() {
        assertEquals("\"a\\\\b\"", SparqlLiterals.literal("a\\b"));
    }

    @Test
    void shouldEscapeLineBreaksAndTabulations() {
        assertEquals("\"line1\\nline2\\rline3\\tend\"", SparqlLiterals.literal("line1\nline2\rline3\tend"));
    }

    @Test
    void shouldEscapeNonAsciiCharacters() {
        assertEquals("\"Libell\\u00E9\"", SparqlLiterals.literal("Libellé"));
    }

    @Test
    void shouldEscapeAnEmptyValueAsAnEmptyLiteral() {
        assertEquals("\"\"", SparqlLiterals.literal(""));
    }

    @Test
    void shouldPreserveTheOriginalValueWhenTheLiteralIsParsedBack() {
        Literal parsed = NTriplesUtil.parseLiteral(SparqlLiterals.literal(INJECTION_PAYLOAD), FACTORY);

        assertEquals(INJECTION_PAYLOAD, parsed.getLabel());
    }

    @Test
    void shouldRejectANullValue() {
        assertThrows(IllegalArgumentException.class, () -> SparqlLiterals.literal(null));
    }

    @Test
    void shouldKeepTheQueryParsableWhenTheValueCarriesAnInjectionPayload() {
        String query = "SELECT * WHERE { ?s ?p " + SparqlLiterals.literal(INJECTION_PAYLOAD) + " }";

        assertDoesNotThrow(() -> QueryParserUtil.parseQuery(QueryLanguage.SPARQL, query, null));
    }

    @Test
    void shouldBreakTheQueryWhenTheSamePayloadIsConcatenatedNaively() {
        String query = "SELECT * WHERE { ?s ?p \"" + INJECTION_PAYLOAD + "\" }";

        assertThrows(MalformedQueryException.class,
                () -> QueryParserUtil.parseQuery(QueryLanguage.SPARQL, query, null));
    }

    @Test
    void shouldAppendTheLanguageTagToTheLiteral() {
        assertEquals("\"Libell\\u00E9\"@fr", SparqlLiterals.literal("Libellé", "fr"));
    }

    @Test
    void shouldAcceptALanguageTagWithASubtag() {
        assertEquals("\"colour\"@en-GB", SparqlLiterals.literal("colour", "en-GB"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"fr\" . } DELETE { ?s ?p ?o } #", "fr fr", "", "-fr", "fr-", "fr_BE", "1fr"})
    void shouldRejectALanguageTagThatIsNotAValidLangtag(String languageTag) {
        assertThrows(IllegalArgumentException.class, () -> SparqlLiterals.literal("valeur", languageTag));
    }

    @Test
    void shouldRejectANullLanguageTag() {
        assertThrows(IllegalArgumentException.class, () -> SparqlLiterals.literal("valeur", null));
    }

    @Test
    void shouldWrapAnIriInAngleBrackets() {
        assertEquals("<http://rdf.insee.fr/def/base#Concept>",
                SparqlLiterals.iri("http://rdf.insee.fr/def/base#Concept"));
    }

    @Test
    void shouldEscapeNonAsciiCharactersInAnIri() {
        assertEquals("<http://example.org/caf\\u00E9>", SparqlLiterals.iri("http://example.org/café"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://example.org/a> . } DELETE { ?s ?p ?o } #",
            "http://example.org/a<b",
            "http://example.org/a b",
            "http://example.org/a\"b",
            "http://example.org/a{b",
            "http://example.org/a}b",
            "http://example.org/a|b",
            "http://example.org/a^b",
            "http://example.org/a`b",
            "http://example.org/a\\b",
            "http://example.org/a\nb"
    })
    void shouldRejectAnIriContainingACharacterForbiddenInAnIriRef(String iri) {
        assertThrows(IllegalArgumentException.class, () -> SparqlLiterals.iri(iri));
    }

    @Test
    void shouldRejectARelativeIri() {
        assertThrows(IllegalArgumentException.class, () -> SparqlLiterals.iri("/def/base#Concept"));
    }

    @Test
    void shouldRejectANullIri() {
        assertThrows(IllegalArgumentException.class, () -> SparqlLiterals.iri(null));
    }

    @Test
    void shouldKeepTheQueryParsableWhenTheIriIsInjectedInATriplePattern() {
        String query = "SELECT * WHERE { " + SparqlLiterals.iri("http://example.org/café") + " ?p ?o }";

        assertDoesNotThrow(() -> QueryParserUtil.parseQuery(QueryLanguage.SPARQL, query, null));
    }

    @Test
    void shouldPrefixAVariableNameWithAQuestionMark() {
        assertEquals("?labelLg1", SparqlLiterals.variable("labelLg1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "o } ORDER BY ?x #",
            "o ?x",
            "o.x",
            "o-x",
            "",
            "?o",
            "s ?p ?o"
    })
    void shouldRejectAVariableNameThatIsNotAValidVarname(String name) {
        assertThrows(IllegalArgumentException.class, () -> SparqlLiterals.variable(name));
    }

    @Test
    void shouldRejectANullVariableName() {
        assertThrows(IllegalArgumentException.class, () -> SparqlLiterals.variable(null));
    }

    @Test
    void shouldKeepTheQueryParsableWhenTheVariableIsInjectedInAnOrderByClause() {
        String query = "SELECT * WHERE { ?s ?p ?o } ORDER BY " + SparqlLiterals.variable("o");

        assertDoesNotThrow(() -> QueryParserUtil.parseQuery(QueryLanguage.SPARQL, query, null));
    }
}
