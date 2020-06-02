package fr.insee.rmes.persistance.ontologies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class SCHEMA {
	
	public static final String NAMESPACE = "http://schema.org/";

	/**
	 * The recommended prefix for the SCHEMA namespace: "schema"
	 */
	public static final String PREFIX = "schema";
	
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);
	
	public static final IRI URL;
		
	static {
		final ValueFactory f = SimpleValueFactory.getInstance();

		URL = f.createIRI(NAMESPACE, "url");
	}
	

}
