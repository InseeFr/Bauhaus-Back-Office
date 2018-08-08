package fr.insee.rmes.persistance.service.sesame.operations.indicators;

import java.io.IOException;
import java.util.List;

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
import fr.insee.rmes.persistance.service.CodeListService;
import fr.insee.rmes.persistance.service.OrganizationsService;
import fr.insee.rmes.persistance.service.sesame.links.OperationsLink;
import fr.insee.rmes.persistance.service.sesame.ontologies.INSEE;
import fr.insee.rmes.persistance.service.sesame.ontologies.PROV;
import fr.insee.rmes.persistance.service.sesame.utils.ObjectType;
import fr.insee.rmes.persistance.service.sesame.utils.QueryUtils;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;

@Component
public class IndicatorsUtils {


	final static Logger logger = LogManager.getLogger(IndicatorsUtils.class);

	@Autowired
	CodeListService codeListService;
	
	@Autowired
	OrganizationsService organizationsService;

	public JSONObject getIndicatorById(String id){
		JSONObject indicator = RepositoryGestion.getResponseAsObject(IndicatorsQueries.indicatorQuery(id));
		indicator.put("id", id);
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


	public void setIndicator(String id, String body) {
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
	
	public void createRdfIndicator(Indicator indicator) {
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
		
		SesameUtils.addTripleString(indicURI, DCTERMS.ABSTRACT, indicator.getAbstractLg1(), Config.LG1, model, SesameUtils.productsGraph());
		SesameUtils.addTripleString(indicURI, DCTERMS.ABSTRACT, indicator.getAbstractLg2(), Config.LG2, model, SesameUtils.productsGraph());
		
		SesameUtils.addTripleString(indicURI, SKOS.HISTORY_NOTE, indicator.getHistoryNoteLg1(), Config.LG1, model, SesameUtils.productsGraph());
		SesameUtils.addTripleString(indicURI, SKOS.HISTORY_NOTE, indicator.getHistoryNoteLg2(), Config.LG2, model, SesameUtils.productsGraph());

		SesameUtils.addTripleUri(indicURI, DCTERMS.CREATOR, organizationsService.getOrganizationUriById(indicator.getCreator()), model, SesameUtils.productsGraph());
		List<String> stakeHolders = indicator.getStakeHolder();
		if (stakeHolders != null){
			for (String stakeHolder : stakeHolders) {
				SesameUtils.addTripleUri(indicURI, INSEE.STAKEHOLDER,organizationsService.getOrganizationUriById(stakeHolder),model, SesameUtils.productsGraph());
			}
		}
		
		String accPeriodicityUri = codeListService.getCodeUri(indicator.getAccrualPeriodicityList(), indicator.getAccrualPeriodicityCode());
		SesameUtils.addTripleUri(indicURI, DCTERMS.ACCRUAL_PERIODICITY, accPeriodicityUri, model, SesameUtils.productsGraph());
		
		List<OperationsLink> seeAlsos = indicator.getSeeAlso();
		if (seeAlsos != null) {
			for (OperationsLink seeAlso : seeAlsos) {
				String seeAlsoUri = ObjectType.getCompleteUriGestion(seeAlso.getType(), seeAlso.getId());
				SesameUtils.addTripleUri(indicURI, RDFS.SEEALSO ,seeAlsoUri, model, SesameUtils.productsGraph());
			}
		}
		
		List<OperationsLink> replaces = indicator.getReplaces();
		if (replaces != null) {
			for (OperationsLink repl : replaces) {
				String replUri = ObjectType.getCompleteUriGestion(repl.getType(), repl.getId());
				SesameUtils.addTripleUri(indicURI, DCTERMS.REPLACES ,replUri, model, SesameUtils.productsGraph());
			}
		}
		
		List<OperationsLink> isReplacedBys = indicator.getIsReplacedBy();
		if (isReplacedBys != null) {
			for (OperationsLink isRepl : isReplacedBys) {
				String isReplUri = ObjectType.getCompleteUriGestion(isRepl.getType(), isRepl.getId());
				SesameUtils.addTripleUri(indicURI, DCTERMS.IS_REPLACED_BY ,isReplUri, model, SesameUtils.productsGraph());
				SesameUtils.addTripleUri(SesameUtils.toURI(isReplUri), DCTERMS.REPLACES ,indicURI, model, SesameUtils.productsGraph());
			}
		}
		
		List<OperationsLink> wasGeneratedBys = indicator.getWasGeneratedBy();
		if (wasGeneratedBys != null) {
			for (OperationsLink wGb : wasGeneratedBys) {
				String wGbUri = ObjectType.getCompleteUriGestion(wGb.getType(), wGb.getId());
				SesameUtils.addTripleUri(indicURI, PROV.WAS_GENERATED_BY ,wGbUri, model, SesameUtils.productsGraph());
			}
		}
		
		RepositoryGestion.keepHierarchicalOperationLinks(indicURI,model);
		
		RepositoryGestion.loadObjectWithReplaceLinks(indicURI, model);
	}

}
