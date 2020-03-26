package fr.insee.rmes.persistance.ontologies;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public class DCTERMS {

	public static final String NAMESPACE = "http://purl.org/dc/terms/";

	/**
	 * The recommended prefix for the INSEE namespace: "insee"
	 */
	public static final String PREFIX = "dcterms";
	
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);
	
	public static final URI HASPART;
		
	static {
		final ValueFactory f = ValueFactoryImpl.getInstance();

		HASPART = f.createURI(NAMESPACE, "hasPart");
	}
	
}
