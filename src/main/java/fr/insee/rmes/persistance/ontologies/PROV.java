package fr.insee.rmes.persistance.ontologies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class PROV {
	
	public static final String NAMESPACE = "http://www.w3.org/ns/prov#";

	/**
	 * The recommended prefix for the Prov namespace: "prov"
	 */
	public static final String PREFIX = "prov";
	
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);
	
	public static final IRI WAS_GENERATED_BY;
		
	static {
		final ValueFactory f = SimpleValueFactory.getInstance();

		WAS_GENERATED_BY = f.createIRI(NAMESPACE, "wasGeneratedBy");
		
	}
	

}
