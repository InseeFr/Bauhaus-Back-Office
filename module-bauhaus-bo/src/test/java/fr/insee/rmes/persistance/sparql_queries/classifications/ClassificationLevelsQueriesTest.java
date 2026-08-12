package fr.insee.rmes.persistance.sparql_queries.classifications;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClassificationLevelsQueriesTest {

    private final ClassificationLevelsQueries queries =
            new ClassificationLevelsQueries(new BauhausLanguagesProperties("fr", "en"));

    @Test
    void levelQuery_returnsExpectedSparql() throws RmesException {
        String sparql = normalize(queries.levelQuery("nafr2", "niveau1"));

        assertThat(sparql).isEqualTo(normalize("""
                SELECT ?classificationId ?levelId ?prefLabelLg1 ?prefLabelLg2 ?depth ?notation
                ?notationPattern ?broaderLg1 ?broaderLg2 ?idBroader ?narrowerLg1 ?narrowerLg2 ?idNarrower
                WHERE {
                ?level rdf:type xkos:ClassificationLevel .
                FILTER(STRENDS(STR(?level),'/codes/nafr2/niveau1'))
                BIND(STRAFTER(STRAFTER(STR(?level),'/codes/'), '/') AS ?levelId)
                BIND(STRBEFORE(STRAFTER(STR(?level),'/codes/'), '/') AS ?classificationId)
                ?level skos:prefLabel ?prefLabelLg1 .
                FILTER (lang(?prefLabelLg1) = 'fr')
                OPTIONAL {?level skos:prefLabel ?prefLabelLg2 .
                FILTER (lang(?prefLabelLg2) = 'en') }
                ?level xkos:depth ?depth .
                ?level skos:notation ?notation .
                ?level xkos:notationPattern ?notationPattern .
                OPTIONAL {?node rdf:first ?level .
                OPTIONAL {?node rdf:rest ?nextNode .
                ?nextNode rdf:first ?nextLevel .
                ?nextLevel skos:prefLabel ?narrowerLg1 .
                FILTER (lang(?narrowerLg1) = 'fr')
                OPTIONAL {?nextLevel skos:prefLabel ?narrowerLg2 .
                FILTER (lang(?narrowerLg2) = 'en') }
                BIND(STRAFTER(STRAFTER(STR(?nextLevel),'/codes/'), '/') AS ?idNarrower) }
                OPTIONAL {?previousNode rdf:rest ?node .
                ?previousNode rdf:first ?previousLevel .
                ?previousLevel skos:prefLabel ?broaderLg1 .
                FILTER (lang(?broaderLg1) = 'fr')
                OPTIONAL {?previousLevel skos:prefLabel ?broaderLg2 .
                FILTER (lang(?broaderLg2) = 'en') }
                BIND(STRAFTER(STRAFTER(STR(?previousLevel),'/codes/'), '/') AS ?idBroader) }
                }
                }
                """));
    }

    @Test
    void levelMembersQuery_returnsExpectedSparql() throws RmesException {
        String sparql = normalize(queries.levelMembersQuery("nafr2", "niveau1"));

        assertThat(sparql).isEqualTo(normalize("""
                SELECT DISTINCT ?item ?id ?labelLg1 ?labelLg2
                WHERE {
                ?level rdf:type xkos:ClassificationLevel .
                FILTER(STRENDS(STR(?level),'/codes/nafr2/niveau1'))
                ?level skos:member ?item .
                ?item skos:prefLabel ?labelLg1 .
                FILTER (lang(?labelLg1) = 'fr')
                OPTIONAL {?item skos:prefLabel ?labelLg2 .
                FILTER (lang(?labelLg2) = 'en') } .
                ?item skos:notation ?id .
                }
                ORDER BY ?id
                """));
    }

    private static String normalize(String sparql) {
        return sparql.replaceAll("\\s+", " ").trim();
    }
}
