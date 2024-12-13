package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.notes.DatableNote;
import fr.insee.rmes.model.notes.VersionableNote;
import fr.insee.rmes.persistance.ontologies.EVOC;
import fr.insee.rmes.persistance.ontologies.XKOS;
import fr.insee.rmes.utils.DateUtils;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XSD;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// TODO Make this class a component to be injected by spring. extract in an other utility class static methods which do not depend over instance
public class RdfUtils {

    private RdfUtils(){}

	private static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);

    static ValueFactory factory =  SimpleValueFactory.getInstance();
    static RdfServicesForRdfUtils instance;

	public static BNode createBlankNode(){ return factory.createBNode(); }
	public static String getBaseGraph(){
		return instance.getBaseGraph();
	}
	
	public static Resource conceptGraph(){
		return factory.createIRI(instance.conceptGraph());
	}

	public static Resource documentsGraph() {
		return factory.createIRI(instance.documentsGraph());
	}
	
	public static Resource operationsGraph(){
		return factory.createIRI(instance.operationsGraph());
	}

	public static Resource productsGraph(){
		return factory.createIRI(instance.productsGraph());
	}
	
	public static Resource simsGraph(String id) {
		return factory.createIRI(instance.documentationsGraph(id));
	}
	

	public static Resource simsGeographyGraph(){
		return factory.createIRI(instance.documentationsGeoGraph());
	}
	
	public static Resource structureGraph(){
		return factory.createIRI(instance.structureGraph());
	}
	public static Resource codesListGraph(){
		return factory.createIRI(instance.codesListGraph());
	}
	public static Resource codesListGraph(String id) {
		return factory.createIRI(instance.codesListGraph(id));
	}
	public static Resource classificationSerieIRI(String id) {
		return factory.createIRI(instance.classificationSerieIRI(id));
	}

	public static Resource structureComponentGraph(){
		return factory.createIRI(instance.structureComponentGraph());
	}
	
	public static Resource conceptScheme(){
		return factory.createIRI(instance.conceptScheme());
	}
	
	public static IRI objectIRI(ObjectType objType, String... ids) {
		return factory.createIRI(instance.getBaseUriGestion(objType, ids));
	}
	
	public static IRI objectIRIPublication(ObjectType objType, String id) {
		return factory.createIRI(instance.getBaseUriPublication(objType, id));
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
	public static IRI conceptIRI() {
		return objectIRI(ObjectType.CONCEPT);
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
				instance.getBaseUriGestion(ObjectType.CONCEPT)
				+ "/" + conceptId 
				+ "/" + versionableNote.getPath()
				+ "/v" + versionableNote.getVersion()
				+ "/" + versionableNote.getLang());
	}
	
	public static IRI previousVersionableNoteIRI(String conceptId, VersionableNote versionableNote) {
		String version = String.valueOf(Integer.parseInt(versionableNote.getVersion()) - 1);
		return RdfUtils.factory.createIRI(
				instance.getBaseUriGestion(ObjectType.CONCEPT)
				+ "/" + conceptId 
				+ "/" + versionableNote.getPath()
				+ "/v" + version
				+ "/" + versionableNote.getLang());
	}
	
	public static IRI datableNoteIRI(String conceptId, DatableNote datableNote) {
		String parsedDate = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now());
		return factory.createIRI(instance.getBaseUriGestion(ObjectType.CONCEPT) + "/" + conceptId + "/" + datableNote.getPath()
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

	public static Literal createLiteral(String value, IRI datatype){
		return factory.createLiteral(value, datatype);
	}

	public static Literal setLiteralInt(String number) {
		return factory.createLiteral(number, XSD.INT);
	}

	public static Literal setLiteralDateTime(String date) {
        String parsedDate = DateTimeFormatter.ISO_DATE_TIME.format(DateUtils.parseDateTime(date));
		return factory.createLiteral(parsedDate, XSD.DATETIME);
	}
	
	public static Literal setLiteralDate(String date) {
        return factory.createLiteral(DATE_FORMATTER.format(DateUtils.parseDate(date)), XSD.DATE);
	}

	public static Literal setLiteralYear(String date) {
        return factory.createLiteral(DATE_FORMATTER.format(DateUtils.parseDate(date)), XSD.GYEAR);
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

	public static IRI addTripleStringMdToXhtml2(IRI objectURI, IRI predicate, String value, String lang, String prefix, Model model, Resource graph) {
		if (value != null && !value.isEmpty()) {
			IRI uri = factory.createIRI(objectURI.toString() + "/" + prefix + "/" + lang);
			addTripleUri(objectURI, predicate, uri, model, graph);
			addTripleUri(uri, RDF.TYPE, XKOS.EXPLANATORY_NOTE, model, graph);
			addTripleLiteralXML(uri, EVOC.NOTE_LITERAL, XhtmlToMarkdownUtils.markdownToXhtml(value), model, graph);
			addTripleLanguage(uri, XSD.LANGUAGE, lang, model, graph);
			return uri;
		}
		return null;
	}

	public static void addTripleLanguage(IRI objectURI, IRI predicate, String value, Model model, Resource graph) {
		if (value != null && !value.isEmpty()) {
			model.add(objectURI, predicate, RdfUtils.setLiteralLanguage(value), graph);
		}
	}

	public static void addTripleDateTime(IRI objectURI, IRI predicate, String value, Model model, Resource graph) {
		if (value != null && !value.isEmpty()) {
			model.add(objectURI, predicate, RdfUtils.setLiteralDateTime(value), graph);
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

	public static void addTripleBNode(Resource objectURI, IRI predicat, BNode value, Model model,Resource graph) {
		if (value != null) {
			model.add(objectURI, predicat, value, graph);
		}
	}

	public static IRI createXSDIRI(String suffix){
		return factory.createIRI("http://www.w3.org/2001/XMLSchema#", suffix);
	}

	


}
