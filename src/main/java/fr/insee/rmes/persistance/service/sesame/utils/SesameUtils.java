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

	private static final String CONCEPTS_SCHEME = Config.BASE_URI_GESTION + Config.CONCEPTS_SCHEME;
	
	public static Resource conceptGraph(){
		return factory.createURI(Config.CONCEPTS_GRAPH);
	}
	
	public static Resource operationsGraph(){
		return factory.createURI(Config.OPERATIONS_GRAPH);
	}
	
	public static Resource productsGraph(){
		return factory.createURI(Config.PRODUCTS_GRAPH);
	}
	
	public static Resource conceptScheme(){
		return factory.createURI(CONCEPTS_SCHEME);
	}
	
	public static URI objectIRI(ObjectType objType, String id) {
		return factory.createURI(objType.getBaseUri() + "/" + id);
	}
	
	public static URI conceptIRI(String id) {
		return objectIRI(ObjectType.CONCEPT, id);
	}
	
	public static URI collectionIRI(String id) {
		return objectIRI(ObjectType.COLLECTION, id);
	}
	
	
	
	public static URI versionableNoteIRI(String conceptId, VersionableNote versionableNote) {
		return SesameUtils.factory.createURI(
				ObjectType.CONCEPT.getBaseUri() 
				+ "/" + conceptId 
				+ "/" + versionableNote.getPath()
				+ "/v" + versionableNote.getVersion()
				+ "/" + versionableNote.getLang());
	}
	
	public static URI previousVersionableNoteIRI(String conceptId, VersionableNote versionableNote) {
		String version = String.valueOf(Integer.parseInt(versionableNote.getVersion()) - 1);
		return SesameUtils.factory.createURI(
				ObjectType.CONCEPT.getBaseUri()
				+ "/" + conceptId 
				+ "/" + versionableNote.getPath()
				+ "/v" + version
				+ "/" + versionableNote.getLang());
	}
	
	public static URI datableNoteIRI(String conceptId, DatableNote datableNote) {
		String text = "";

		if (Config.ENV.matches("prod|pre-prod")) {
			LocalDate date = LocalDate.now();
			text = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
		} else {
			text = new Date().toString();
		}
		String parsedDate = LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE).toString();
		return SesameUtils.factory.createURI(ObjectType.CONCEPT.getBaseUri() + "/" + conceptId + "/" + datableNote.getPath()
				+ "/" + parsedDate + "/" + datableNote.getLang());
	}
	
	public static Literal setLiteralString(String string) {
		return factory.createLiteral(string.trim());
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
		return factory.createLiteral(date, XMLSchema.DATETIME);
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

	private SesameUtils() {
	    throw new IllegalStateException("Utility class");
	}
}
