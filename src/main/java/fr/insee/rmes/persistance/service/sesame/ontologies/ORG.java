package fr.insee.rmes.persistance.service.sesame.ontologies;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

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
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);


	/* OWL classes */

	/**
	 * The org:unitOf class
	 */
	public static final URI UNIT_OF;

	/**
	 * The org:hasUnit class
	 */
	public static final URI HAS_UNIT;

	/**
	 * The org:reportsTo class
	 */
	public static final URI REPORTS_TO;

	/**
	 * The org:linkedTo class
	 */
	public static final URI LINKED_TO;

	public static final URI ORGANIZATION;
	public static final URI ORGANIZATION_UNIT;

	static {
		final ValueFactory f = ValueFactoryImpl.getInstance();

		UNIT_OF = f.createURI(NAMESPACE, "unitOf");
		HAS_UNIT = f.createURI(NAMESPACE, "hasUnit");
		REPORTS_TO = f.createURI(NAMESPACE, "reportsTo");
		LINKED_TO = f.createURI(NAMESPACE, "linkedTo");
		 
		ORGANIZATION = f.createURI(NAMESPACE, "Organization");
		ORGANIZATION_UNIT = f.createURI(NAMESPACE, "OrganizationUnit");
	}

}
