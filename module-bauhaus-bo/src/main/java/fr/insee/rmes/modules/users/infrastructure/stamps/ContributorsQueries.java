package fr.insee.rmes.modules.users.infrastructure.stamps;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.graphdb.SparqlLiterals;
import java.util.Map;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Component;

/**
 * Les contributeurs (dc:contributor) ou créateurs (dc:creator) d'une ressource, lus dans le graphe
 * qui la porte.
 */
@Component
public class ContributorsQueries {

    public String getContributors(String graph, IRI iri) throws RmesException {
        return getStamps(graph, iri, "dc:contributor");
    }

    /** Les timbres sont rendus sous la même variable ?contributor que les contributeurs. */
    public String getCreators(String graph, IRI iri) throws RmesException {
        return getStamps(graph, iri, "dc:creator");
    }

    private String getStamps(String graph, IRI iri, String predicate) throws RmesException {
        Map<String, Object> params = Map.of(
                "GRAPH",
                SparqlLiterals.iri(graph),
                "IRI",
                SparqlLiterals.iri(iri.stringValue()),
                "PREDICATE",
                predicate);
        return FreeMarkerUtils.buildRequest("common/", "getContributors.ftlh", params);
    }
}
