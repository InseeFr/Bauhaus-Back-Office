package fr.insee.rmes.bauhaus_services.sesame.structures;

import java.io.IOException;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import fr.insee.rmes.bauhaus_services.sesame.utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.sesame.utils.SesameService;
import fr.insee.rmes.bauhaus_services.sesame.utils.SesameUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.structures.DSD;
import fr.insee.rmes.persistance.ontologies.QB;

@Component
public class DSDUtils extends SesameService {
	
	static final Logger logger = LogManager.getLogger(DSDUtils.class);
	
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
		URI dsdUri = SesameUtils.dsdIRI(dsdId);
		RepositoryGestion.clearDSDNodeAndComponents(dsdUri);
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
		URI dsdUri = SesameUtils.dsdIRI(dsdId);
		/*Const*/
		model.add(dsdUri, RDF.TYPE, QB.DATA_STRUCTURE_DEFINITION, SesameUtils.dsdGraph());
		/*Required*/
		model.add(dsdUri, DCTERMS.IDENTIFIER, SesameUtils.setLiteralString(dsdId), SesameUtils.dsdGraph());
		model.add(dsdUri, RDFS.LABEL, SesameUtils.setLiteralString(dsd.getLabelLg1(), Config.LG1), SesameUtils.dsdGraph());
		/*Optional*/
		SesameUtils.addTripleString(dsdUri, RDFS.LABEL, dsd.getLabelLg2(), Config.LG2, model, SesameUtils.dsdGraph());
		SesameUtils.addTripleString(dsdUri, DC.DESCRIPTION, dsd.getDescriptionLg1(), Config.LG1, model, SesameUtils.dsdGraph());
		SesameUtils.addTripleString(dsdUri, DC.DESCRIPTION, dsd.getDescriptionLg2(), Config.LG2, model, SesameUtils.dsdGraph());
		
		dsd.getComponents().forEach(component->{
			URI componentIRI = SesameUtils.componentIRI(component.getId(), component.getType());
			Resource blankNode = SesameUtils.blankNode();
			/* BNode */
		    model.add(dsdUri, QB.COMPONENT, blankNode, SesameUtils.dsdGraph());
		    model.add(blankNode, RDF.TYPE, QB.COMPONENT_SPECIFICATION, SesameUtils.dsdGraph());
		    SesameUtils.addTripleUri(blankNode, QB.COMPONENT_ATTACHMENT, component.getAttachment(), model, SesameUtils.dsdGraph());
		    model.add(blankNode, SesameUtils.toURI(component.getType()), componentIRI, SesameUtils.dsdGraph());
		    /* Component */
		    model.add(componentIRI, RDF.TYPE, SesameUtils.componentTypeIRI(component.getType()), SesameUtils.dsdGraph());
		    if (component.getCodeList() != null) {
				model.add(componentIRI, RDF.TYPE, QB.CODED_PROPERTY, SesameUtils.dsdGraph());
			}
		    model.add(componentIRI, DCTERMS.IDENTIFIER, SesameUtils.setLiteralString(component.getId()), SesameUtils.dsdGraph());
		    model.add(componentIRI, RDFS.LABEL, SesameUtils.setLiteralString(component.getLabelLg1(), Config.LG1), SesameUtils.dsdGraph());
		    SesameUtils.addTripleString(componentIRI, RDFS.LABEL, component.getLabelLg2(), Config.LG2, model, SesameUtils.dsdGraph());
		    SesameUtils.addTripleUri(componentIRI, QB.CODE_LIST, component.getCodeList(), model, SesameUtils.dsdGraph());
		    SesameUtils.addTripleUri(componentIRI, RDFS.RANGE, component.getRange(), model, SesameUtils.dsdGraph());
		    SesameUtils.addTripleUri(componentIRI, QB.CONCEPT, component.getConcept(), model, SesameUtils.dsdGraph());
		    
		});
		
		repoGestion.loadSimpleObject(dsdUri, model, null);
	}

}
