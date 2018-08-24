package fr.insee.rmes.persistance.service.sesame.ontologies;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

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
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

	/*  classes */

	/**
	 * The xsd:string class
	 */
	public static final URI STRING;
	
	public static final URI DATE;
	
	static {
		final ValueFactory f = ValueFactoryImpl.getInstance();

		STRING = f.createURI(NAMESPACE, "string");
		DATE = f.createURI(NAMESPACE, "date");
	}
}
