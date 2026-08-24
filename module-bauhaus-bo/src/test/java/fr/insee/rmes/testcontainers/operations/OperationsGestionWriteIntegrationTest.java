package fr.insee.rmes.testcontainers.operations;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.json.JSONArray;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Filet de sécurité sur les écritures « gestion » d'objets de type opérations/séries via
 * {@link RepositoryGestion} (chemin critique « CRUD écriture » du lot A de la migration
 * GraphDB → Fuseki).
 *
 * <p>Avant ce test, l'écriture côté gestion n'était couverte que par des tests E2E HTTP
 * (REST) ou des mocks. Or ce sont précisément les sémantiques de
 * {@code loadSimpleObject} (remove + add), {@code replaceGraph} (clear + add) et
 * {@code deleteObject} qui doivent se comporter à l'identique sur le futur backend SPARQL.
 * On les exerce ici contre un vrai GraphDB en lecture/écriture round-trip.
 *
 * <p>Le conteneur GraphDB étant partagé entre toutes les classes de test d'intégration,
 * chaque méthode écrit dans un graphe nommé dédié (préfixe {@code operations-write-it}) avec
 * des IRIs uniques, pour rester totalement isolée des autres suites (qui comptent par ex. le
 * nombre d'opérations du graphe réel).
 */
@Tag("integration")
class OperationsGestionWriteIntegrationTest extends WithGraphDBContainer {

    private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();
    private static final String LABEL = "http://www.w3.org/2000/01/rdf-schema#label";

    private static final String GRAPH_LOAD = "http://rdf.insee.fr/graphes/operations-write-it/load";
    private static final String GRAPH_REPLACE_OBJECT = "http://rdf.insee.fr/graphes/operations-write-it/replace-object";
    private static final String GRAPH_REPLACE_GRAPH = "http://rdf.insee.fr/graphes/operations-write-it/replace-graph";
    private static final String GRAPH_DELETE = "http://rdf.insee.fr/graphes/operations-write-it/delete";

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(),
            new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    @Test
    void loadSimpleObject_persists_a_series_readable_by_sparql() throws RmesException {
        Resource graph = VF.createIRI(GRAPH_LOAD);
        IRI series = VF.createIRI("http://bauhaus/operations/serie/s-load-it");

        repositoryGestion.loadSimpleObject(series, seriesModel(series, "Série persistée", graph));

        assertThat(labelsOf(GRAPH_LOAD, series)).containsExactly("Série persistée");
    }

    @Test
    void loadSimpleObject_replaces_existing_triples_for_the_same_subject() throws RmesException {
        Resource graph = VF.createIRI(GRAPH_REPLACE_OBJECT);
        IRI series = VF.createIRI("http://bauhaus/operations/serie/s-replace-object-it");

        repositoryGestion.loadSimpleObject(series, seriesModel(series, "Libellé initial", graph));
        repositoryGestion.loadSimpleObject(series, seriesModel(series, "Libellé corrigé", graph));

        assertThat(labelsOf(GRAPH_REPLACE_OBJECT, series))
                .as("loadSimpleObject doit remplacer les triplets de l'objet, pas les cumuler")
                .containsExactly("Libellé corrigé");
    }

    @Test
    void replaceGraph_clears_the_graph_before_loading_the_new_model() throws RmesException {
        Resource graph = VF.createIRI(GRAPH_REPLACE_GRAPH);
        IRI seriesA = VF.createIRI("http://bauhaus/operations/serie/s-replace-graph-a-it");
        IRI seriesB = VF.createIRI("http://bauhaus/operations/serie/s-replace-graph-b-it");
        IRI seriesC = VF.createIRI("http://bauhaus/operations/serie/s-replace-graph-c-it");

        Model initial = seriesModel(seriesA, "Série A", graph);
        initial.addAll(seriesModel(seriesB, "Série B", graph));
        repositoryGestion.replaceGraph(graph, initial, null);

        repositoryGestion.replaceGraph(graph, seriesModel(seriesC, "Série C", graph), null);

        assertThat(labelsOf(GRAPH_REPLACE_GRAPH, seriesA)).as("série A effacée par replaceGraph").isEmpty();
        assertThat(labelsOf(GRAPH_REPLACE_GRAPH, seriesB)).as("série B effacée par replaceGraph").isEmpty();
        assertThat(labelsOf(GRAPH_REPLACE_GRAPH, seriesC)).as("série C présente après replaceGraph").containsExactly("Série C");
    }

    @Test
    void deleteObject_removes_all_triples_of_a_subject() throws RmesException {
        Resource graph = VF.createIRI(GRAPH_DELETE);
        IRI series = VF.createIRI("http://bauhaus/operations/serie/s-delete-it");
        repositoryGestion.loadSimpleObject(series, seriesModel(series, "À supprimer", graph));
        assertThat(labelsOf(GRAPH_DELETE, series)).as("série présente avant suppression").containsExactly("À supprimer");

        repositoryGestion.deleteObject(series);

        assertThat(labelsOf(GRAPH_DELETE, series)).as("série supprimée").isEmpty();
    }

    private static Model seriesModel(IRI series, String label, Resource graph) {
        Model model = new LinkedHashModel();
        model.add(series, RDF.TYPE, SKOS.CONCEPT_SCHEME, graph);
        model.add(series, DCTERMS.IDENTIFIER, VF.createLiteral(series.getLocalName()), graph);
        model.add(series, RDFS.LABEL, VF.createLiteral(label), graph);
        return model;
    }

    private List<String> labelsOf(String graph, IRI series) throws RmesException {
        String query = "SELECT ?label WHERE { GRAPH <%s> { <%s> <%s> ?label } }"
                .formatted(graph, series.stringValue(), LABEL);
        JSONArray array = repositoryGestion.getResponseAsArray(query);
        List<String> labels = new ArrayList<>();
        if (array != null) {
            JSONUtils.stream(array)
                    .map(row -> row.getString("label"))
                    .forEach(labels::add);
        }
        return labels;
    }
}
