package fr.insee.rmes.persistance.service.sesame.ontologies;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public class DCMITYPE {
	
	public static final String NAMESPACE = "http://purl.org/dc/dcmitype/";

	/**
	 * The recommended prefix for the INSEE namespace: "insee"
	 */
	public static final String PREFIX = "dcmitype";
	
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);
	
	public static final URI TEXT;
		
	static {
		final ValueFactory f = ValueFactoryImpl.getInstance();

		TEXT = f.createURI(NAMESPACE, "Text");
	}
	

}
