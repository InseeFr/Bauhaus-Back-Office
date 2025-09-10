package fr.insee.rmes.persistance.ontologies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class SDMX_MM {
	
	  private SDMX_MM() {
		    throw new IllegalStateException("Utility class");
	}

	
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
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);

	/*  classes */

	public static final IRI REPORTED_ATTRIBUTE;
	public static final IRI METADATA_REPORT;
	public static final IRI METADATA_REPORT_PREDICATE;
	public static final IRI TARGET;

	
	
	static {
		final ValueFactory f = SimpleValueFactory.getInstance();

		REPORTED_ATTRIBUTE = f.createIRI(NAMESPACE, "ReportedAttribute");
		METADATA_REPORT = f.createIRI(NAMESPACE,"MetadataReport");
		METADATA_REPORT_PREDICATE = f.createIRI(NAMESPACE,"metadataReport");
		TARGET = f.createIRI(NAMESPACE,"target");
	}
}
