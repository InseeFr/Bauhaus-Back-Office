package fr.insee.rmes.persistance.ontologies;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public class PROV {
	
	public static final String NAMESPACE = "http://www.w3.org/ns/prov#";

	/**
	 * The recommended prefix for the Prov namespace: "prov"
	 */
	public static final String PREFIX = "prov";
	
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);
	
	public static final URI WAS_GENERATED_BY;
		
	static {
		final ValueFactory f = ValueFactoryImpl.getInstance();

		WAS_GENERATED_BY = f.createURI(NAMESPACE, "wasGeneratedBy");
		
	}
	

}
