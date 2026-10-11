package fr.insee.rmes.modules.shared_kernel.infrastructure.publication;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
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
 * Le statut est réécrit contre un vrai GraphDB : c'est le graphe visé par les suppressions qui est
 * en jeu, et un mock de {@link RepositoryGestion} ne le montrerait pas.
 * <p>
 * Chaque test travaille sur ses propres IRI et graphes, le conteneur étant partagé entre les suites.
 */
@Tag("integration")
class ValidationStatusListenerIntegrationTest extends WithGraphDBContainer {

    private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    private final ValidationStatusListener listener = new ValidationStatusListener(repositoryGestion);

    @Test
    void should_validate_an_unpublished_object_in_its_management_graph() throws RmesException {
        IRI graph = VF.createIRI("http://rdf.insee.fr/graphes/validation-status-it/unpublished");
        IRI object = VF.createIRI("http://bauhaus/validation-status-it/unpublished");
        store(object, graph, ValidationStatus.UNPUBLISHED, ValidationStatus.MODIFIED);

        listener.on(new ObjectPublished(object, graph));

        assertThat(statusesOf(object, graph)).containsExactly(ValidationStatus.VALIDATED.getValue());
        assertThat(labelsOf(object, graph)).as("seul le statut est réécrit").containsExactly("objet");
    }

    /** Contrairement à objectValidation, qui purge le statut dans tous les graphes. */
    @Test
    void should_leave_the_status_held_in_another_graph_untouched() throws RmesException {
        IRI graph = VF.createIRI("http://rdf.insee.fr/graphes/validation-status-it/management");
        IRI otherGraph = VF.createIRI("http://rdf.insee.fr/graphes/validation-status-it/other");
        IRI object = VF.createIRI("http://bauhaus/validation-status-it/two-graphs");
        store(object, graph, ValidationStatus.UNPUBLISHED);
        store(object, otherGraph, ValidationStatus.MODIFIED);

        listener.on(new ObjectPublished(object, graph));

        assertThat(statusesOf(object, graph)).containsExactly(ValidationStatus.VALIDATED.getValue());
        assertThat(statusesOf(object, otherGraph)).containsExactly(ValidationStatus.MODIFIED.getValue());
    }

    private void store(IRI object, IRI graph, ValidationStatus... statuses) throws RmesException {
        Model model = new LinkedHashModel();
        model.add(object, RDFS.LABEL, VF.createLiteral("objet"), graph);
        for (ValidationStatus status : statuses) {
            model.add(object, INSEE.VALIDATION_STATE, VF.createLiteral(status.getValue()), graph);
        }
        repositoryGestion.loadSimpleObjectWithoutDeletion(object, model, null);
    }

    private List<String> statusesOf(IRI object, IRI graph) throws RmesException {
        return valuesOf(object, INSEE.VALIDATION_STATE, graph);
    }

    private List<String> labelsOf(IRI object, IRI graph) throws RmesException {
        return valuesOf(object, RDFS.LABEL, graph);
    }

    private List<String> valuesOf(IRI object, IRI predicate, IRI graph) throws RmesException {
        String query = "SELECT ?value WHERE { GRAPH <%s> { <%s> <%s> ?value } }"
                .formatted(graph.stringValue(), object.stringValue(), predicate.stringValue());
        return JSONUtils.stream(repositoryGestion.getResponseAsArray(query))
                .map(row -> row.getString("value"))
                .toList();
    }
}
