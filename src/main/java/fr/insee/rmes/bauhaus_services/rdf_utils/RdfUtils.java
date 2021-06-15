package fr.insee.rmes.bauhaus_services.rdf_utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XSD;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.notes.DatableNote;
import fr.insee.rmes.model.notes.VersionableNote;
import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;

public class RdfUtils {
	
	private static final String DATE_FORMAT = "yyyy-MM-dd";

	static final Logger logger = LogManager.getLogger(RdfUtils.class);
	
	static ValueFactory factory =  SimpleValueFactory.getInstance();

	private static final String CONCEPTS_SCHEME = Config.BASE_URI_GESTION + Config.CONCEPTS_SCHEME;
	
	public static Resource blankNode(){
		return factory.createBNode();
	}
	
	public static Resource conceptGraph(){
		return factory.createIRI(Config.CONCEPTS_GRAPH);
	}

	public static Resource documentsGraph() {
		return factory.createIRI(Config.DOCUMENTS_GRAPH);
	}
	
	public static Resource operationsGraph(){
		return factory.createIRI(Config.OPERATIONS_GRAPH);
	}


	public static Resource productsGraph(){
		return factory.createIRI(Config.PRODUCTS_GRAPH);
	}
	
	public static Resource simsGraph(String id) {
		return factory.createIRI(Config.DOCUMENTATIONS_GRAPH +"/"+ id);
	}
	

	public static Resource simsGeographyGraph(){
		return factory.createIRI(Config.DOCUMENTATIONS_GEO_GRAPH);
	}
	
	public static Resource structureGraph(){
		return factory.createIRI(Config.STRUCTURES_GRAPH);
	}
	public static Resource codesListGraph(){
		return factory.createIRI(Config.CODELIST_GRAPH);
	}
	
	public static Resource structureComponentGraph(){
		return factory.createIRI(Config.STRUCTURES_COMPONENTS_GRAPH);
	}
	
	public static Resource conceptScheme(){
		return factory.createIRI(CONCEPTS_SCHEME);
	}
	
	public static IRI objectIRI(ObjectType objType, String id) {
		return factory.createIRI(objType.getBaseUri() + "/" + id);
	}
	
	public static IRI objectIRIPublication(ObjectType objType, String id) {
		return factory.createIRI(objType.getBaseUriPublication() + "/" + id);
	}

	public static IRI structureComponentAttributeIRI(String id) {
		return objectIRI(ObjectType.ATTRIBUTE_PROPERTY, id);
	}
	public static IRI structureComponentDimensionIRI(String id) {
		return objectIRI(ObjectType.DIMENSION_PROPERTY, id);
	}

	public static IRI structureComponentMeasureIRI(String id) {
		return objectIRI(ObjectType.MEASURE_PROPERTY, id);
	}

	public static IRI structureComponentDefinitionIRI(String structureIRI, String componentDefinitionID) {
		return factory.createIRI(structureIRI + "/", componentDefinitionID);
	}

	public static IRI conceptIRI(String id) {
		return objectIRI(ObjectType.CONCEPT, id);
	}
	
	public static IRI collectionIRI(String id) {
		return objectIRI(ObjectType.COLLECTION, id);
	}
	
	public static Resource familyIRI(String id) {
		return objectIRI(ObjectType.FAMILY, id);
	}
	
	public static Resource seriesIRI(String id) {
		return objectIRI(ObjectType.SERIES, id);
	}
	
	public static Resource operationIRI(String id) {
		return objectIRI(ObjectType.OPERATION, id);
	}

	public static IRI documentIRI(String id) {
		return objectIRI(ObjectType.DOCUMENT, id);
	}
	
	public static IRI linkIRI(String id) {
		return objectIRI(ObjectType.LINK, id);
	}
	
	public static IRI structureIRI(String id) {
		return objectIRI(ObjectType.STRUCTURE, id);
	}

	public static IRI codeListIRI(String id) {
		return objectIRI(ObjectType.CODE_LIST, id);
	}

	public static IRI createIRI(String uri){
		return factory.createIRI(uri);
	}

	public static IRI versionableNoteIRI(String conceptId, VersionableNote versionableNote) {
		return RdfUtils.factory.createIRI(
				ObjectType.CONCEPT.getBaseUri() 
				+ "/" + conceptId 
				+ "/" + versionableNote.getPath()
				+ "/v" + versionableNote.getVersion()
				+ "/" + versionableNote.getLang());
	}
	
	public static IRI previousVersionableNoteIRI(String conceptId, VersionableNote versionableNote) {
		String version = String.valueOf(Integer.parseInt(versionableNote.getVersion()) - 1);
		return RdfUtils.factory.createIRI(
				ObjectType.CONCEPT.getBaseUri()
				+ "/" + conceptId 
				+ "/" + versionableNote.getPath()
				+ "/v" + version
				+ "/" + versionableNote.getLang());
	}
	
	public static IRI datableNoteIRI(String conceptId, DatableNote datableNote) {
		String parsedDate = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now());
		return RdfUtils.factory.createIRI(ObjectType.CONCEPT.getBaseUri() + "/" + conceptId + "/" + datableNote.getPath()
				+ "/" + parsedDate + "/" + datableNote.getLang());
	}
	
	public static Literal setLiteralString(String string) {
		return factory.createLiteral(string.trim());
	}
	
	public static Literal setLiteralString(ValidationStatus status) {
		return factory.createLiteral(status.getValue().trim());
	}
	
	public static Literal setLiteralString(String string, String language) {
		return factory.createLiteral(string.trim(),language);
	}
	
	public static Literal setLiteralBoolean(Boolean bool) {
		return factory.createLiteral(bool);
	}
	
	public static Literal setLiteralInt(String number) {
		return factory.createLiteral(number, XSD.INT);
	}
	
	public static Literal setLiteralDateTime(String date) {
        String parsedDate = DateTimeFormatter.ISO_DATE_TIME.format(DateUtils.parseDateTime(date));
		return factory.createLiteral(parsedDate, XSD.DATETIME);
	}
	
	public static Literal setLiteralDate(String date) {
		String parsedDate = new SimpleDateFormat(DATE_FORMAT).format(DateUtils.parseDate(date));
		return factory.createLiteral(parsedDate, XSD.DATE);
	}
	
	public static Literal setLiteralXML(String string) {
		return factory.createLiteral(string.trim(), RDF.XMLLITERAL);
	}
	
	public static Literal setLiteralLanguage(String string) {
		return factory.createLiteral(string.trim(), XSD.LANGUAGE);
	}
	
	public static IRI toURI(String string) {
		return factory.createIRI(string.trim());
	}
	
	/**
	 * Utils to create triples if data exist
	 */
	
	public static void addTripleString(IRI objectURI, IRI predicat, String value, Model model, Resource graph) {
		if (value != null && !value.isEmpty()) {
			model.add(objectURI, predicat, RdfUtils.setLiteralString(value), graph);
		}
	}
	public static void addTripleString(IRI objectURI, IRI predicat, String value, String lang, Model model, Resource graph) {
		if (value != null && !value.isEmpty()) {
			model.add(objectURI, predicat, RdfUtils.setLiteralString(value, lang), graph);
		}
	}
	public static void addTripleStringMdToXhtml(IRI objectURI, IRI predicat, String value, String lang, Model model, Resource graph) {
		if (value != null && !value.isEmpty()) {
			addTripleString(objectURI, predicat, XhtmlToMarkdownUtils.markdownToXhtml(value), lang, model, graph);	
		}
	}
	public static void addTripleDateTime(IRI objectURI, IRI predicat, String value, Model model, Resource graph) {
		if (value != null && !value.isEmpty()) {
			model.add(objectURI, predicat, RdfUtils.setLiteralDateTime(value), graph);
		}
	}
	public static void addTripleDate(IRI objectURI, IRI predicat, String value, Model model, Resource graph) {
		if (value != null && !value.isEmpty()) {
			model.add(objectURI, predicat, RdfUtils.setLiteralDate(value), graph);
		}
	}
	public static void addTripleInt(IRI objectURI, IRI predicat, String value, Model model, Resource graph) {
		if (value != null && !value.isEmpty()) {
			model.add(objectURI, predicat, RdfUtils.setLiteralInt(value), graph);
		}
	}
	
	public static void addTripleLiteralXML(IRI objectURI, IRI predicat, String value, Model model,Resource graph) {
		if (value != null && !value.isEmpty()) {
			model.add(objectURI, predicat, RdfUtils.setLiteralXML(value), graph);
		}
	}
	public static void addTripleUri(IRI objectURI, IRI predicat, IRI value, Model model,Resource graph) {
		if (value != null) {
			model.add(objectURI, predicat, value, graph);
		}
	}
	
	public static void addTripleUri(Resource objectURI, IRI predicat, String value, Model model,Resource graph) {
		if (value != null && !value.isEmpty()) {
			model.add(objectURI, predicat, toURI(value), graph);
		}
	}
	
	public static void addTripleBNode(IRI objectURI, IRI predicat, BNode value, Model model,Resource graph) {
		if (value != null) {
			model.add(objectURI, predicat, value, graph);
		}
	}
	
	public static void addTripleBNode(BNode bnode, IRI predicat, String value,String lang, Model model,Resource graph) {
		if (value != null  && !value.isEmpty()) {
				model.add(bnode, predicat, RdfUtils.setLiteralString(value, lang), graph);
		}
	}
	
	private RdfUtils() {
	    throw new IllegalStateException("Utility class");
	}

	

}
