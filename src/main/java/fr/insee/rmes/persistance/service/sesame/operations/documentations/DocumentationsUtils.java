package fr.insee.rmes.persistance.service.sesame.operations.documentations;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import fr.insee.rmes.persistance.service.sesame.codeList.CodeListQueries;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;

@Component
public class DocumentationsUtils {

	public JSONObject getMetadataAttributeById(String id){
		JSONObject mas = RepositoryGestion.getResponseAsObject(DocumentationsQueries.getAttributeSpecificationQuery(id));
		transformRangeType(mas);
		mas.put("id", id);
		return mas;
	}
	
	public void transformRangeType(JSONObject mas) {
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

}
