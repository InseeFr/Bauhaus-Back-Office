package fr.insee.rmes.persistance.sparql_queries.concepts;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.junit.jupiter.api.Test;

import static fr.insee.rmes.persistance.sparql_queries.SparqlQueryNormalizer.normalize;
import static org.assertj.core.api.Assertions.assertThat;

class ConceptConceptsQueriesTest {

    private final ConceptConceptsQueries queries =
            new ConceptConceptsQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());

    @Test
    void checkIfExists_returnsExpectedAskQuery() throws RmesException {
        String sparql = normalize(queries.checkIfExists("c1000"));

        assertThat(sparql).isEqualTo(normalize("""
                ASK
                WHERE
                { ?uri ?b ?c .
                FILTER(STRENDS(STR(?uri),\"/concepts/definition/c1000\")) . }
                """));
    }
}
