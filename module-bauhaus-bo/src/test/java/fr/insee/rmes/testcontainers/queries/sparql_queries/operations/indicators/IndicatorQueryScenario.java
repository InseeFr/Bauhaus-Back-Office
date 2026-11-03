package fr.insee.rmes.testcontainers.queries.sparql_queries.operations.indicators;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.BauhausUriPropertiesStub;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationIndicatorsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

/**
 * Lecture d'un indicateur, rejouée sur chaque triplestore (voir {@link IndicatorQueryGraphDBIntegrationTest}
 * et {@link IndicatorQueryFusekiIntegrationTest}).
 *
 * <p>L'indicateur p1665 des fixtures n'a pas de date de création. Une requête dont le premier motif
 * est un {@code OPTIONAL} sur cette date lie d'abord l'indicateur à tout objet daté de la base : dès
 * qu'il en existe un, p1665 n'était plus rendu sur Fuseki.
 */
interface IndicatorQueryScenario {

    RepositoryGestion repositoryGestion();

    @Test
    default void reads_an_indicator_without_creation_date() throws RmesException {
        repositoryGestion().executeUpdate("""
                INSERT DATA { GRAPH <http://rdf.insee.fr/graphes/produits> {
                  <http://bauhaus/produits/indicateur/p9999> <http://purl.org/dc/terms/created> "2026-01-01T00:00:00" .
                } }
                """);
        OperationIndicatorsQueries queries = new OperationIndicatorsQueries(
                BauhausUriPropertiesStub.stub(),
                new BauhausLanguagesProperties("fr", "en"),
                GraphsPropertiesStub.stub());

        JSONObject indicator = repositoryGestion().getResponseAsObject(queries.indicatorQuery("p1665"));

        assertThat(indicator.optString("prefLabelLg1")).isEqualTo("Commerce extérieur en valeur de la France");
        assertThat(indicator.optString("validationState")).isEqualTo("Validated");
    }
}
