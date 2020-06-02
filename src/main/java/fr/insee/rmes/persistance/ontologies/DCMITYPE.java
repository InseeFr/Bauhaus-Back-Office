package fr.insee.rmes.persistance.ontologies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class DCMITYPE {
	
	public static final String NAMESPACE = "http://purl.org/dc/dcmitype/";

	/**
	 * The recommended prefix for the INSEE namespace: "insee"
	 */
	public static final String PREFIX = "dcmitype";
	
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);
	
	public static final IRI TEXT;
		
	static {
		final ValueFactory f = SimpleValueFactory.getInstance();

		TEXT = f.createIRI(NAMESPACE, "Text");
	}
	

}
