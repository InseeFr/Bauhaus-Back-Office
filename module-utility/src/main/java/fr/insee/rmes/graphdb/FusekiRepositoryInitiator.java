package fr.insee.rmes.graphdb;

import java.util.Optional;
import org.eclipse.rdf4j.http.client.HttpClientSessionManager;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

/**
 * Fuseki ne sert que SPARQL 1.1 : le dépôt passe par les points d'accès {@code /sparql} et {@code /update} du
 * dataset, là où GraphDB parle le protocole REST RDF4J.
 */
public class FusekiRepositoryInitiator implements RepositoryInitiator {

    private final Optional<RdfBasicCredentials> credentials;

    public FusekiRepositoryInitiator(Optional<RdfBasicCredentials> credentials) {
        this.credentials = credentials;
    }

    @Override
    public Repository initRepository(
            String rdfServer, String repositoryID, HttpClientSessionManager httpClientSessionManager) {
        String datasetUrl = rdfServer + "/" + repositoryID;
        var repository = new SilentClearSparqlRepository(datasetUrl + "/sparql", datasetUrl + "/update");
        repository.setHttpClientSessionManager(httpClientSessionManager);
        credentials.ifPresent(basic -> repository.setUsernameAndPassword(basic.username(), basic.password()));
        repository.init();
        return repository;
    }

    /**
     * Vider un graphe absent est une erreur pour Fuseki, pas pour GraphDB : sans {@code CLEAR SILENT}, la première
     * publication d'un graphe échoue (décision de portabilité SPARQL, C3).
     */
    private static final class SilentClearSparqlRepository extends SPARQLRepository {

        private SilentClearSparqlRepository(String queryEndpointUrl, String updateEndpointUrl) {
            super(queryEndpointUrl, updateEndpointUrl);
        }

        @Override
        public RepositoryConnection getConnection() {
            var connection = (SPARQLConnection) super.getConnection();
            connection.setSilentClear(true);
            return connection;
        }
    }
}
