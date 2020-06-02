package fr.insee.rmes.persistance.ontologies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class ORG {
	/**
	 * The ORG namespace: http://www.w3.org/ns/org#
	 */
	public static final String NAMESPACE = "http://www.w3.org/ns/org#";

	/**
	 * The recommended prefix for the namespace: "org"
	 */
	public static final String PREFIX = "org";

	/**
	 * An immutable {@link Namespace} constant that represents the XKOS namespace.
	 */
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);


	/* OWL classes */

	/**
	 * The org:unitOf class
	 */
	public static final IRI UNIT_OF;

	/**
	 * The org:hasUnit class
	 */
	public static final IRI HAS_UNIT;

	/**
	 * The org:reportsTo class
	 */
	public static final IRI REPORTS_TO;

	/**
	 * The org:linkedTo class
	 */
	public static final IRI LINKED_TO;

	public static final IRI ORGANIZATION;
	public static final IRI ORGANIZATION_UNIT;

	static {
		final ValueFactory f = SimpleValueFactory.getInstance();

		UNIT_OF = f.createIRI(NAMESPACE, "unitOf");
		HAS_UNIT = f.createIRI(NAMESPACE, "hasUnit");
		REPORTS_TO = f.createIRI(NAMESPACE, "reportsTo");
		LINKED_TO = f.createIRI(NAMESPACE, "linkedTo");
		 
		ORGANIZATION = f.createIRI(NAMESPACE, "Organization");
		ORGANIZATION_UNIT = f.createIRI(NAMESPACE, "OrganizationUnit");
	}

}
