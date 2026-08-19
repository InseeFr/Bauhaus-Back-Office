package fr.insee.rmes.testcontainers.queries.sparql_queries.structures;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.graphdb.ontologies.QB;
import fr.insee.rmes.modules.structures.infrastructure.graphdb.StructureQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code StructureQueries.lastId} alimente le compteur des composants mutualisés :
 * {@code StructureComponentUtils.generateNextId} rend « d »/« m »/« a » + (dernier identifiant + 1).
 * Le graphe des composants contient d'autres ressources du même rdf:type que les composants
 * mutualisés ; seule une exécution réelle montre si elles polluent le compteur.
 */
@Tag("integration")
class StructureComponentLastIdIntegrationTest extends WithGraphDBContainer {

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(),
            new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    private final StructureQueries structureQueries = new StructureQueries(
            new BauhausLanguagesProperties("fr", "en"),
            GraphsPropertiesStub.stub("operations", "a9-composants"));

    @BeforeAll
    static void initData() {
        container.withTrigFiles("a9-dernier-identifiant-it.trig");
    }

    @Test
    void lastId_returns_the_greatest_identifier_of_the_requested_component_type() throws RmesException {
        JSONObject result = repositoryGestion.getResponseAsObject(
                structureQueries.lastId("d", QB.DIMENSION_PROPERTY.stringValue()));

        assertThat(result.getString("id"))
                .as("d1102 est le plus grand identifiant de dimension du graphe")
                .isEqualTo("1102");
    }

    @Test
    void lastId_ignores_a_resource_of_the_same_type_that_declares_no_identifier() throws RmesException {
        JSONObject result = repositoryGestion.getResponseAsObject(
                structureQueries.lastId("d", QB.DIMENSION_PROPERTY.stringValue()));

        assertThat(result.getString("id"))
                .as("la dimension imbriquée .../cs1/dimension/99999 n'est pas un composant mutualisé")
                .isNotEqualTo("9999");
    }

    @Test
    void lastId_does_not_leak_the_identifier_of_another_component_type() throws RmesException {
        JSONObject result = repositoryGestion.getResponseAsObject(
                structureQueries.lastId("m", QB.MEASURE_PROPERTY.stringValue()));

        assertThat(result.getString("id"))
                .as("m1300 est le seul identifiant de mesure du graphe")
                .isEqualTo("1300");
    }
}
