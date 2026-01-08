package fr.insee.rmes.bauhaus_services.operations;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.bauhaus_services.operations.families.FamiliesUtils;
import fr.insee.rmes.bauhaus_services.operations.indicators.IndicatorsUtils;
import fr.insee.rmes.bauhaus_services.operations.operations.OperationsUtils;
import fr.insee.rmes.bauhaus_services.operations.series.SeriesUtils;
import fr.insee.rmes.graphdb.QueryUtils;
import fr.insee.rmes.modules.operations.series.domain.model.Series;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.auth.user.AuthorizeMethodDecider;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.model.operations.*;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationFamilyQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationIndicatorsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationsOperationQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import fr.insee.rmes.utils.DiacriticSorter;
import fr.insee.rmes.utils.EncodingType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

@Service
public class OperationsImpl  implements OperationsService {

	static final Logger logger = LoggerFactory.getLogger(OperationsImpl.class);

	@Autowired
	RepositoryGestion repoGestion;

	@Autowired
	AuthorizeMethodDecider authorizeMethodDecider;

	@Autowired
	SeriesUtils seriesUtils;

	@Autowired
	OperationsUtils operationsUtils;

	@Autowired
	FamiliesUtils familiesUtils;

	@Autowired
	IndicatorsUtils indicatorsUtils;


	/***************************************************************************************************
	 * SERIES
	 *
	 *****************************************************************************************************/


	@Override
	public List<PartialOperationSeries> getSeries() throws RmesException  {
		logger.info("Starting to get operation series list");
		var series = repoGestion.getResponseAsArray(OperationSeriesQueries.seriesQuery());

		return DiacriticSorter.sortGroupingByIdConcatenatingAltLabels(series,
				PartialOperationSeries[].class,
				PartialOperationSeries::label);
	}

	@Override
	public String getSeriesForSearch() throws RmesException  {
		return seriesUtils.getSeriesForSearch(null);
	}

	@Override
	public String getSeriesWithSims() throws RmesException  {
		logger.info("Starting to get series list with sims");
		JSONArray seriesArray = repoGestion.getResponseAsArray(OperationSeriesQueries.seriesWithSimsQuery());
		return QueryUtils.correctEmptyGroupConcat(seriesArray.toString());
	}

	@Override
	public String getSeriesWithStamp(String stamp) throws RmesException  {
		logger.info("Starting to get series list with sims based on a stamp");
		JSONArray series = repoGestion.getResponseAsArray(OperationSeriesQueries.seriesWithStampQuery(stamp, this.authorizeMethodDecider.isAdmin()));
		List<JSONObject> seriesList = new ArrayList<>();
		for (int i = 0; i < series.length(); i++) {
			seriesList.add(series.getJSONObject(i));
		}
		seriesList.sort(( o1,  o2) -> {
				String key1 = Normalizer.normalize(o1.getString(Constants.LABEL), Normalizer.Form.NFD);
				String key2 = Normalizer.normalize(o2.getString(Constants.LABEL), Normalizer.Form.NFD);
				return key1.compareTo(key2);
			});
		return QueryUtils.correctEmptyGroupConcat(seriesList.toString());
	}

	@Override
	public String getSeriesForSearchWithStamp(String stamp) throws RmesException {
		return seriesUtils.getSeriesForSearch(stamp);
	}

	@Override
	public Series getSeriesByID(String id) throws RmesException {
		return seriesUtils.getSeriesById(id,EncodingType.MARKDOWN);
	}


	/**
	 * Return the series in a JSONObject encoding in markdown
	 */
	@Override
	public String getSeriesJsonByID(String id) throws RmesException {
		JSONObject series = seriesUtils.getSeriesJsonById(id, EncodingType.MARKDOWN);
		return series.toString();
	}

	@Override
	public void setSeries(String id, String body) throws RmesException {
		seriesUtils.setSeries(id,body);
	}

	@Override
	public String getOperationsWithoutReport(String idSeries) throws RmesException {
		JSONArray resQuery = repoGestion.getResponseAsArray(OperationsOperationQueries.operationsWithoutSimsQuery(idSeries));
		if (resQuery.length()==1 && resQuery.getJSONObject(0).isEmpty()) {resQuery.remove(0);}
		return QueryUtils.correctEmptyGroupConcat(resQuery.toString());
	}

	@Override
	public String getOperationsWithReport(String idSeries) throws RmesException {
		JSONArray resQuery = repoGestion.getResponseAsArray(OperationsOperationQueries.operationsWithSimsQuery(idSeries));
		if (resQuery.length()==1 && resQuery.getJSONObject(0).isEmpty()) {resQuery.remove(0);}
		return QueryUtils.correctEmptyGroupConcat(resQuery.toString());
	}

	@Override
	public String createSeries(String body) throws RmesException {
		return seriesUtils.createSeries(body);
	}

	@Override
	public void setSeriesValidation(String id) throws RmesException{
		seriesUtils.setSeriesValidation(id);
	}

	/***************************************************************************************************
	 * OPERATIONS
	 *
	 *****************************************************************************************************/


	@Override
	public List<PartialOperation> getOperations() throws RmesException  {
		logger.info("Starting to get operations list");
		var operations = repoGestion.getResponseAsArray(OperationsOperationQueries.operationsQuery());

		return DiacriticSorter.sortGroupingByIdConcatenatingAltLabels(operations,
				PartialOperation[].class,
				PartialOperation::label);

	}

	@Override
	public Operation getOperationById(String id) throws RmesException {
		return operationsUtils.getOperationById(id);
	}

	/**
	 * UPDATE
	 */
	@Override
	public void setOperation(String id, String body) throws RmesException {
		operationsUtils.setOperation(id,body);
	}

	/**
	 * CREATE
	 */
	@Override
	public String createOperation(String body) throws RmesException {
		return operationsUtils.setOperation(body);				
	}

	@Override
	public void setOperationValidation(String id) throws RmesException{
		operationsUtils.setOperationValidation(id);
	}

	/***************************************************************************************************
	 * FAMILIES
	 * @throws RmesException 
	 *****************************************************************************************************/


	@Override
	public String getFamiliesForSearch() throws RmesException {
		logger.info("Starting to get families list for search");
		String resQuery = repoGestion.getResponseAsArray(OperationFamilyQueries.familiesSearchQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}


	@Override
	public void setFamily(String id, String body) throws RmesException {
		familiesUtils.setFamily(id, body);
	}

	@Override
	public String createFamily(String body) throws RmesException {
		return familiesUtils.createFamily(body);
	}

	@Override
	public void setFamilyValidation(String id) throws RmesException{
		familiesUtils.setFamilyValidation(id);
	}

	public String getSeriesWithReport(String idFamily) throws RmesException {
		JSONArray resQuery = repoGestion.getResponseAsArray(OperationsOperationQueries.seriesWithSimsQuery(idFamily));
		if (resQuery.length()==1 && resQuery.getJSONObject(0).isEmpty()) {resQuery.remove(0);}
		return QueryUtils.correctEmptyGroupConcat(resQuery.toString());
	}


	/***************************************************************************************************
	 * INDICATORS
	 * @throws RmesException 
	 *****************************************************************************************************/


	@Override
	public List<PartialOperationIndicator> getIndicators() throws RmesException {
		logger.info("Starting to get indicators list");
		var indicators = repoGestion.getResponseAsArray(OperationIndicatorsQueries.indicatorsQuery());

		return DiacriticSorter.sortGroupingByIdConcatenatingAltLabels(indicators,
				PartialOperationIndicator[].class,
				PartialOperationIndicator::label);
	}

	@Override
	public String getIndicatorsWithSims() throws RmesException {
		logger.info("Starting to get indicators list with sims");
		String resQuery = repoGestion.getResponseAsArray(OperationIndicatorsQueries.indicatorsWithSimsQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public String getIndicatorsForSearch() throws RmesException {
		return indicatorsUtils.getIndicatorsForSearch();
	}

	@Override
	public String getIndicatorJsonByID(String id) throws RmesException {
		JSONObject indicator = indicatorsUtils.getIndicatorJsonById(id);
		return indicator.toString();
	}

	@Override
	public Indicator getIndicatorById(String id) throws RmesException {
		return indicatorsUtils.getIndicatorById(id,false);
	}

	@Override
	public void setIndicator(String id, String body) throws RmesException {
		indicatorsUtils.setIndicator(id,body);
	}

	/**
	 * Publish indicator
	 * @throws RmesException 
	 */
	@Override
	public void validateIndicator(String id) throws RmesException{
		indicatorsUtils.validateIndicator(id);
	}

	/**
	 * Create indicator
	 * @throws RmesException 
	 */
	@Override
	public String setIndicator(String body) throws RmesException {
		return indicatorsUtils.setIndicator(body);
	}

}
