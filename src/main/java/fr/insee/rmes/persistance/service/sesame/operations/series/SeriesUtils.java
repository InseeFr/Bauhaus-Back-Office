package fr.insee.rmes.persistance.service.sesame.operations.series;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;

@Component
public class SeriesUtils {


	final static Logger logger = LogManager.getLogger(SeriesUtils.class);


	public JSONObject getSeriesById(String id){
		JSONObject series = RepositoryGestion.getResponseAsObject(SeriesQueries.oneSeriesQuery(id));
		addSeriesOperations(id, series);
		return series;
	}


	private void addSeriesOperations(String idSeries, JSONObject series) {
		JSONArray operations = RepositoryGestion.getResponseAsArray(SeriesQueries.getOperations(idSeries));
		if (operations.length() != 0) {
			series.put("operations", operations);
		}
	}


}
