package fr.insee.rmes.persistance.sparql_queries.classifications;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClassificationFamiliesQueriesTest {

    private final ClassificationFamiliesQueries queries =
            new ClassificationFamiliesQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());

    @Test
    void familyQuery_returnsExpectedSparql() throws RmesException {
        String sparql = normalize(queries.familyQuery("nafr2"));

        assertThat(sparql).isEqualTo(normalize("""
                SELECT ?prefLabelLg1
                WHERE {
                    GRAPH<http://rdf.insee.fr/graphes/codes/nomenclatures> {
                        ?family skos:prefLabel ?prefLabelLg1 .
                        FILTER (lang(?prefLabelLg1) = 'fr')
                        FILTER(REGEX(STR(?family),'/familleDeNomenclatures/nafr2'))
                    }
                }
                """));
    }

    @Test
    void familyMembersQuery_returnsExpectedSparql() throws RmesException {
        String sparql = normalize(queries.familyMembersQuery("nafr2"));

        assertThat(sparql).isEqualTo(normalize("""
                SELECT DISTINCT ?id ?labelLg1 ?labelLg2
                WHERE {
                    ?series xkos:belongsTo ?family .
                    ?series skos:prefLabel ?labelLg1 .
                    FILTER (lang(?labelLg1) = 'fr')
                    OPTIONAL {?series skos:prefLabel ?labelLg2 .
                    FILTER (lang(?labelLg2) = 'en') }
                    FILTER(REGEX(STR(?family),'/familleDeNomenclatures/nafr2')) .
                    BIND(STRAFTER(STR(?series),'/codes/serieDeNomenclatures/') AS ?id)
                }
                ORDER BY ?labelLg1
                """));
    }

    private static String normalize(String sparql) {
        return sparql.replaceAll("\\s+", " ").trim();
    }
}
