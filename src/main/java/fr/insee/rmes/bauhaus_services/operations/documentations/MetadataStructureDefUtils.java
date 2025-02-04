package fr.insee.rmes.bauhaus_services.operations.documentations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.documentations.RangeType;
import fr.insee.rmes.persistance.sparql_queries.operations.documentations.DocumentationsQueries;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MetadataStructureDefUtils  extends RdfService {
	
		private static final String RANGE = "range";
		static final Logger logger = LoggerFactory.getLogger(MetadataStructureDefUtils.class);


	public JSONObject getMetadataAttributeById(String id) throws RmesException{
		JSONObject mas = repoGestion.getResponseAsObject(DocumentationsQueries.getAttributeSpecificationQuery(id));
		if (mas.isEmpty()) {throw new RmesException(HttpStatus.SC_BAD_REQUEST, "Attribute not found", "id doesn't exist"+id);}
		transformRangeType(mas);
		mas.put(Constants.ID, id);
		return mas;
	}
	

	public void transformRangeType(JSONObject mas) throws RmesException {
		if (!mas.has(RANGE)) {throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "At least one attribute don't have range", (mas.has("id") ? mas.get(Constants.ID) : mas).toString());}
		String rangeUri = mas.getString(RANGE);
		RangeType type = RangeType.getEnumByRdfType(RdfUtils.toURI(rangeUri));
		mas.put(Constants.RANGE_TYPE, type.getJsonType());
		mas.remove(RANGE);

		if (!type.equals(RangeType.CODELIST)) {
			mas.remove(Constants.CODELIST);
		}

	}

	public JSONArray getMetadataAttributes() throws RmesException {
		JSONArray attributesList = repoGestion.getResponseAsArray(DocumentationsQueries.getAttributesQuery());
		if (!attributesList.isEmpty()) {
			 for (int i = 0; i < attributesList.length(); i++) {
		         JSONObject attribute = attributesList.getJSONObject(i);
		         transformRangeType(attribute);
		     }
		}
		return attributesList;
	}
	
	public Map<String,String> getMetadataAttributesUri() throws RmesException {
		Map<String,String> attributes = new HashMap<>();
		JSONArray attributesList = repoGestion.getResponseAsArray(DocumentationsQueries.getAttributesUriQuery());
		if (!attributesList.isEmpty()) {
			 for (int i = 0; i < attributesList.length(); i++) {
		         JSONObject attribute = attributesList.getJSONObject(i);
		         if (attribute.has(Constants.ID)&& attribute.has(Constants.URI)) {
		        	 String id = StringUtils.upperCase(attribute.getString(Constants.ID));
		        	 attributes.put(id, attribute.getString(Constants.URI));
		         }
		     }
		}
		return attributes;
	}

}
