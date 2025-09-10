package fr.insee.rmes.persistance.ontologies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class PAV {
	
	  private PAV() {
		    throw new IllegalStateException("Utility class");
	}

	
	public static final String NAMESPACE = "http://purl.org/pav/";

	/**
	 * The recommended prefix for the pav namespace: "pav"
	 */
	public static final String PREFIX = "pav";
	
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);
	
	public static final IRI VERSION;
	public static final IRI LASTREFRESHEDON;
	
	static {
		final ValueFactory f = SimpleValueFactory.getInstance();

		VERSION = f.createIRI(NAMESPACE, "version");
		LASTREFRESHEDON = f.createIRI(NAMESPACE, "lastRefreshedOn");
	
	}
	
	

}
