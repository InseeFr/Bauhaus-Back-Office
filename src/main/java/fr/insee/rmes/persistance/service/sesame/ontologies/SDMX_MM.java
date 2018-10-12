package fr.insee.rmes.persistance.service.sesame.ontologies;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public class SDMX_MM {
	/**
	 * The namespace
	 */
	public static final String NAMESPACE = "http://www.w3.org/ns/sdmx-mm#";

	/**
	 * The recommended prefix for the namespace
	 */
	public static final String PREFIX = "sdmx-mm";

	/**
	 * An immutable {@link Namespace} constant that represents the namespace.
	 */
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

	/*  classes */

	public static final URI REPORTED_ATTRIBUTE;
	public static final URI METADATA_REPORT;
	public static final URI TARGET;

	
	
	static {
		final ValueFactory f = ValueFactoryImpl.getInstance();

		REPORTED_ATTRIBUTE = f.createURI(NAMESPACE, "ReportedAttribute");
		METADATA_REPORT = f.createURI(NAMESPACE,"MetadataReport");
		TARGET = f.createURI(NAMESPACE,"target");
	}
}
