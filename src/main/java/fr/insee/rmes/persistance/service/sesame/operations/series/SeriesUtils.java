package fr.insee.rmes.persistance.service.sesame.operations.series;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SKOS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.persistance.service.CodeListService;
import fr.insee.rmes.persistance.service.OrganizationsService;
import fr.insee.rmes.persistance.service.sesame.links.OperationsLink;
import fr.insee.rmes.persistance.service.sesame.ontologies.INSEE;
import fr.insee.rmes.persistance.service.sesame.utils.ObjectType;
import fr.insee.rmes.persistance.service.sesame.utils.QueryUtils;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;

@Component
public class SeriesUtils {

	@Autowired
	CodeListService codeListService;
	
	@Autowired
	OrganizationsService organizationsService;
	

	final static Logger logger = LogManager.getLogger(SeriesUtils.class);

/*READ*/
	public JSONObject getSeriesById(String id){
		JSONObject series = RepositoryGestion.getResponseAsObject(SeriesQueries.oneSeriesQuery(id));
		series.put("id", id);
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
	
	private void addOneTypeOfLink(String id, JSONObject series, URI predicate) {
		JSONArray links = RepositoryGestion.getResponseAsArray(SeriesQueries.seriesLinks(id, predicate));
		if (links.length() != 0) {
			links = QueryUtils.transformRdfTypeInString(links);
			series.put(predicate.getLocalName(), links);
		}
	}
	
	private void addOneOrganizationLink(String id, JSONObject series, URI predicate) {
		JSONArray organizations = RepositoryGestion.getResponseAsArray(SeriesQueries.getMultipleOrganizations(id, predicate));
		if (organizations.length() != 0) {
			 for (int i = 0; i < organizations.length(); i++) {
		         JSONObject orga = organizations.getJSONObject(i);
		         orga.put("type", ObjectType.ORGANIZATION.getLabelType());
		     }
			series.put(predicate.getLocalName(), organizations);
		}
	}
	
	
	
/*WRITE*/
	public void setSeries(String id, String body) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Series series = new Series(id);
		try {
			series = mapper.readerForUpdating(series).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		createRdfSeries(series);
		logger.info("Update series : " + series.getId() + " - " + series.getPrefLabelLg1());
		
	}
	
	
	public void createRdfSeries(Series series) {
		Model model = new LinkedHashModel();
		URI seriesURI = SesameUtils.objectIRI(ObjectType.SERIES,series.getId());
		/*Const*/
		model.add(seriesURI, RDF.TYPE, INSEE.SERIES, SesameUtils.operationsGraph());
		/*Required*/
		model.add(seriesURI, SKOS.PREF_LABEL, SesameUtils.setLiteralString(series.getPrefLabelLg1(), Config.LG1), SesameUtils.operationsGraph());
		/*Optional*/
		SesameUtils.addTripleString(seriesURI, SKOS.PREF_LABEL, series.getPrefLabelLg2(), Config.LG2, model, SesameUtils.operationsGraph());
		SesameUtils.addTripleString(seriesURI, SKOS.ALT_LABEL, series.getAltLabelLg1(), Config.LG1, model, SesameUtils.operationsGraph());
		SesameUtils.addTripleString(seriesURI, SKOS.ALT_LABEL, series.getAltLabelLg2(), Config.LG2, model, SesameUtils.operationsGraph());
		
		SesameUtils.addTripleString(seriesURI, DCTERMS.ABSTRACT, series.getAbstractLg1(), Config.LG1, model, SesameUtils.operationsGraph());
		SesameUtils.addTripleString(seriesURI, DCTERMS.ABSTRACT, series.getAbstractLg2(), Config.LG2, model, SesameUtils.operationsGraph());
		
		SesameUtils.addTripleString(seriesURI, SKOS.HISTORY_NOTE, series.getHistoryNoteLg1(), Config.LG1, model, SesameUtils.operationsGraph());
		SesameUtils.addTripleString(seriesURI, SKOS.HISTORY_NOTE, series.getHistoryNoteLg2(), Config.LG2, model, SesameUtils.operationsGraph());

		SesameUtils.addTripleUri(seriesURI, DCTERMS.CREATOR, organizationsService.getOrganizationUriById(series.getCreator()), model, SesameUtils.operationsGraph());
		SesameUtils.addTripleUri(seriesURI, DCTERMS.CONTRIBUTOR, organizationsService.getOrganizationUriById(series.getContributor()), model, SesameUtils.operationsGraph());
		List<OperationsLink> stakeHolders = series.getStakeHolder();
		if (stakeHolders != null){
			for (OperationsLink stakeHolder : stakeHolders) {
				SesameUtils.addTripleUri(seriesURI, INSEE.STAKEHOLDER,organizationsService.getOrganizationUriById(stakeHolder.getId()),model, SesameUtils.operationsGraph());
			}
		}
		
		List<OperationsLink> dataCollectors = series.getDataCollector();
		if (dataCollectors != null) {
			for (OperationsLink dataCollector : dataCollectors) {
				SesameUtils.addTripleUri(seriesURI, INSEE.DATA_COLLECTOR,organizationsService.getOrganizationUriById(dataCollector.getId()),model, SesameUtils.operationsGraph());
			}		
		}

		String typeUri = codeListService.getCodeUri(series.getTypeList(), series.getTypeCode());
		SesameUtils.addTripleUri(seriesURI, DCTERMS.TYPE, typeUri,model, SesameUtils.operationsGraph());
		
		String accPeriodicityUri = codeListService.getCodeUri(series.getAccrualPeriodicityList(), series.getAccrualPeriodicityCode());
		SesameUtils.addTripleUri(seriesURI, DCTERMS.ACCRUAL_PERIODICITY, accPeriodicityUri, model, SesameUtils.operationsGraph());
		

		List<OperationsLink> seeAlsos = series.getSeeAlso();
		if (seeAlsos != null) {
			for (OperationsLink seeAlso : seeAlsos) {
				String seeAlsoUri = ObjectType.getCompleteUriGestion(seeAlso.getType(), seeAlso.getId());
				SesameUtils.addTripleUri(seriesURI, RDFS.SEEALSO ,seeAlsoUri, model, SesameUtils.operationsGraph());
			}
		}
		
		List<OperationsLink> replaces = series.getReplaces();
		if (replaces != null) {
			for (OperationsLink repl : replaces) {
				String replUri = ObjectType.getCompleteUriGestion(repl.getType(), repl.getId());
				SesameUtils.addTripleUri(seriesURI, DCTERMS.REPLACES ,replUri, model, SesameUtils.operationsGraph());
			}
		}
		
		List<OperationsLink> isReplacedBys = series.getIsReplacedBy();
		if (isReplacedBys != null) {
			for (OperationsLink isRepl : isReplacedBys) {
				String isReplUri = ObjectType.getCompleteUriGestion(isRepl.getType(), isRepl.getId());
				SesameUtils.addTripleUri(seriesURI, DCTERMS.IS_REPLACED_BY ,isReplUri, model, SesameUtils.operationsGraph());
				SesameUtils.addTripleUri(SesameUtils.toURI(isReplUri), DCTERMS.REPLACES ,seriesURI, model, SesameUtils.operationsGraph());
			}
		}
		
		RepositoryGestion.keepHierarchicalOperationLinks(seriesURI,model);
		
		RepositoryGestion.loadObjectWithReplaceLinks(seriesURI, model);
	}

}
