package fr.insee.rmes.bauhaus_services.rdf_utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.notes.DatableNote;
import fr.insee.rmes.model.notes.VersionableNote;
import fr.insee.rmes.persistance.ontologies.QB;
import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;

public class

RdfUtils {
	
	static ValueFactory factory = ValueFactoryImpl.getInstance();

	private static final String CONCEPTS_SCHEME = Config.BASE_URI_GESTION + Config.CONCEPTS_SCHEME;
	
	public static Resource blankNode(){
		return factory.createBNode();
	}
	
	public static Resource conceptGraph(){
		return factory.createURI(Config.CONCEPTS_GRAPH);
	}

	public static Resource documentsGraph() {
		return factory.createURI(Config.DOCUMENTS_GRAPH);
	}
	
	public static Resource operationsGraph(){
		return factory.createURI(Config.OPERATIONS_GRAPH);
	}

	public static Resource structuresComponentsGraph(){
		return factory.createURI(Config.STRUCTURE_GRAPH+ "/" + Config.STRUCTURE_COMPONENT_BASE_URI);
	}

	public static Resource productsGraph(){
		return factory.createURI(Config.PRODUCTS_GRAPH);
	}
	
	public static Resource simsGraph(String id) {
		return factory.createURI(Config.DOCUMENTATIONS_BASE_GRAPH +Config.DOCUMENTATIONS_BASE_URI+"/"+ id);
	}
	
	public static Resource structureGraph(){
		return factory.createURI(Config.STRUCTURE_GRAPH + "/" + Config.STRUCTURE_BASE_URI);
	}
	
	public static Resource conceptScheme(){
		return factory.createURI(CONCEPTS_SCHEME);
	}
	
	public static URI objectIRI(ObjectType objType, String id) {
		return factory.createURI(objType.getBaseUri() + "/" + id);
	}
	
	public static URI objectIRIPublication(ObjectType objType, String id) {
		return factory.createURI(objType.getBaseUriPublication() + "/" + id);
	}

	public static URI structureComponentAttributeIRI(String id) {
		return factory.createURI(ObjectType.getBaseUri("attributeProperty") + "/", id);
	}
	public static URI structureComponentDimensionIRI(String id) {
		return factory.createURI(ObjectType.getBaseUri("dimensionProperty") + "/", id);
	}

	public static URI structureComponentMeasureIRI(String id) {
		return factory.createURI(ObjectType.getBaseUri("measureProperty") + "/", id);
	}
	public static URI structureComponentDefinitionIRI(String structureIRI, String componentDefinitionID) {
		return factory.createURI(structureIRI + "/components/", componentDefinitionID);
	}


	public static URI conceptIRI(String id) {
		return objectIRI(ObjectType.CONCEPT, id);
	}
	
	public static URI collectionIRI(String id) {
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

	public static URI documentIRI(String id) {
		return objectIRI(ObjectType.DOCUMENT, id);
	}
	
	public static URI linkIRI(String id) {
		return objectIRI(ObjectType.LINK, id);
	}
	
	public static URI structureIRI(String id) {
		return objectIRI(ObjectType.STRUCTURE, id);
	}

	public static URI createIRI(String uri){
		return factory.createURI(uri);
	}
	public static URI componentIRI(String id, String uriType) {
		URI uri = factory.createURI(uriType);
		return objectIRI(ObjectType.getEnum(uri), id);
	}
	
	public static URI componentTypeIRI(String uriType) {
		URI uri = factory.createURI(uriType);
		if (uri.equals(QB.ATTRIBUTE)) {
			return QB.ATTRIBUTE_PROPERTY;
		} else if (uri.equals(QB.DIMENSION)) {
			return QB.DIMENSION_PROPERTY;
		} else if (uri.equals(QB.MEASURE)) {
			return QB.MEASURE_PROPERTY;
		}
		return null;
	}
	
	public static URI versionableNoteIRI(String conceptId, VersionableNote versionableNote) {
		return RdfUtils.factory.createURI(
				ObjectType.CONCEPT.getBaseUri() 
				+ "/" + conceptId 
				+ "/" + versionableNote.getPath()
				+ "/v" + versionableNote.getVersion()
				+ "/" + versionableNote.getLang());
	}
	
	public static URI previousVersionableNoteIRI(String conceptId, VersionableNote versionableNote) {
		String version = String.valueOf(Integer.parseInt(versionableNote.getVersion()) - 1);
		return RdfUtils.factory.createURI(
				ObjectType.CONCEPT.getBaseUri()
				+ "/" + conceptId 
				+ "/" + versionableNote.getPath()
				+ "/v" + version
				+ "/" + versionableNote.getLang());
	}
	
	public static URI datableNoteIRI(String conceptId, DatableNote datableNote) {
		String parsedDate = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now());
		return RdfUtils.factory.createURI(ObjectType.CONCEPT.getBaseUri() + "/" + conceptId + "/" + datableNote.getPath()
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
		return factory.createLiteral(number, XMLSchema.INT);
	}
	
	public static Literal setLiteralDateTime(String date) {
        String parsedDate = DateTimeFormatter.ISO_DATE_TIME.format(DateUtils.parseDate(date));
		return factory.createLiteral(parsedDate, XMLSchema.DATETIME);
	}
	
	public static Literal setLiteralXML(String string) {
		return factory.createLiteral(string.trim(), RDF.XMLLITERAL);
	}
	
	public static Literal setLiteralLanguage(String string) {
		return factory.createLiteral(string.trim(), XMLSchema.LANGUAGE);
	}
	
	public static URI toURI(String string) {
		return factory.createURI(string.trim());
	}
	
	/**
	 * Utils to create triples if data exist
	 */
	
	public static void addTripleString(URI objectURI, URI predicat, String value, Model model, Resource graph) {
		if (value != null && !value.isEmpty()) {
			model.add(objectURI, predicat, RdfUtils.setLiteralString(value), graph);
		}
	}
	public static void addTripleString(URI objectURI, URI predicat, String value, String lang, Model model, Resource graph) {
		if (value != null && !value.isEmpty()) {
			model.add(objectURI, predicat, RdfUtils.setLiteralString(value, lang), graph);
		}
	}
	public static void addTripleStringMdToXhtml(URI objectURI, URI predicat, String value, String lang, Model model, Resource graph) {
		if (value != null && !value.isEmpty()) {
			addTripleString(objectURI, predicat, XhtmlToMarkdownUtils.markdownToXhtml(value), lang, model, graph);	
		}
	}
	public static void addTripleDateTime(URI objectURI, URI predicat, String value, Model model, Resource graph) {
		if (value != null && !value.isEmpty()) {
			model.add(objectURI, predicat, RdfUtils.setLiteralDateTime(value), graph);
		}
	}
	public static void addTripleInt(URI objectURI, URI predicat, String value, Model model, Resource graph) {
		if (value != null && !value.isEmpty()) {
			model.add(objectURI, predicat, RdfUtils.setLiteralInt(value), graph);
		}
	}
	
	public static void addTripleLiteralXML(URI objectURI, URI predicat, String value, Model model,Resource graph) {
		if (value != null && !value.isEmpty()) {
			model.add(objectURI, predicat, RdfUtils.setLiteralXML(value), graph);
		}
	}
	public static void addTripleUri(URI objectURI, URI predicat, URI value, Model model,Resource graph) {
		if (value != null) {
			model.add(objectURI, predicat, value, graph);
		}
	}
	
	public static void addTripleUri(Resource objectURI, URI predicat, String value, Model model,Resource graph) {
		if (value != null && !value.isEmpty()) {
			model.add(objectURI, predicat, toURI(value), graph);
		}
	}
	
	public static void addTripleBNode(URI objectURI, URI predicat, BNode value, Model model,Resource graph) {
		if (value != null) {
			model.add(objectURI, predicat, value, graph);
		}
	}
	
	public static void addTripleBNode(BNode bnode, URI predicat, String value,String lang, Model model,Resource graph) {
		if (value != null  && !value.isEmpty()) {
				model.add(bnode, predicat, RdfUtils.setLiteralString(value, lang), graph);
		}
	}

	private RdfUtils() {
	    throw new IllegalStateException("Utility class");
	}

	

}
