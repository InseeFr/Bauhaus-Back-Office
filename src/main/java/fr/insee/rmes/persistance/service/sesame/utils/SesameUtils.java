package fr.insee.rmes.persistance.service.sesame.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.persistance.service.sesame.notes.DatableNote;
import fr.insee.rmes.persistance.service.sesame.notes.VersionableNote;

public class SesameUtils {
	
	static ValueFactory factory = ValueFactoryImpl.getInstance();

	private final static String CONCEPTS_SCHEME = Config.BASE_URI_GESTION + Config.CONCEPTS_SCHEME;
	
	public static Resource conceptGraph(){
		Resource conceptGraph = factory.createURI(Config.CONCEPTS_GRAPH);
		return conceptGraph;
	}
	
	public static Resource operationsGraph(){
		Resource conceptGraph = factory.createURI(Config.OPERATIONS_GRAPH);
		return conceptGraph;
	}
	
	public static Resource productsGraph(){
		Resource conceptGraph = factory.createURI(Config.PRODUCTS_GRAPH);
		return conceptGraph;
	}
	
	public static Resource conceptScheme(){
		Resource conceptScheme = factory.createURI(CONCEPTS_SCHEME);
		return conceptScheme;
	}
	
	public static URI objectIRI(ObjectType objType, String id) {
		URI uri = factory.createURI(objType.getBaseUri() + "/" + id);
		return uri;
	}
	
	public static URI conceptIRI(String id) {
		return objectIRI(ObjectType.CONCEPT, id);
	}
	
	public static URI collectionIRI(String id) {
		return objectIRI(ObjectType.COLLECTION, id);
	}
	
	
	
	public static URI versionableNoteIRI(String conceptId, VersionableNote versionableNote) {
		URI noteURI = SesameUtils.factory.createURI(
				ObjectType.CONCEPT.getBaseUri() 
				+ "/" + conceptId 
				+ "/" + versionableNote.getPath()
				+ "/v" + versionableNote.getVersion()
				+ "/" + versionableNote.getLang());
		return noteURI;
	}
	
	public static URI previousVersionableNoteIRI(String conceptId, VersionableNote versionableNote) {
		String version = String.valueOf(Integer.parseInt(versionableNote.getVersion()) - 1);
		URI noteURI = SesameUtils.factory.createURI(
				ObjectType.CONCEPT.getBaseUri()
				+ "/" + conceptId 
				+ "/" + versionableNote.getPath()
				+ "/v" + version
				+ "/" + versionableNote.getLang());
		return noteURI;
	}
	
	public static URI datableNoteIRI(String conceptId, DatableNote datableNote) {
		String parsedDate = "";

		if (Config.ENV.matches("prod|pre-prod")) {
			LocalDate date = LocalDate.now();
			String text = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
			parsedDate = LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE).toString();
		} else {
			parsedDate = new Date().toString();
		}
		URI noteURI = SesameUtils.factory.createURI(ObjectType.CONCEPT.getBaseUri() + "/" + conceptId + "/" + datableNote.getPath()
				+ "/" + parsedDate + "/" + datableNote.getLang());
		return noteURI;
	}
	
	public static Literal setLiteralString(String string) {
		Literal literalString = factory.createLiteral(string);
		return literalString;
	}
	
	public static Literal setLiteralString(String string, String language) {
		Literal literalString = factory.createLiteral(string,language);
		return literalString;
	}
	
	public static Literal setLiteralBoolean(Boolean bool) {
		Literal literalBoolean = factory.createLiteral(bool);
		return literalBoolean;
	}
	
	public static Literal setLiteralInt(String number) {
		Literal literalInt = factory.createLiteral(number, XMLSchema.INT);
		return literalInt;
	}
	
	public static Literal setLiteralDateTime(String date) {
		Literal literalDate = factory.createLiteral(date, XMLSchema.DATETIME);
		return literalDate;
	}
	
	public static Literal setLiteralXML(String string) {
		Literal literalString = factory.createLiteral(string, RDF.XMLLITERAL);
		return literalString;
	}
	
	public static Literal setLiteralLanguage(String string) {
		Literal literalString = factory.createLiteral(string, XMLSchema.LANGUAGE);
		return literalString;
	}
	
	public static URI toURI(String string) {
		URI stringToResource = factory.createURI(string);
		return stringToResource;
	}
	
	/**
	 * Utils to create triples if data exist
	 */
	
	public static void addTripleString(URI objectURI, URI predicat, String value, Model model, Resource graph) {
		if (value != null && !value.isEmpty()) {
			model.add(objectURI, predicat, SesameUtils.setLiteralString(value), graph);
		}
	}
	public static void addTripleString(URI objectURI, URI predicat, String value, String lang, Model model, Resource graph) {
		if (value != null && !value.isEmpty()) {
			model.add(objectURI, predicat, SesameUtils.setLiteralString(value, lang), graph);
		}
	}
	public static void addTripleDateTime(URI objectURI, URI predicat, String value, Model model, Resource graph) {
		if (value != null && !value.isEmpty()) {
			model.add(objectURI, predicat, SesameUtils.setLiteralDateTime(value), graph);
		}
	}
	public static void addTripleInt(URI objectURI, URI predicat, String value, Model model, Resource graph) {
		if (value != null && !value.isEmpty()) {
			model.add(objectURI, predicat, SesameUtils.setLiteralInt(value), graph);
		}
	}
	
	public static void addTripleLiteralXML(URI objectURI, URI predicat, String value, Model model,Resource graph) {
		if (value != null && !value.isEmpty()) {
			model.add(objectURI, predicat, SesameUtils.setLiteralXML(value), graph);
		}
	}
	public static void addTripleUri(URI objectURI, URI predicat, URI value, Model model,Resource graph) {
		if (value != null) {
			model.add(objectURI, predicat, value, graph);
		}
	}
	
	public static void addTripleUri(URI objectURI, URI predicat, String value, Model model,Resource graph) {
		if (value != null&& !value.isEmpty()) {
			model.add(objectURI, predicat, toURI(value), graph);
		}
	}

}
