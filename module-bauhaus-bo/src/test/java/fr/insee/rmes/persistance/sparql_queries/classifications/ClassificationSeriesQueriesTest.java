package fr.insee.rmes.persistance.sparql_queries.classifications;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClassificationSeriesQueriesTest {

    private final ClassificationSeriesQueries queries =
            new ClassificationSeriesQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());

    @Test
    void oneSeriesQuery_returnsExpectedSparql() throws RmesException {
        String sparql = normalize(queries.oneSeriesQuery("nafr2"));

        assertThat(sparql).isEqualTo(normalize("""
                SELECT ?id ?prefLabelLg1 ?prefLabelLg2 ?altLabelLg1 ?altLabelLg2
                ?scopeNoteLg1 ?scopeNoteLg2 ?subject ?publishers ?covers ?familyLg1 ?familyLg2 ?idFamily
                WHERE { GRAPH<http://rdf.insee.fr/graphes/codes/nomenclatures> {
                ?series skos:prefLabel ?prefLabelLg1 .
                FILTER(REGEX(STR(?series),\"/serieDeNomenclatures/nafr2\")) .
                BIND(STRAFTER(STR(?series),'/codes/serieDeNomenclatures/') AS ?id) .
                FILTER (lang(?prefLabelLg1) = \"fr\") .
                OPTIONAL {?series skos:prefLabel ?prefLabelLg2 .
                FILTER (lang(?prefLabelLg2) = \"en\") } .
                {OPTIONAL{
                SELECT (group_concat(?altLg1;separator=' || ') as ?altLabelLg1) WHERE {
                ?series skos:altLabel ?altLg1 .
                FILTER (lang(?altLg1) = \"fr\")  .
                FILTER(REGEX(STR(?series),\"/codes/serieDeNomenclatures/nafr2\")) .
                }}}
                {OPTIONAL{
                SELECT (group_concat(?altLg2;separator=' || ') as ?altLabelLg2) WHERE {
                ?series skos:altLabel ?altLg2 .
                FILTER (lang(?altLg2) = \"en\")  .
                FILTER(REGEX(STR(?series),\"/codes/serieDeNomenclatures/nafr2\")) .
                }}}
                OPTIONAL {?series dc:subject ?subject } .
                OPTIONAL {?series dc:publisher ?publishers } .
                OPTIONAL {?series xkos:covers ?covers } .
                OPTIONAL {?series skos:scopeNote ?scopeLg1 .
                ?scopeLg1 dcterms:language \"fr\"^^xsd:language .
                ?scopeLg1 evoc:noteLiteral ?scopeNoteLg1 .
                } .
                OPTIONAL {?series skos:scopeNote ?scopeLg2 .
                ?scopeLg2 dcterms:language \"en\"^^xsd:language .
                ?scopeLg2 evoc:noteLiteral ?scopeNoteLg2 .
                } .
                OPTIONAL {?series xkos:belongsTo ?familyURI .
                ?familyURI skos:prefLabel ?familyLg1 .
                FILTER (lang(?familyLg1) = \"fr\")  .
                BIND(STRAFTER(STR(?familyURI),'/codes/familleDeNomenclatures/') AS ?idFamily) } .
                OPTIONAL {?series xkos:belongsTo ?familyURI .
                ?familyURI skos:prefLabel ?familyLg2 .
                FILTER (lang(?familyLg2) = \"en\") }  .
                }}
                LIMIT 1
                """));
    }

    @Test
    void seriesMembersQuery_returnsExpectedSparql() throws RmesException {
        String sparql = normalize(queries.seriesMembersQuery("nafr2"));

        assertThat(sparql).isEqualTo(normalize("""
                SELECT DISTINCT ?id ?labelLg1 ?labelLg2
                WHERE {
                ?classification xkos:belongsTo ?series .
                ?classification skos:prefLabel ?labelLg1 .
                FILTER (lang(?labelLg1) = \"fr\")
                OPTIONAL {?classification skos:prefLabel ?labelLg2 .
                FILTER (lang(?labelLg2) = \"en\") }
                FILTER(REGEX(STR(?series),\"/serieDeNomenclatures/nafr2\")) .
                BIND(STRBEFORE(STRAFTER(STR(?classification),'/codes/'), '/') AS ?id)
                }
                ORDER BY ?labelLg1
                """));
    }

    private static String normalize(String sparql) {
        return sparql.replaceAll("\\s+", " ").trim();
    }
}
