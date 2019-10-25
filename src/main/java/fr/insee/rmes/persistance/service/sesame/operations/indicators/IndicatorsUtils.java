package fr.insee.rmes.persistance.service.sesame.operations.indicators;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SKOS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.persistance.service.CodeListService;
import fr.insee.rmes.persistance.service.OrganizationsService;
import fr.insee.rmes.persistance.service.sesame.links.OperationsLink;
import fr.insee.rmes.persistance.service.sesame.ontologies.INSEE;
import fr.insee.rmes.persistance.service.sesame.ontologies.PROV;
import fr.insee.rmes.persistance.service.sesame.operations.series.SeriesPublication;
import fr.insee.rmes.persistance.service.sesame.utils.ObjectType;
import fr.insee.rmes.persistance.service.sesame.utils.QueryUtils;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;

@Component
public class IndicatorsUtils {


	final static Logger logger = LogManager.getLogger(IndicatorsUtils.class);

	@Autowired
	CodeListService codeListService;
	
	@Autowired
	OrganizationsService organizationsService;

	public JSONObject getIndicatorById(String id) throws RmesException{
		if (!checkIfIndicatorExists(id)) {throw new RmesNotFoundException("Indicator not found: ", id);}
		JSONObject indicator = RepositoryGestion.getResponseAsObject(IndicatorsQueries.indicatorQuery(id));
		XhtmlToMarkdownUtils.convertJSONObject(indicator);
		indicator.put("id", id);
		addLinks(id, indicator);
		return indicator;
	}


	private void addLinks(String idIndic, JSONObject indicator) throws RmesException {
		addOneTypeOfLink(idIndic,indicator,DCTERMS.REPLACES);
		addOneTypeOfLink(idIndic,indicator,DCTERMS.IS_REPLACED_BY);
		addOneTypeOfLink(idIndic,indicator,RDFS.SEEALSO);
		addOneTypeOfLink(idIndic,indicator,PROV.WAS_GENERATED_BY);
		addOneOrganizationLink(idIndic,indicator, DCTERMS.CONTRIBUTOR);
	}
	
	private void addOneTypeOfLink(String id, JSONObject object, URI predicate) throws RmesException {
		JSONArray links = RepositoryGestion.getResponseAsArray(IndicatorsQueries.indicatorLinks(id, predicate));
		if (links.length() != 0) {
			links = QueryUtils.transformRdfTypeInString(links);
			object.put(predicate.getLocalName(), links);
		}
	}
	
	private void addOneOrganizationLink(String id, JSONObject object, URI predicate) throws RmesException {
		JSONArray organizations = RepositoryGestion.getResponseAsArray(IndicatorsQueries.getMultipleOrganizations(id, predicate));
		if (organizations.length() != 0) {
			 for (int i = 0; i < organizations.length(); i++) {
		         JSONObject orga = organizations.getJSONObject(i);
		         orga.put("type", ObjectType.ORGANIZATION.getLabelType());
		     }
			 object.put(predicate.getLocalName(), organizations);
		}
	}

	/**
	 * Create
	 * @param body
	 * @return
	 * @throws RmesException 
	 */
	public String setIndicator(String body) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Indicator indicator = new Indicator();
		if (indicator.getId() == null) {
			logger.error("Create indicator cancelled - no id");
			return null;
		}
		try {
			indicator = mapper.readValue(body, Indicator.class);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		createRdfIndicator(indicator);
		logger.info("Create indicator : " + indicator.getId() + " - " + indicator.getPrefLabelLg1());
		return indicator.getId();
	}


	/**
	 * Update
	 * @param id
	 * @param body
	 * @throws RmesException 
	 */
	public void setIndicator(String id, String body) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Indicator indicator = new Indicator(id);
		try {
			indicator = mapper.readerForUpdating(indicator).readValue(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		createRdfIndicator(indicator);
		logger.info("Update indicator : " + indicator.getId() + " - " + indicator.getPrefLabelLg1());
		
	}
	
	private void createRdfIndicator(Indicator indicator) throws RmesException {
		Model model = new LinkedHashModel();
		URI indicURI = SesameUtils.objectIRI(ObjectType.INDICATOR,indicator.getId());
		/*Const*/
		model.add(indicURI, RDF.TYPE, INSEE.INDICATOR, SesameUtils.productsGraph());
		/*Required*/
		model.add(indicURI, SKOS.PREF_LABEL, SesameUtils.setLiteralString(indicator.getPrefLabelLg1(), Config.LG1), SesameUtils.productsGraph());
		/*Optional*/
		SesameUtils.addTripleString(indicURI, SKOS.PREF_LABEL, indicator.getPrefLabelLg2(), Config.LG2, model, SesameUtils.productsGraph());
		SesameUtils.addTripleString(indicURI, SKOS.ALT_LABEL, indicator.getAltLabelLg1(), Config.LG1, model, SesameUtils.productsGraph());
		SesameUtils.addTripleString(indicURI, SKOS.ALT_LABEL, indicator.getAltLabelLg2(), Config.LG2, model, SesameUtils.productsGraph());
		
		SesameUtils.addTripleStringMdToXhtml(indicURI, DCTERMS.ABSTRACT, indicator.getAbstractLg1(), Config.LG1, model, SesameUtils.productsGraph());
		SesameUtils.addTripleStringMdToXhtml(indicURI, DCTERMS.ABSTRACT, indicator.getAbstractLg2(), Config.LG2, model, SesameUtils.productsGraph());
		
		SesameUtils.addTripleStringMdToXhtml(indicURI, SKOS.HISTORY_NOTE, indicator.getHistoryNoteLg1(), Config.LG1, model, SesameUtils.productsGraph());
		SesameUtils.addTripleStringMdToXhtml(indicURI, SKOS.HISTORY_NOTE, indicator.getHistoryNoteLg2(), Config.LG2, model, SesameUtils.productsGraph());

		SesameUtils.addTripleUri(indicURI, DCTERMS.CREATOR, organizationsService.getOrganizationUriById(indicator.getCreator()), model, SesameUtils.productsGraph());
		List<OperationsLink> contributors = indicator.getContributor();
		if (contributors != null){//partenaires
			for (OperationsLink contributor : contributors) {
				SesameUtils.addTripleUri(indicURI, DCTERMS.CONTRIBUTOR,organizationsService.getOrganizationUriById(contributor.getId()),model, SesameUtils.productsGraph());
			}
		}
		
		String gestionnaire=indicator.getGestionnaire();
		if (!StringUtils.isEmpty(gestionnaire)) {
			SesameUtils.addTripleUri(indicURI, INSEE.GESTIONNAIRE, organizationsService.getOrganizationUriById(gestionnaire), model, SesameUtils.productsGraph());
		}
		
		String accPeriodicityUri = codeListService.getCodeUri(indicator.getAccrualPeriodicityList(), indicator.getAccrualPeriodicityCode());
		SesameUtils.addTripleUri(indicURI, DCTERMS.ACCRUAL_PERIODICITY, accPeriodicityUri, model, SesameUtils.productsGraph());
		
		addOneWayLink(model, indicURI, indicator.getSeeAlso(), RDFS.SEEALSO);
		addOneWayLink(model, indicURI,  indicator.getReplaces(), DCTERMS.REPLACES);
		addOneWayLink(model, indicURI, indicator.getWasGeneratedBy(), PROV.WAS_GENERATED_BY);
		
		List<OperationsLink> isReplacedBys = indicator.getIsReplacedBy();
		if (isReplacedBys != null) {
			for (OperationsLink isRepl : isReplacedBys) {
				String isReplUri = ObjectType.getCompleteUriGestion(isRepl.getType(), isRepl.getId());
				SesameUtils.addTripleUri(indicURI, DCTERMS.IS_REPLACED_BY ,isReplUri, model, SesameUtils.productsGraph());
				SesameUtils.addTripleUri(SesameUtils.toURI(isReplUri), DCTERMS.REPLACES ,indicURI, model, SesameUtils.productsGraph());
			}
		}
		
		RepositoryGestion.keepHierarchicalOperationLinks(indicURI,model);
		
		RepositoryGestion.loadObjectWithReplaceLinks(indicURI, model);
	}


	public String setIndicatorValidation(String id)  throws RmesUnauthorizedException, RmesException  {
		Model model = new LinkedHashModel();
		
		//TODO Check autorisation
			IndicatorPublication.publishIndicator(id);
		
			URI indicatorURI = SesameUtils.objectIRI(ObjectType.INDICATOR, id);
			model.add(indicatorURI, INSEE.VALIDATION_STATE, SesameUtils.setLiteralString(INSEE.VALIDATED), SesameUtils.operationsGraph());
			model.remove(indicatorURI, INSEE.VALIDATION_STATE, SesameUtils.setLiteralString(INSEE.UNPUBLISHED), SesameUtils.operationsGraph());
			model.remove(indicatorURI, INSEE.VALIDATION_STATE, SesameUtils.setLiteralString(INSEE.MODIFIED), SesameUtils.operationsGraph());
			logger.info("Validate indicator : " + indicatorURI);

			RepositoryGestion.objectsValidation(indicatorURI, model);
			
		return id;
	}
	
	
	
	private void addOneWayLink(Model model, URI indicURI, List<OperationsLink> links, URI linkPredicate) {
		if (links != null) {
			for (OperationsLink oneLink : links) {
				String linkedObjectUri = ObjectType.getCompleteUriGestion(oneLink.getType(), oneLink.getId());
				SesameUtils.addTripleUri(indicURI, linkPredicate ,linkedObjectUri, model, SesameUtils.productsGraph());
			}
		}
	}


	public String createID() throws RmesException {
		logger.info("Generate indicator id");
		JSONObject json = RepositoryGestion.getResponseAsObject(IndicatorsQueries.lastID());
		logger.debug("JSON for indicator id : " + json);
		if (json.length()==0) {return null;}
		String id = json.getString("id");
		if (id.equals("undefined")) {return null;}
		int ID = Integer.parseInt(id.substring(1))+1;
		return "p" + ID;
	}

	public static Boolean checkIfIndicatorExists(String id) throws RmesException {
		return RepositoryGestion.getResponseAsBoolean(IndicatorsQueries.checkIfExists(id));
	}


}
