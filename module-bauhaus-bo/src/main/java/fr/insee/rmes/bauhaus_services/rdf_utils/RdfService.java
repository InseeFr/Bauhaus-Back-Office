package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.IdGenerator;
import org.eclipse.rdf4j.model.*;

public abstract class RdfService {

    protected final RepositoryGestion repoGestion;

    protected final IdGenerator idGenerator;

    protected final RepositoryPublication repositoryPublication;

    protected final PublicationUtils publicationUtils;

    protected RdfService(
            RepositoryGestion repoGestion,
            IdGenerator idGenerator,
            RepositoryPublication repositoryPublication,
            PublicationUtils publicationUtils) {
        this.repoGestion = repoGestion;
        this.idGenerator = idGenerator;
        this.repositoryPublication = repositoryPublication;
        this.publicationUtils = publicationUtils;
    }

    /**
     * Recopie le triplet vers la base de publication, dans le graphe donné : le contexte d'un triplet
     * relu est {@code null} hors de GraphDB, le graphe vient donc de l'appelant.
     */
    public void transformTripleToPublish(Model model, Statement st, Resource graph) {
        Resource subject = publicationUtils.tranformBaseURIToPublish(st.getSubject());
        IRI predicateIRI = RdfUtils.createIRI(
                publicationUtils.tranformBaseURIToPublish(st.getPredicate()).stringValue());
        Value object = st.getObject();
        if (st.getObject() instanceof Resource resource) {
            object = publicationUtils.tranformBaseURIToPublish(resource);
        }

        model.add(subject, predicateIRI, object, graph);
    }
}
