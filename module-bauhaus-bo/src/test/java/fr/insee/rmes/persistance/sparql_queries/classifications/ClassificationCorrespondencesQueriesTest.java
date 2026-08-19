package fr.insee.rmes.persistance.sparql_queries.classifications;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClassificationCorrespondencesQueriesTest {

    private ClassificationCorrespondencesQueries queries;

    @BeforeEach
    void setUp() {
        queries = new ClassificationCorrespondencesQueries(new BauhausLanguagesProperties("fr", "en"));
    }

    @Test
    void correspondencesQuery_returnsExpectedSparql() throws RmesException {
        String sparql = normalize(queries.correspondencesQuery());

        assertThat(sparql).isEqualTo(normalize("""
                SELECT DISTINCT ?id ?label
                WHERE {
                    ?correspondence rdf:type xkos:Correspondence .
                    ?correspondence skos:prefLabel ?label .
                    FILTER (lang(?label) = \"fr\")
                    BIND(STRAFTER(STR(?correspondence),'/codes/') AS ?id)
                }
                ORDER BY ?label
                """));
    }

    @Test
    void correspondenceQuery_substitutesIdsAndLanguages() throws RmesException {
        String sparql = normalize(queries.correspondenceQuery("nafr2-cpfr21"));

        assertThat(sparql)
                .contains("FILTER(STRENDS(STR(?correspondence),\"/codes/nafr2-cpfr21\"))")
                .contains("FILTER(REGEX(STR(?firstClassURI),\"/codes/nafr2/\"))")
                .contains("BIND(\"nafr2\" as ?idFirstClass)")
                .contains("FILTER(REGEX(STR(?secondClassURI),\"/codes/cpfr21/\"))")
                .contains("BIND(\"cpfr21\" as ?idSecondClass)")
                .contains("FILTER (lang(?labelLg1) = \"fr\")")
                .contains("FILTER (lang(?labelLg2) = \"en\")")
                .contains("LIMIT 1");
    }

    @Test
    void correspondenceAssociationsQuery_substitutesCorrespondenceIdAndLanguages() throws RmesException {
        String sparql = normalize(queries.correspondenceAssociationsQuery("nafr2-cpfr21"));

        assertThat(sparql)
                .contains("?correspondence xkos:madeOf ?association .")
                .contains("FILTER(REGEX(STR(?correspondence),\"/codes/nafr2-cpfr21\"))")
                .contains("BIND(STRAFTER(STR(?association),'/association/') AS ?id)")
                .contains("FILTER (lang(?sourceLabelLg1) = \"fr\")")
                .contains("FILTER (lang(?sourceLabelLg2) = \"en\")")
                .contains("FILTER (lang(?targetLabelLg1) = \"fr\")")
                .contains("FILTER (lang(?targetLabelLg2) = \"en\")");
    }

    @Test
    void correspondenceAssociationQuery_substitutesAllIdsAndLanguages() throws RmesException {
        String sparql = normalize(queries.correspondenceAssociationQuery("nafr2-cpfr21", "001-A01"));

        assertThat(sparql)
                .contains("FILTER(STRENDS(STR(?correspondence),\"/codes/nafr2-cpfr21\"))")
                .contains("BIND(\"001-A01\" as ?associationId)")
                .contains("FILTER(STRENDS(STR(?association),\"/codes/nafr2-cpfr21/association/001-A01\"))")
                .contains("FILTER(REGEX(STR(?sourceClassURI),\"/codes/nafr2/\"))")
                .contains("BIND(\"nafr2\" as ?sourceClassId)")
                .contains("FILTER(REGEX(STR(?targetClassURI),\"/codes/cpfr21/\"))")
                .contains("BIND(\"cpfr21\" as ?targetClassId)")
                .contains("BIND(\"001\" as ?sourceItemId)")
                .contains("BIND(\"A01\" as ?targetItemId)")
                .contains("?scopeLg1 dcterms:language \"fr\"^^xsd:language")
                .contains("?scopeLg2 dcterms:language \"en\"^^xsd:language")
                .contains("LIMIT 1");
    }

    private static String normalize(String sparql) {
        return sparql.replaceAll("\\s+", " ").trim();
    }
}
