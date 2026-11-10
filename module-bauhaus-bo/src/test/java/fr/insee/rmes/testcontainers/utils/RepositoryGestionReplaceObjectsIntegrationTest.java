package fr.insee.rmes.testcontainers.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.rdf_utils.SubjectModelGraph;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * {@link RepositoryGestion#replaceObjects} : les mises à jour préalables et le remplacement des
 * objets forment une seule transaction, qui s'applique en entier ou pas du tout.
 */
@Tag("integration")
class RepositoryGestionReplaceObjectsIntegrationTest extends WithGraphDBContainer {

    private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();
    private static final IRI GRAPH = VF.createIRI("http://rdf.insee.fr/graphes/replace-objects-it");
    private static final IRI OTHER_GRAPH = VF.createIRI("http://rdf.insee.fr/graphes/replace-objects-it-autre");

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    @Test
    void runs_the_updates_then_replaces_each_object_in_its_graph() throws RmesException {
        IRI first = VF.createIRI("http://bauhaus/replace-objects/first");
        IRI second = VF.createIRI("http://bauhaus/replace-objects/second");
        IRI purged = VF.createIRI("http://bauhaus/replace-objects/purged");
        repositoryGestion.loadSimpleObject(first, labelled(first, "Ancien", GRAPH), null);
        repositoryGestion.loadSimpleObject(purged, labelled(purged, "À purger", GRAPH), null);

        repositoryGestion.replaceObjects(
                List.of("DELETE WHERE { GRAPH <" + GRAPH + "> { <" + purged + "> ?p ?o } }"),
                List.of(
                        new SubjectModelGraph(first, labelled(first, "Nouveau", GRAPH), GRAPH),
                        new SubjectModelGraph(second, labelled(second, "Autre graphe", OTHER_GRAPH), OTHER_GRAPH)));

        assertThat(labelsOf(GRAPH, first)).containsExactly("Nouveau");
        assertThat(labelsOf(OTHER_GRAPH, second)).containsExactly("Autre graphe");
        assertThat(labelsOf(GRAPH, purged)).isEmpty();
    }

    @Test
    void leaves_the_repository_untouched_when_a_step_fails() throws RmesException {
        IRI kept = VF.createIRI("http://bauhaus/replace-objects/kept");
        repositoryGestion.loadSimpleObject(kept, labelled(kept, "Intact", GRAPH), null);

        assertThatThrownBy(() -> repositoryGestion.replaceObjects(
                        List.of("DELETE WHERE { GRAPH <" + GRAPH + "> { <" + kept + "> ?p ?o } }", "PAS DU SPARQL"),
                        List.of(new SubjectModelGraph(kept, labelled(kept, "Remplacé", GRAPH), GRAPH))))
                .isInstanceOf(RmesException.class);

        assertThat(labelsOf(GRAPH, kept)).containsExactly("Intact");
    }

    private static Model labelled(IRI subject, String label, IRI graph) {
        Model model = new LinkedHashModel();
        model.add(subject, RDFS.LABEL, VF.createLiteral(label), graph);
        return model;
    }

    private List<String> labelsOf(IRI graph, IRI subject) throws RmesException {
        return JSONUtils.stream(repositoryGestion.getResponseAsArray("SELECT ?label WHERE { GRAPH <" + graph + "> { <"
                        + subject + "> <" + RDFS.LABEL + "> ?label } }"))
                .map(row -> row.getString("label"))
                .toList();
    }
}
