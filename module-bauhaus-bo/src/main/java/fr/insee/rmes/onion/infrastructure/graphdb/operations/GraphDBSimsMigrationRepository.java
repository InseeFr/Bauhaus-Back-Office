package fr.insee.rmes.onion.infrastructure.graphdb.operations;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.modules.commons.domain.GenericInternalServerException;
import fr.insee.rmes.modules.operations.msd.domain.model.SimsConvertedTextNode;
import fr.insee.rmes.modules.operations.msd.domain.model.SimsTextNode;
import fr.insee.rmes.modules.operations.msd.domain.port.serverside.SimsMigrationRepository;
import fr.insee.rmes.onion.infrastructure.graphdb.operations.queries.SimsMigrationQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.rdf_utils.SubjectModelGraph;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Values;
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
    public List<SimsTextNode> findHtmlTextNodes(int limit, int offset) throws GenericInternalServerException {
        return fetchHtmlTextNodes(repositoryGestion::getResponseAsArray, limit, offset);
    }

    @Override
    public List<SimsTextNode> findPublicationHtmlTextNodes(int limit, int offset) throws GenericInternalServerException {
        return fetchHtmlTextNodes(repositoryPublication::getResponseAsArray, limit, offset);
    }

    private List<SimsTextNode> fetchHtmlTextNodes(QueryExecutor executor, int limit, int offset) throws GenericInternalServerException {
        JSONArray results;
        try {
            results = executor.execute(SimsMigrationQueries.getSimsHtmlTextNodes(limit, offset));
        } catch (RmesException e) {
            throw new GenericInternalServerException(e.getDetails());
        }
        List<SimsTextNode> nodes = new ArrayList<>();
        for (int i = 0; i < results.length(); i++) {
            JSONObject row = results.getJSONObject(i);
            nodes.add(new SimsTextNode(
                    row.getString("graph"),
                    row.getString("uri"),
                    row.getString("predicate"),
                    row.getString("value"),
                    row.optBoolean("needHTML", false),
                    row.optString("lang", "")
            ));
        }
        return nodes;
    }

    @Override
    public void bulkUpdateGestionTextNodes(List<SimsConvertedTextNode> nodes) throws GenericInternalServerException {
        List<SubjectModelGraph> updates = nodes.stream().map(node -> {
            var graphIri = Values.iri(node.graph());
            var uriIri = Values.iri(node.uri());
            Model model = new LinkedHashModel();
            var literal = node.lang() != null && !node.lang().isEmpty()
                    ? Values.literal(node.markdown(), node.lang())
                    : Values.literal(node.markdown());
            model.add(uriIri, Values.iri(node.predicate()), literal, graphIri);
            return new SubjectModelGraph(uriIri, model, graphIri);
        }).toList();
        try {
            repositoryGestion.bulkOverrideTriplets(updates);
        } catch (RmesException e) {
            throw new GenericInternalServerException(e.getDetails());
        }
    }

    @Override
    public void bulkUpdatePublicationTextNodes(List<SimsConvertedTextNode> nodes) throws GenericInternalServerException {
        List<SubjectModelGraph> updates = nodes.stream().map(node -> {
            var graphIri = Values.iri(node.graph());
            var uriIri = Values.iri(node.uri());
            Model model = new LinkedHashModel();
            var literal = node.lang() != null && !node.lang().isEmpty()
                    ? Values.literal(node.markdown(), node.lang())
                    : Values.literal(node.markdown());
            model.add(uriIri, Values.iri(node.predicate()), literal, graphIri);
            if (node.needHTML()) {
                model.add(uriIri, INSEE.HTML, Values.literal(node.html()), graphIri);
            }
            return new SubjectModelGraph(uriIri, model, graphIri);
        }).toList();
        try {
            repositoryPublication.bulkOverrideTriplets(updates);
        } catch (RmesException e) {
            throw new GenericInternalServerException(e.getDetails());
        }
    }
}