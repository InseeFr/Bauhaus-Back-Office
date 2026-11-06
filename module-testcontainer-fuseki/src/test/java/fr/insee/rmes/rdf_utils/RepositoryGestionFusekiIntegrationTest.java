package fr.insee.rmes.rdf_utils;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RdfBasicCredentials;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.testcontainers.FusekiContainer;
import fr.insee.rmes.testcontainers.WithFusekiContainer;
import java.util.List;
import java.util.stream.IntStream;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.json.JSONArray;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Passage d'un objet à l'état « Validated » par {@link RepositoryGestion} sur Fuseki : l'ancien statut
 * doit disparaître, alors qu'un {@code remove} sans graphe nommé n'y supprime rien.
 * <p>
 * Chaque test travaille sur ses propres IRI, le dataset n'étant vidé qu'entre deux classes.
 */
@Tag("integration")
class RepositoryGestionFusekiIntegrationTest extends WithFusekiContainer {

    private static final IRI GRAPH = Values.iri("http://rdf.insee.fr/graphes/concepts/definitions");
    private static final IRI OPERATIONS_GRAPH = Values.iri("http://rdf.insee.fr/graphes/operations");

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(),
            new RepositoryUtils(
                    null,
                    RepositoryInitiator.Type.DISABLED,
                    "fuseki",
                    new RdfBasicCredentials(FusekiContainer.ADMIN_USER, FusekiContainer.ADMIN_PASSWORD)));

    @Test
    void objectValidation_replaces_the_previous_status() throws RmesException {
        IRI object = Values.iri("http://bauhaus/concepts/definition/c9001");
        storeStatus(object, "Unpublished");

        repositoryGestion.objectValidation(object, validated(object));

        assertThat(statusesOf(object)).containsExactly("Validated");
    }

    @Test
    void objectsValidation_replaces_the_previous_status_of_each_object() throws RmesException {
        IRI first = Values.iri("http://bauhaus/concepts/definition/c9002");
        IRI second = Values.iri("http://bauhaus/concepts/definition/c9003");
        storeStatus(first, "Unpublished");
        storeStatus(second, "Modified");
        Model model = validated(first);
        model.addAll(validated(second));

        repositoryGestion.objectsValidation(List.of(first, second), model);

        assertThat(statusesOf(first)).containsExactly("Validated");
        assertThat(statusesOf(second)).containsExactly("Validated");
    }

    @Test
    void loadSimpleObject_replaces_the_previous_triples_of_the_object() throws RmesException {
        IRI family = Values.iri("http://bauhaus/operations/famille/s9001");

        repositoryGestion.loadSimpleObject(family, labelled(family, "Première version"), null);
        repositoryGestion.loadSimpleObject(family, labelled(family, "Deuxième version"), null);

        assertThat(prefLabelsOf(family)).containsExactly("Deuxième version");
    }

    @Test
    void deleteObject_removes_every_triple_of_the_object() throws RmesException {
        IRI family = Values.iri("http://bauhaus/operations/famille/s9002");
        repositoryGestion.loadSimpleObject(family, labelled(family, "À supprimer"), null);

        repositoryGestion.deleteObject(family);

        assertThat(repositoryGestion
                        .getResponseAsArray("SELECT ?p WHERE { GRAPH ?g { <%s> ?p ?o } }".formatted(family))
                        .length())
                .isZero();
    }

    private static Model labelled(IRI object, String label) {
        Model model = new LinkedHashModel();
        model.add(object, SKOS.PREF_LABEL, Values.literal(label, "fr"), OPERATIONS_GRAPH);
        return model;
    }

    private List<String> prefLabelsOf(IRI object) throws RmesException {
        JSONArray rows = repositoryGestion.getResponseAsArray("SELECT ?label WHERE { GRAPH <%s> { <%s> <%s> ?label } }"
                .formatted(OPERATIONS_GRAPH, object, SKOS.PREF_LABEL));
        return IntStream.range(0, rows.length())
                .mapToObj(i -> rows.getJSONObject(i).getString("label"))
                .toList();
    }

    private void storeStatus(IRI object, String status) throws RmesException {
        repositoryGestion.executeUpdate("INSERT DATA { GRAPH <%s> { <%s> <%s> \"%s\" } }"
                .formatted(GRAPH, object, INSEE.VALIDATION_STATE, status));
    }

    private static Model validated(IRI object) {
        Model model = new LinkedHashModel();
        model.add(object, INSEE.VALIDATION_STATE, Values.literal("Validated"), GRAPH);
        return model;
    }

    private List<String> statusesOf(IRI object) throws RmesException {
        JSONArray rows =
                repositoryGestion.getResponseAsArray("SELECT ?status WHERE { GRAPH <%s> { <%s> <%s> ?status } }"
                        .formatted(GRAPH, object, INSEE.VALIDATION_STATE));
        return IntStream.range(0, rows.length())
                .mapToObj(i -> rows.getJSONObject(i).getString("status"))
                .toList();
    }
}
