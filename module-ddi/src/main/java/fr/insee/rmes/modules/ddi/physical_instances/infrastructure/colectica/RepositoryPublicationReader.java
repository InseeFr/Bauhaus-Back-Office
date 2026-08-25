package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryUtils;
import org.eclipse.rdf4j.repository.Repository;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Accès en lecture seule au dépôt GraphDB de <b>publication</b> depuis module-ddi.
 * <p>
 * module-ddi ne dépend pas de module-bauhaus-bo, où vit {@code RepositoryPublication} ;
 * ce composant fournit donc le minimum nécessaire (requête SPARQL → {@link JSONArray})
 * en s'appuyant sur {@link RepositoryUtils} et les propriétés de connexion publication.
 * Il sert à retrouver, lors de l'initialisation Colectica, les series et operations
 * <em>publiées</em> : leurs IRIs sont alors en base de publication
 * ({@code http://id.insee.fr/...}), cohérentes avec la clé de recherche de l'endpoint
 * {@code GET /ddi/operation/{id}/studyUnit}.
 */
@Component
public class RepositoryPublicationReader {

    private final RepositoryUtils repositoryUtils;
    private final String sesameServer;
    private final String repositoryId;

    public RepositoryPublicationReader(
        RepositoryUtils repositoryUtils,
        @Value("${fr.insee.rmes.bauhaus.sesame.publication.sesameServer}") String sesameServer,
        @Value("${fr.insee.rmes.bauhaus.sesame.publication.repository}") String repositoryId
    ) {
        this.repositoryUtils = repositoryUtils;
        this.sesameServer = sesameServer;
        this.repositoryId = repositoryId;
    }

    public JSONArray getResponseAsArray(String query) throws RmesException {
        Repository repository = repositoryUtils.initRepository(sesameServer, repositoryId);
        return repositoryUtils.getResponseAsArray(query, repository);
    }
}
