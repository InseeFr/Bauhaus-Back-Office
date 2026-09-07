package fr.insee.rmes.persistance.sparql_queries.classifications;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClassificationsQueriesTest {

    private final ClassificationsQueries classificationsQueries =
            new ClassificationsQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());

    @Test
    void classificationQuery_readsValidationStateFromTheNomenclaturesGraph() throws RmesException {
        String query = classificationsQueries.classificationQuery("cpfr21");

        assertThat(query).contains(
                "GRAPH <http://rdf.insee.fr/graphes/codes/nomenclatures> { ?classification insee:validationState ?validationState }");
    }
}
