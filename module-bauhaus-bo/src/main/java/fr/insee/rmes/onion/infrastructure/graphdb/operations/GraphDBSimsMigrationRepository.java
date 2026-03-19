package fr.insee.rmes.onion.infrastructure.graphdb.operations;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.modules.commons.domain.GenericInternalServerException;
import fr.insee.rmes.modules.operations.msd.domain.model.SimsTextNode;
import fr.insee.rmes.modules.operations.msd.domain.port.serverside.SimsMigrationRepository;
import fr.insee.rmes.onion.infrastructure.graphdb.operations.queries.SimsMigrationQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class GraphDBSimsMigrationRepository implements SimsMigrationRepository {

    private final RepositoryGestion repositoryGestion;
    private final RepositoryPublication repositoryPublication;

    public GraphDBSimsMigrationRepository(RepositoryGestion repositoryGestion, RepositoryPublication repositoryPublication) {
        this.repositoryGestion = repositoryGestion;
        this.repositoryPublication = repositoryPublication;
    }

    @FunctionalInterface
    private interface QueryExecutor {
        JSONArray execute(String query) throws RmesException;
    }

    @Override
    public List<SimsTextNode> findAllHtmlTextNodes() throws GenericInternalServerException {
        return fetchHtmlTextNodes(repositoryGestion::getResponseAsArray);
    }

    @Override
    public void updateTextNodeValue(String graph, String uri, String markdownValue) throws GenericInternalServerException {
        try {
            var graphIri = Values.iri(graph);
            var uriIri = Values.iri(uri);
            Model model = new LinkedHashModel();
            model.add(uriIri, RDF.VALUE, Values.literal(markdownValue), graphIri);
            repositoryGestion.overrideTriplets(uriIri, model, graphIri);
        } catch (RmesException e) {
            throw new GenericInternalServerException(e.getDetails());
        }
    }

    @Override
    public List<SimsTextNode> findAllPublicationHtmlTextNodes() throws GenericInternalServerException {
        return fetchHtmlTextNodes(repositoryPublication::getResponseAsArray);
    }

    private List<SimsTextNode> fetchHtmlTextNodes(QueryExecutor executor) throws GenericInternalServerException {
        JSONArray results;
        try {
            results = executor.execute(SimsMigrationQueries.getSimsHtmlTextNodes());
        } catch (RmesException e) {
            throw new GenericInternalServerException(e.getDetails());
        }
        List<SimsTextNode> nodes = new ArrayList<>();
        for (int i = 0; i < results.length(); i++) {
            JSONObject row = results.getJSONObject(i);
            nodes.add(new SimsTextNode(
                    row.getString("graph"),
                    row.getString("uri"),
                    row.getString("value")
            ));
        }
        return nodes;
    }

    @Override
    public void updatePublicationTextNodeWithMarkdownAndHtml(String graph, String uri, String markdown, String html) throws GenericInternalServerException {
        try {
            var graphIri = Values.iri(graph);
            var uriIri = Values.iri(uri);
            Model model = new LinkedHashModel();
            model.add(uriIri, RDF.VALUE, Values.literal(markdown), graphIri);
            model.add(uriIri, INSEE.HTML, Values.literal(html), graphIri);
            repositoryPublication.overrideTriplets(uriIri, model, graphIri);
        } catch (RmesException e) {
            throw new GenericInternalServerException(e.getDetails());
        }
    }
}