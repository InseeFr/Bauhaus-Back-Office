package fr.insee.rmes.persistance.ontologies;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public class PAV {
	
	public static final String NAMESPACE = "http://purl.org/pav/";

	/**
	 * The recommended prefix for the pav namespace: "pav"
	 */
	public static final String PREFIX = "pav";
	
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);
	
	public static final URI VERSION;
	public static final URI LASTREFRESHEDON;
	
	static {
		final ValueFactory f = ValueFactoryImpl.getInstance();

		VERSION = f.createURI(NAMESPACE, "version");
		LASTREFRESHEDON = f.createURI(NAMESPACE, "lastRefreshedOn");
	
	}
	
	

}
