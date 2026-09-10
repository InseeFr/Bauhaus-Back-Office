package fr.insee.rmes.modules.operations.msd.infrastructure.graphdb;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.graphdb.SparqlLiterals;
import fr.insee.rmes.model.operations.documentations.RangeType;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.rdf4j.model.Resource;
import org.springframework.stereotype.Component;

@Component
public class DocumentationQueries {

    private static final String ID_SIMS = Constants.ID_SIMS;
    private static final String DOCUMENTATION_GRAPH_IRI = "DOCUMENTATION_GRAPH_IRI";

    private final BauhausLanguagesProperties languages;
    private final GraphsProperties graphs;

    public DocumentationQueries(BauhausLanguagesProperties languages, GraphsProperties graphs) {
        this.languages = languages;
        this.graphs = graphs;
    }

    private Map<String, Object> initParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("LG1", SparqlLiterals.literal(languages.lg1()));
        params.put("LG2", SparqlLiterals.literal(languages.lg2()));

        params.put("MSD_GRAPH", SparqlLiterals.iri(graphs.msdGraph()));
        params.put("CODELIST_GRAPH", SparqlLiterals.iri(graphs.codeListGraph()));
        params.put("MSD_CONCEPTS_GRAPH", SparqlLiterals.iri(graphs.msdConceptsGraph()));
        return params;
    }

    public String deleteGraph(Resource graph) throws RmesException {
        Map<String, Object> params = initParams();
        params.put("DOCUMENTATION_GRAPH", SparqlLiterals.iri(graph.stringValue()));
        return buildRequest("deleteGraph.ftlh", params);
    }

    private String buildRequest(String fileName, Map<String, Object> params) throws RmesException {
        return FreeMarkerUtils.buildRequest("operations/documentations/", fileName, params);
    }

    public String msdQuery() throws RmesException {
        Map<String, Object> params = initParams();
        return buildRequest("msdQuery.ftlh", params);
    }

    public String getAttributesUriQuery() throws RmesException {
        Map<String, Object> params = initParams();
        return buildRequest("getAttributesUriQuery.ftlh", params);
    }

    public String getDocumentationTitleQuery(String idSims) throws RmesException {
        Map<String, Object> params = initParams();
        params.put(ID_SIMS, SparqlLiterals.literal(idSims));
        params.put(DOCUMENTATION_GRAPH_IRI, SparqlLiterals.iri(graphs.documentationsGraph() + "/" + idSims));
        return buildRequest("getDocumentationTitleQuery.ftlh", params);
    }

    public String getTargetByIdSims(String idSims) throws RmesException {
        Map<String, Object> params = initParams();
        params.put(ID_SIMS, SparqlLiterals.literal(idSims));
        params.put(DOCUMENTATION_GRAPH_IRI, SparqlLiterals.iri(graphs.documentationsGraph() + "/" + idSims));
        return buildRequest("getTargetByIdSimsQuery.ftlh", params);
    }

    public String getSimsByTarget(String idTarget) throws RmesException {
        Map<String, Object> params = initParams();
        params.put("idTarget", SparqlLiterals.literal(idTarget));
        return buildRequest("getSimsByIdTargetQuery.ftlh", params);
    }

    public String getDocumentationRubricsQuery(String idSims, String clLg1, String clLg2) throws RmesException {
        Map<String, Object> params = initParams();
        params.put(ID_SIMS, SparqlLiterals.literal(idSims));
        params.put(DOCUMENTATION_GRAPH_IRI, SparqlLiterals.iri(graphs.documentationsGraph() + "/" + idSims));
        params.put("DATE_JSON_TYPE", SparqlLiterals.literal(RangeType.DATE.getJsonType()));
        params.put(
                "DATE_RDF_TYPE", SparqlLiterals.iri(RangeType.DATE.getRdfType().stringValue()));
        params.put("STRING_JSON_TYPE", SparqlLiterals.literal(RangeType.STRING.getJsonType()));
        params.put("RICHTEXT_JSON_TYPE", SparqlLiterals.literal(RangeType.RICHTEXT.getJsonType()));
        params.put("CODELIST_JSON_TYPE", SparqlLiterals.literal(RangeType.CODELIST.getJsonType()));
        params.put("ORGANIZATION_JSON_TYPE", SparqlLiterals.literal(RangeType.ORGANIZATION.getJsonType()));
        params.put("GEOGRAPHY_JSON_TYPE", SparqlLiterals.literal(RangeType.GEOGRAPHY.getJsonType()));

        params.put("ORGANIZATIONS_GRAPH", SparqlLiterals.iri(graphs.organizationsGraph()));
        params.put("ORG_INSEE_GRAPH", SparqlLiterals.iri(graphs.orgInseeGraph()));
        params.put("COG_GRAPH", SparqlLiterals.iri(graphs.geographyGraph()));
        params.put("DOCUMENTATIONS_GEO_GRAPH", SparqlLiterals.iri(graphs.documentationsGeoGraph()));
        params.put("LG1_CL", SparqlLiterals.iri(clLg1));
        params.put("LG2_CL", SparqlLiterals.iri(clLg2));
        return buildRequest("getDocumentationRubricsQuery.ftlh", params);
    }

    public String lastID() throws RmesException {
        return buildRequest("lastID.ftlh", null);
    }

    public String getPublicationState(String id) throws RmesException {
        Map<String, Object> params = initParams();
        params.put(Constants.ID_SIMS, SparqlLiterals.literal(id));
        params.put(DOCUMENTATION_GRAPH_IRI, SparqlLiterals.iri(graphs.documentationsGraph() + "/" + id));

        return buildRequest("getPublicationStatusQuery.ftlh", params);
    }

    public String getAttributeSpecificationQuery(String idMas) throws RmesException {
        Map<String, Object> params = initParams();
        params.put("idMas", SparqlLiterals.literal(idMas));
        params.put("uniqueAttr", true);
        params.put("MSD_GRAPH", SparqlLiterals.iri(graphs.msdGraph()));
        params.put("CODELIST_GRAPH", SparqlLiterals.iri(graphs.codeListGraph()));
        params.put("MSD_CONCEPTS_GRAPH", SparqlLiterals.iri(graphs.msdConceptsGraph()));
        return buildRequest("getAttributeSpecificationQuery.ftlh", params);
    }

    /**
     * @return ?id ?masLabelLg1 ?masLabelLg2 ?range ?isPresentational
     * @throws RmesException
     */
    public String getAttributesQuery() throws RmesException {
        Map<String, Object> params = initParams();
        params.put("uniqueAttr", false);
        return buildRequest("getAttributeSpecificationQuery.ftlh", params);
    }
}
