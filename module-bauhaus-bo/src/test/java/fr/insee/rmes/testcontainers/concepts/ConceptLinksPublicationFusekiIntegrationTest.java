package fr.insee.rmes.testcontainers.concepts;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RdfBasicCredentials;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.testcontainers.FusekiContainer;
import fr.insee.rmes.testcontainers.WithFusekiContainer;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Publication d'un concept sur Fuseki : les liens entrants qu'il ne porte plus doivent disparaître de
 * la base de publication, alors qu'un {@code remove} sans graphe nommé n'y supprime rien.
 */
@Tag("integration")
class ConceptLinksPublicationFusekiIntegrationTest extends WithFusekiContainer {

    private static final IRI GRAPH = Values.iri("http://rdf.insee.fr/graphes/concepts");
    private static final IRI PARENT = Values.iri("http://id.insee.fr/concepts/definition/c9201");
    private static final IRI CHILD = Values.iri("http://id.insee.fr/concepts/definition/c9202");

    private final RepositoryPublication repositoryPublication = new RepositoryPublication(
            getRdfGestionConnectionDetails().getUrlServer(),
            getRdfGestionConnectionDetails().repositoryId(),
            new RepositoryUtils(
                    null,
                    RepositoryInitiator.Type.DISABLED,
                    "fuseki",
                    new RdfBasicCredentials(FusekiContainer.ADMIN_USER, FusekiContainer.ADMIN_PASSWORD)));

    @Test
    void publishConcept_removes_the_broader_links_pointing_to_the_concept() throws RmesException {
        repositoryPublication.executeUpdate(
                "INSERT DATA { GRAPH <%s> { <%s> <%s> <%s> } }".formatted(GRAPH, CHILD, SKOS.BROADER, PARENT));
        Model parent = new LinkedHashModel();
        parent.add(PARENT, SKOS.PREF_LABEL, Values.literal("Parent sans enfant", "fr"), GRAPH);

        repositoryPublication.publishConcept(PARENT, parent, List.of(), List.of(), GRAPH);

        assertThat(repositoryPublication
                        .getResponseAsArray("SELECT ?g WHERE { GRAPH ?g { <%s> <%s> <%s> } }"
                                .formatted(CHILD, SKOS.BROADER, PARENT))
                        .length())
                .isZero();
    }
}
