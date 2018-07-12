package fr.insee.rmes.persistance.service.sesame.operations.series;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.utils.JSONUtils;

@Component
public class SeriesUtils {


	final static Logger logger = LogManager.getLogger(SeriesUtils.class);


	public JSONObject getSeriesById(String id){
		JSONObject series = RepositoryGestion.getResponseAsObject(SeriesQueries.oneSeriesQuery(id));
		addSeriesAltLabel(id, series);
		addSeriesOperations(id, series);
		return series;
	}

	private void addSeriesAltLabel(String idSeries, JSONObject series) {
		JSONArray altLabel = RepositoryGestion.getResponseAsArray(SeriesQueries.altLabel(idSeries));
		if (altLabel.length() != 0) {
			series.put("altLabel", JSONUtils.extractFieldToArray(altLabel, "altLabel"));
		}
	}

	private void addSeriesOperations(String idSeries, JSONObject series) {
		JSONArray operations = RepositoryGestion.getResponseAsArray(SeriesQueries.getOperations(idSeries));
		if (operations.length() != 0) {
			series.put("operations", operations);
		}
	}


}
