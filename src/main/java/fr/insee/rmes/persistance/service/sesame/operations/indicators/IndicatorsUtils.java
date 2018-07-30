package fr.insee.rmes.persistance.service.sesame.operations.indicators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

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
		JSONArray links = RepositoryGestion.getResponseAsArray(IndicatorsQueries.indicatorLinks(idIndic));
		if (links.length() != 0) {
			indicator.put("links", links);
		}
	}


}
