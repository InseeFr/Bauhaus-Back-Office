package fr.insee.rmes.onion.infrastructure.graphdb.operations.queries;

import fr.insee.rmes.bauhaus_services.rdf_utils.FreeMarkerUtils;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;

import java.util.HashMap;
import java.util.Map;

public class DocumentationQueries  extends GenericQueries {
    private static Map<String,Object> initParams() {
        Map<String,Object> params = new HashMap<>();
        params.put("LG1", config.getLg1());
        params.put("LG2", config.getLg2());
        params.put("DOCUMENTATIONS_GRAPH", config.getDocumentationsGraph());
        params.put("MSD_GRAPH",config.getMsdGraph());
        params.put("CODELIST_GRAPH",config.getCodeListGraph());
        params.put("MSD_CONCEPTS_GRAPH", config.getMsdConceptsGraph());
        return params;
    }


    private static String buildRequest(String fileName, Map<String, Object> params) throws RmesException  {
        return FreeMarkerUtils.buildRequest("operations/documentations/", fileName, params);
    }

    public static String getAttributeSpecificationQuery(String idMas) throws RmesException {
        Map<String,Object> params = initParams();
        params.put("idMas", idMas);
        params.put("uniqueAttr","true");
        params.put("MSD_GRAPH",config.getMsdGraph());
        params.put("CODELIST_GRAPH",config.getCodeListGraph());
        params.put("MSD_CONCEPTS_GRAPH", config.getMsdConceptsGraph());
        return buildRequest("getAttributeSpecificationQuery.ftlh", params);
    }

    /**
     * @return ?id ?masLabelLg1 ?masLabelLg2 ?range ?isPresentational
     * @throws RmesException
     */
    public static String getAttributesQuery() throws RmesException {
        Map<String,Object> params = initParams();
        params.put("uniqueAttr","false");
        return buildRequest("getAttributeSpecificationQuery.ftlh", params);
    }
}
