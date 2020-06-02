package fr.insee.rmes.persistance.ontologies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class XSD {
	/**
	 * The XSD namespace: http://www.w3.org/2001/XMLSchema#
	 */
	public static final String NAMESPACE = "http://www.w3.org/2001/XMLSchema#";

	/**
	 * The recommended prefix for the XSD namespace: "xsd"
	 */
	public static final String PREFIX = "xsd";

	/**
	 * An immutable {@link Namespace} constant that represents the XSD namespace.
	 */
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);

	/*  classes */

	/**
	 * The xsd:string class
	 */
	public static final IRI STRING;

	public static final IRI DATETIME;
	
	static {
		final ValueFactory f = SimpleValueFactory.getInstance();

		STRING = f.createIRI(NAMESPACE, "string");
		DATETIME = f.createIRI(NAMESPACE, "dateTime");
	}
}
