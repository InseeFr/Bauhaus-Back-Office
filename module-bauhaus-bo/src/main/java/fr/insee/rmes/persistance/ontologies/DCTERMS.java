package fr.insee.rmes.persistance.ontologies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class DCTERMS {
	
	  private DCTERMS() {
		    throw new IllegalStateException("Utility class");
	}


	public static final String NAMESPACE = "http://purl.org/dc/terms/";

	/**
	 * The recommended prefix for the INSEE namespace: "insee"
	 */
	public static final String PREFIX = "dcterms";
	
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);
	
	public static final IRI HAS_PART;
		
	static {
		final ValueFactory f = SimpleValueFactory.getInstance();

		HAS_PART = f.createIRI(NAMESPACE, "hasPart");
	}
	
}
