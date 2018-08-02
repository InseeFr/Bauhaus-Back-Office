package fr.insee.rmes.persistance.service.sesame.operations.series;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDFS;
import org.springframework.stereotype.Component;

import fr.insee.rmes.persistance.service.sesame.utils.QueryUtils;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;

@Component
public class SeriesUtils {


	final static Logger logger = LogManager.getLogger(SeriesUtils.class);


	public JSONObject getSeriesById(String id){
		JSONObject series = RepositoryGestion.getResponseAsObject(SeriesQueries.oneSeriesQuery(id));
		addSeriesOperations(id, series);
		addSeriesFamily(id,series);
		addSeriesLinks(id, series);
		addGeneratedWith(id, series);
		return series;
	}


	private void addSeriesOperations(String idSeries, JSONObject series) {
		JSONArray operations = RepositoryGestion.getResponseAsArray(SeriesQueries.getOperations(idSeries));
		if (operations.length() != 0) {
			series.put("operations", operations);
		}
	}
	
	private void addGeneratedWith(String idSeries, JSONObject series) {
		JSONArray generated = RepositoryGestion.getResponseAsArray(SeriesQueries.getGeneratedWith(idSeries));
		if (generated.length() != 0) {
			generated = QueryUtils.transformRdfTypeInString(generated);
			series.put("generate", generated);
		}
	}
	
	private void addSeriesFamily(String idSeries, JSONObject series) {
		JSONObject family = RepositoryGestion.getResponseAsObject(SeriesQueries.getFamily(idSeries));
		series.put("family", family);
	}

	private void addSeriesLinks(String idSeries, JSONObject series) {
		addOneTypeOfLink(idSeries,series,DCTERMS.REPLACES);
		addOneTypeOfLink(idSeries,series,DCTERMS.IS_REPLACED_BY);
		addOneTypeOfLink(idSeries,series,RDFS.SEEALSO);
		addOneOrganizationLink(idSeries,series, INSEE.STAKEHOLDER);
		addOneOrganizationLink(idSeries,series, INSEE.DATA_COLLECTOR);
	}
	
	private void addOneTypeOfLink(String id, JSONObject object, URI predicate) {
		JSONArray links = RepositoryGestion.getResponseAsArray(SeriesQueries.seriesLinks(id, predicate));
		if (links.length() != 0) {
			links = QueryUtils.transformRdfTypeInString(links);
			object.put(predicate.getLocalName(), links);
		}
	}
	
	private void addOneOrganizationLink(String id, JSONObject object, URI predicate) {
		JSONArray organizations = RepositoryGestion.getResponseAsArray(SeriesQueries.getMultipleOrganizations(id, predicate));
		if (organizations.length() != 0) {
			object.put(predicate.getLocalName(), organizations);
		}
	}
	



}
