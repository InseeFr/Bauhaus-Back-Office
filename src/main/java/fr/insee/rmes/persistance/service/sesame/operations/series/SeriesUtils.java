package fr.insee.rmes.persistance.service.sesame.operations.series;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import fr.insee.rmes.config.Config;
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
		JSONArray altLabelLg1 = RepositoryGestion.getResponseAsArray(SeriesQueries.altLabel(idSeries, Config.LG1));
		JSONArray altLabelLg2 = RepositoryGestion.getResponseAsArray(SeriesQueries.altLabel(idSeries, Config.LG2));
		if (altLabelLg1.length() != 0) {
			series.put("altLabelLg1", JSONUtils.extractFieldToArray(altLabelLg1, "altLabel"));
		}
		if (altLabelLg2.length() != 0) {
			series.put("altLabelLg2", JSONUtils.extractFieldToArray(altLabelLg2, "altLabel"));
		}
	}

	private void addSeriesOperations(String idSeries, JSONObject series) {
		JSONArray operations = RepositoryGestion.getResponseAsArray(SeriesQueries.getOperations(idSeries));
		if (operations.length() != 0) {
			series.put("operations", operations);
		}
	}


}
