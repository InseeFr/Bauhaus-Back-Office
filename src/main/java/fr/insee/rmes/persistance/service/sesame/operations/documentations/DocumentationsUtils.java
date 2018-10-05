package fr.insee.rmes.persistance.service.sesame.operations.documentations;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.service.sesame.code_list.CodeListQueries;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;

@Component
public class DocumentationsUtils {

	public JSONObject getMetadataAttributeById(String id) throws RmesException{
		JSONObject mas = RepositoryGestion.getResponseAsObject(DocumentationsQueries.getAttributeSpecificationQuery(id));
		transformRangeType(mas);
		mas.put("id", id);
		return mas;
	}
	
	public JSONObject getDocumentationByIdSims(String idSims) throws RmesException{
		JSONObject doc = RepositoryGestion.getResponseAsObject(DocumentationsQueries.getDocumentationTitleQuery(idSims));
		doc.put("id", idSims);
		JSONArray docRubrics = RepositoryGestion.getResponseAsArray(DocumentationsQueries.getDocumentationRubricsQuery(idSims));
		doc.put("rubrics", docRubrics);
		return doc;
	}
	

	public void transformRangeType(JSONObject mas) throws RmesException {
		String rangeUri = mas.getString("range");
		RangeType type = RangeType.getEnumByRdfType(SesameUtils.toURI(rangeUri));
		mas.put("rangeType", type.getJsonType());
		mas.remove("range");

		switch (type) {
			case CODELIST:
				JSONObject codeList = RepositoryGestion.getResponseAsObject(CodeListQueries.getCodeListNotationByUri(rangeUri));
				if (codeList != null && !codeList.isNull("notation")) {
					String codeListNotation = codeList.getString("notation");
					mas.put("codeList", codeListNotation);
				}
				break;
			default:
				break;
		}

	}

	public JSONArray getMetadataAttributes() throws RmesException {
		JSONArray attributesList = RepositoryGestion.getResponseAsArray(DocumentationsQueries.getAttributesQuery());
		if (attributesList.length() != 0) {
			 for (int i = 0; i < attributesList.length(); i++) {
		         JSONObject attribute = attributesList.getJSONObject(i);
		         transformRangeType(attribute);
		     }
		}
		return attributesList;
	}	

}
