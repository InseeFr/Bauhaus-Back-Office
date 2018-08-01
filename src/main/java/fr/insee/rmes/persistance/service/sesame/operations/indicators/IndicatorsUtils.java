package fr.insee.rmes.persistance.service.sesame.operations.indicators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDFS;
import org.springframework.stereotype.Component;

import fr.insee.rmes.persistance.service.sesame.ontologies.PROV;
import fr.insee.rmes.persistance.service.sesame.utils.QueryUtils;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;

@Component
public class IndicatorsUtils {


	final static Logger logger = LogManager.getLogger(IndicatorsUtils.class);


	public JSONObject getIndicatorById(String id){
		JSONObject indicator = RepositoryGestion.getResponseAsObject(IndicatorsQueries.indicatorQuery(id));
		addLinks(id, indicator);
		return indicator;
	}


	private void addLinks(String idIndic, JSONObject indicator) {
		addOneTypeOfLink(idIndic,indicator,DCTERMS.REPLACES);
		addOneTypeOfLink(idIndic,indicator,DCTERMS.IS_REPLACED_BY);
		addOneTypeOfLink(idIndic,indicator,RDFS.SEEALSO);
		addOneTypeOfLink(idIndic,indicator,PROV.WAS_GENERATED_BY);
	}
	
	private void addOneTypeOfLink(String id, JSONObject object, URI predicate) {
		JSONArray links = RepositoryGestion.getResponseAsArray(IndicatorsQueries.indicatorLinks(id, predicate));
		if (links.length() != 0) {
			links = QueryUtils.transformRdfTypeInString(links);
			object.put(predicate.getLocalName(), links);
		}
	}
	


}
