package fr.insee.rmes.bauhaus_services.structures;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.rmes.model.structures.StructureComponent;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.ontologies.XSD;
import fr.insee.rmes.persistance.sparql_queries.structures.DSDQueries;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.structures.DSD;
import fr.insee.rmes.persistance.ontologies.QB;

@Component
public class DSDUtils extends RdfService {
	
	static final Logger logger = LogManager.getLogger(DSDUtils.class);

    public static String formatComponent(RepositoryGestion repoGestion, String id, JSONObject response) throws RmesException {
    	response.put("id", id);
		addCodeListRange(response);
		addStructures(response, id, repoGestion);
    	return response.toString();

    }

	private static void addStructures(JSONObject response, String id, RepositoryGestion repoGestion) throws RmesException {
		JSONArray structures = repoGestion.getResponseAsArray(DSDQueries.getStructuresForComponent(id));
		response.put("structures", structures);
	}

	private static void addCodeListRange(JSONObject response) {
    	if(response.has("codeList")){
    		response.put("range", INSEE.CODELIST.toString());
		}
	}

    public String setDSD(String body) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
				DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		DSD dsd = null;
		try {
			dsd = mapper.readValue(body, DSD.class);
		} catch (IOException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "IOException");
		}
		createRdfDSD(dsd);
		logger.info("Create DSD : {} - {}" , dsd.getId() , dsd.getLabelLg1());
		return dsd.getId().replace(" ", "-").toLowerCase();
	}
	
	public String setDSD(String id, String body) throws RmesException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
				DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		DSD dsd = new DSD(id);
		try {
			dsd = mapper.readerForUpdating(dsd).readValue(body);
		} catch (IOException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "IOException");
		}
		String dsdId = dsd.getId().replace(" ", "-").toLowerCase();
		URI dsdUri = RdfUtils.dsdIRI(dsdId);
		repoGestion.clearDSDNodeAndComponents(dsdUri);
		createRdfDSD(dsd);
		logger.info("Update DSD : {} - {}" , dsd.getId() , dsd.getLabelLg1());
		return dsdId;
	}
	
	/**
	 * Dsd to sesame
	 * @throws RmesException 
	 */

	public void createRdfDSD(DSD dsd) throws RmesException {
		Model model = new LinkedHashModel();
		String dsdId = dsd.getId().replace(" ", "-").toLowerCase();
		URI dsdUri = RdfUtils.dsdIRI(dsdId);
		/*Const*/
		model.add(dsdUri, RDF.TYPE, QB.DATA_STRUCTURE_DEFINITION, RdfUtils.dsdGraph());
		/*Required*/
		model.add(dsdUri, DCTERMS.IDENTIFIER, RdfUtils.setLiteralString(dsdId), RdfUtils.dsdGraph());
		model.add(dsdUri, RDFS.LABEL, RdfUtils.setLiteralString(dsd.getLabelLg1(), Config.LG1), RdfUtils.dsdGraph());
		/*Optional*/
		RdfUtils.addTripleString(dsdUri, RDFS.LABEL, dsd.getLabelLg2(), Config.LG2, model, RdfUtils.dsdGraph());
		RdfUtils.addTripleString(dsdUri, DC.DESCRIPTION, dsd.getDescriptionLg1(), Config.LG1, model, RdfUtils.dsdGraph());
		RdfUtils.addTripleString(dsdUri, DC.DESCRIPTION, dsd.getDescriptionLg2(), Config.LG2, model, RdfUtils.dsdGraph());
		
		dsd.getComponents().forEach(component->{
			URI componentIRI = RdfUtils.componentIRI(component.getId(), component.getType());
			Resource blankNode = RdfUtils.blankNode();
			/* BNode */
		    model.add(dsdUri, QB.COMPONENT, blankNode, RdfUtils.dsdGraph());
		    model.add(blankNode, RDF.TYPE, QB.COMPONENT_SPECIFICATION, RdfUtils.dsdGraph());
		    RdfUtils.addTripleUri(blankNode, QB.COMPONENT_ATTACHMENT, component.getAttachment(), model, RdfUtils.dsdGraph());
		    model.add(blankNode, RdfUtils.toURI(component.getType()), componentIRI, RdfUtils.dsdGraph());
		    /* Component */
		    model.add(componentIRI, RDF.TYPE, RdfUtils.componentTypeIRI(component.getType()), RdfUtils.dsdGraph());
		    if (component.getCodeList() != null) {
				model.add(componentIRI, RDF.TYPE, QB.CODED_PROPERTY, RdfUtils.dsdGraph());
			}
		    model.add(componentIRI, DCTERMS.IDENTIFIER, RdfUtils.setLiteralString(component.getId()), RdfUtils.dsdGraph());
		    model.add(componentIRI, RDFS.LABEL, RdfUtils.setLiteralString(component.getLabelLg1(), Config.LG1), RdfUtils.dsdGraph());
		    RdfUtils.addTripleString(componentIRI, RDFS.LABEL, component.getLabelLg2(), Config.LG2, model, RdfUtils.dsdGraph());
		    RdfUtils.addTripleUri(componentIRI, QB.CODE_LIST, component.getCodeList(), model, RdfUtils.dsdGraph());
		    RdfUtils.addTripleUri(componentIRI, RDFS.RANGE, component.getRange(), model, RdfUtils.dsdGraph());
		    RdfUtils.addTripleUri(componentIRI, QB.CONCEPT, component.getConcept(), model, RdfUtils.dsdGraph());
		    
		});
		
		repoGestion.loadSimpleObject(dsdUri, model, null);
	}

}
