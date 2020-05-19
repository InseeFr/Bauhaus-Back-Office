package fr.insee.rmes.persistance.ontologies;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public class SCHEMA {
	
	public static final String NAMESPACE = "http://schema.org/";

	/**
	 * The recommended prefix for the SCHEMA namespace: "schema"
	 */
	public static final String PREFIX = "schema";
	
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);
	
	public static final URI URL;
		
	static {
		final ValueFactory f = ValueFactoryImpl.getInstance();

		URL = f.createURI(NAMESPACE, "url");
	}
	

}
