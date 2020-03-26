package fr.insee.rmes.persistance.service.sesame.structures;

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

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.modele.structures.DSD;
import fr.insee.rmes.persistance.ontologies.QB;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;

@Component
public class DSDUtils {
	
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
		logger.info("Create DSD : " + dsd.getId() + " - " + dsd.getLabelLg1());
		return dsd.getId().replaceAll(" ", "-").toLowerCase();
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
		String dsdId = dsd.getId().replaceAll(" ", "-").toLowerCase();
		URI DSDURI = SesameUtils.dsdIRI(dsdId);
		RepositoryGestion.clearDSDNodeAndComponents(DSDURI);
		createRdfDSD(dsd);
		logger.info("Update DSD : " + dsd.getId() + " - " + dsd.getLabelLg1());
		return dsdId;
	}
	
	/**
	 * Dsd to sesame
	 * @throws RmesException 
	 */

	public void createRdfDSD(DSD dsd) throws RmesException {
		Model model = new LinkedHashModel();
		String dsdId = dsd.getId().replaceAll(" ", "-").toLowerCase();
		URI DSDURI = SesameUtils.dsdIRI(dsdId);
		/*Const*/
		model.add(DSDURI, RDF.TYPE, QB.DATA_STRUCTURE_DEFINITION, SesameUtils.DSDGraph());
		/*Required*/
		model.add(DSDURI, DCTERMS.IDENTIFIER, SesameUtils.setLiteralString(dsdId), SesameUtils.DSDGraph());
		model.add(DSDURI, RDFS.LABEL, SesameUtils.setLiteralString(dsd.getLabelLg1(), Config.LG1), SesameUtils.DSDGraph());
		/*Optional*/
		SesameUtils.addTripleString(DSDURI, RDFS.LABEL, dsd.getLabelLg2(), Config.LG2, model, SesameUtils.DSDGraph());
		SesameUtils.addTripleString(DSDURI, DC.DESCRIPTION, dsd.getDescriptionLg1(), Config.LG1, model, SesameUtils.DSDGraph());
		SesameUtils.addTripleString(DSDURI, DC.DESCRIPTION, dsd.getDescriptionLg2(), Config.LG2, model, SesameUtils.DSDGraph());
		
		dsd.getComponents().forEach(component->{
			URI componentIRI = SesameUtils.componentIRI(component.getId(), component.getType());
			Resource BNode = SesameUtils.blankNode();
			/* BNode */
		    model.add(DSDURI, QB.COMPONENT, BNode, SesameUtils.DSDGraph());
		    model.add(BNode, RDF.TYPE, QB.COMPONENT_SPECIFICATION, SesameUtils.DSDGraph());
		    SesameUtils.addTripleUri(BNode, QB.COMPONENT_ATTACHMENT, component.getAttachment(), model, SesameUtils.DSDGraph());
		    model.add(BNode, SesameUtils.toURI(component.getType()), componentIRI, SesameUtils.DSDGraph());
		    /* Component */
		    model.add(componentIRI, RDF.TYPE, SesameUtils.componentTypeIRI(component.getType()), SesameUtils.DSDGraph());
		    if (component.getCodeList() != null) {
				model.add(componentIRI, RDF.TYPE, QB.CODED_PROPERTY, SesameUtils.DSDGraph());
			}
		    model.add(componentIRI, DCTERMS.IDENTIFIER, SesameUtils.setLiteralString(component.getId()), SesameUtils.DSDGraph());
		    model.add(componentIRI, RDFS.LABEL, SesameUtils.setLiteralString(component.getLabelLg1(), Config.LG1), SesameUtils.DSDGraph());
		    SesameUtils.addTripleString(componentIRI, RDFS.LABEL, component.getLabelLg2(), Config.LG2, model, SesameUtils.DSDGraph());
		    SesameUtils.addTripleUri(componentIRI, QB.CODE_LIST, component.getCodeList(), model, SesameUtils.DSDGraph());
		    SesameUtils.addTripleUri(componentIRI, RDFS.RANGE, component.getRange(), model, SesameUtils.DSDGraph());
		    SesameUtils.addTripleUri(componentIRI, QB.CONCEPT, component.getConcept(), model, SesameUtils.DSDGraph());
		    
		});
		
		RepositoryGestion.loadSimpleObject(DSDURI, model, null);
	}

}
