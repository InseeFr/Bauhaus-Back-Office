package fr.insee.rmes.onion.infrastructure.graphdb.operations.queries;

import fr.insee.rmes.Constants;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.model.operations.documentations.RangeType;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.GenericQueries;
import org.eclipse.rdf4j.model.Resource;

import java.util.HashMap;
import java.util.Map;

public class DocumentationQueries  extends GenericQueries {
    private static final String ID_SIMS = Constants.ID_SIMS;


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

    public static String deleteGraph(Resource graph) throws RmesException {
        Map<String,Object> params = initParams();
        params.put("DOCUMENTATION_GRAPH", graph);
        return buildRequest("deleteGraph.ftlh", params);
    }

    private static String buildRequest(String fileName, Map<String, Object> params) throws RmesException  {
        return FreeMarkerUtils.buildRequest("operations/documentations/", fileName, params);
    }



    public static String msdQuery() throws RmesException{
        Map<String,Object> params = initParams();
        return buildRequest("msdQuery.ftlh", params);
    }

    public static String getAttributesUriQuery() throws RmesException {
        Map<String,Object> params = initParams();
        return buildRequest("getAttributesUriQuery.ftlh", params);
    }

    public static String getDocumentationTitleQuery(String idSims) throws RmesException {
        Map<String,Object> params = initParams();
        params.put(ID_SIMS, idSims);
        return buildRequest("getDocumentationTitleQuery.ftlh", params);
    }

    public static String getTargetByIdSims(String idSims) throws RmesException {
        Map<String,Object> params = initParams();
        params.put(ID_SIMS, idSims);
        return buildRequest("getTargetByIdSimsQuery.ftlh", params);
    }

    public static String getSimsByTarget(String idTarget) throws RmesException {
        Map<String,Object> params = initParams();
        params.put("idTarget", idTarget);
        return buildRequest("getSimsByIdTargetQuery.ftlh", params);
    }

    public static String getDocumentationRubricsQuery(String idSims, String clLg1, String clLg2) throws RmesException {
        Map<String,Object> params = initParams();
        params.put(ID_SIMS, idSims);
        params.put("DATE", RangeType.DATE);
        params.put("STRING", RangeType.STRING);
        params.put("RICHTEXT", RangeType.RICHTEXT);
        params.put("ATTRIBUTE", RangeType.ATTRIBUTE);
        params.put("CODELIST", RangeType.CODELIST);
        params.put("ORGANIZATION", RangeType.ORGANIZATION);
        params.put("GEOGRAPHY", RangeType.GEOGRAPHY);

        params.put("ORGANIZATIONS_GRAPH", config.getOrganizationsGraph());
        params.put("ORG_INSEE_GRAPH", config.getOrgInseeGraph());
        params.put("COG_GRAPH", config.getGeographyGraph());
        params.put("DOCUMENTATIONS_GEO_GRAPH", config.getDocumentationsGeoGraph());
        params.put("LG1_CL",clLg1);
        params.put("LG2_CL",clLg2);
        return buildRequest("getDocumentationRubricsQuery.ftlh", params);
    }

    public static String lastID() throws RmesException {
        return buildRequest("lastID.ftlh", null);
    }


    public static String getPublicationState(String id) throws RmesException{
        Map<String,Object> params = initParams();
        params.put(Constants.ID_SIMS, id);
        params.put("DOCUMENTATIONS_GRAPH", config.getDocumentationsGraph());
        return buildRequest("getPublicationStatusQuery.ftlh", params);
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
