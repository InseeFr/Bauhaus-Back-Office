package fr.insee.rmes.testcontainers.queries.sparql_queries.structures;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.modules.structures.infrastructure.graphdb.StructureQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.json.JSONObject;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Tag("integration")
class StructureQueriesTest extends WithGraphDBContainer {

    RepositoryGestion repositoryGestion = new RepositoryGestion(getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));
    StructureQueries structureQueries = new StructureQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());

    @BeforeAll
    static void initData() {
        container.withTrigFiles("jeuxDeDonnees-pour-tests.trig");
        container.withTrigFiles("a6-appariement-variables-it.trig");
    }

    @Test
    void should_return_false_if_existing_structure_with_same_components_and_id_null() throws Exception {
        boolean result = repositoryGestion.getResponseAsBoolean(structureQueries.checkUnicityStructure(null, List.of("2").toArray(new String[0])));
        assertFalse(result);
    }

    @Test
    void getUriClasseOwl_returns_the_owl_class_referencing_the_code_list() throws Exception {
        JSONObject result = repositoryGestion.getResponseAsObject(
                structureQueries.getUriClasseOwl("http://bauhaus/codes/listeAvecClasseA6"));

        assertThat(result.getString("uriClasseOwl")).isEqualTo("http://bauhaus/codes/classeOwlA6");
    }

    @Test
    void getUriClasseOwl_ignores_a_resource_referencing_the_code_list_without_being_an_owl_class() throws Exception {
        JSONObject result = repositoryGestion.getResponseAsObject(
                structureQueries.getUriClasseOwl("http://bauhaus/codes/listeSansClasseA6"));

        assertThat(result.isEmpty())
                .as("la contrainte rdf:type owl:Class doit porter sur la variable projetée")
                .isTrue();
    }
}