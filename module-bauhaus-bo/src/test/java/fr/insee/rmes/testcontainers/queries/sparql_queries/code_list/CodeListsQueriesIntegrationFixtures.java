package fr.insee.rmes.testcontainers.queries.sparql_queries.code_list;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.PaginationProperties;
import fr.insee.rmes.config.BauhausUriPropertiesStub;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.graphdb.RdfConnectionDetails;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.modules.codeslists.codeslists.infrastructure.graphdb.CodeListsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;

/** Dépôt de gestion et requêtes des listes de codes branchés sur le conteneur GraphDB des tests. */
final class CodeListsQueriesIntegrationFixtures {

    private CodeListsQueriesIntegrationFixtures() {}

    static RepositoryGestion repositoryGestion(RdfConnectionDetails connectionDetails) {
        return new RepositoryGestion(connectionDetails, new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));
    }

    static CodeListsQueries codeListsQueries() {
        return new CodeListsQueries(
                BauhausUriPropertiesStub.stub(),
                new BauhausLanguagesProperties("fr", "en"),
                GraphsPropertiesStub.stub(),
                new PaginationProperties(5));
    }
}
