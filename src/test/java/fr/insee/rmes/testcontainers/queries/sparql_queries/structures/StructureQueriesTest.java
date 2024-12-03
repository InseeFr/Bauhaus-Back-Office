package fr.insee.rmes.testcontainers.queries.sparql_queries.structures;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryInitiator;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryUtils;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.persistance.sparql_queries.structures.StructureQueries;
import fr.insee.rmes.testcontainers.queries.WithGraphDBContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class StructureQueriesTest extends WithGraphDBContainer {
    RepositoryGestion repositoryGestion = new RepositoryGestion(getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    @BeforeAll
    static void initData(){
        container.withTrigFiles("jeuxDeDonnees-pour-tests.trig");
    }

    @Test
    void should_return_false_if_existing_structure_with_same_components_and_id_null() throws Exception {
        StructureQueries.setConfig(new ConfigStub());
        boolean result = repositoryGestion.getResponseAsBoolean(StructureQueries.checkUnicityStructure(null, List.of("2").toArray(new String[0])));
        assertFalse(result);
    }
}