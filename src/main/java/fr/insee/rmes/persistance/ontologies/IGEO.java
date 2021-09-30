package fr.insee.rmes.persistance.ontologies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class IGEO {
	
	  private IGEO() {
		    throw new IllegalStateException("Utility class");
	}


    public static final String NAMESPACE = "http://rdf.insee.fr/def/geo#";

	public static final String PREFIX = "igeo";
	
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);
	
	public static final IRI NOM;
	public static final IRI CODE_INSEE;

	
	static {
		final ValueFactory f = SimpleValueFactory.getInstance();

		NOM = f.createIRI(NAMESPACE, "nom");
		CODE_INSEE = f.createIRI(NAMESPACE, "codeINSEE");
	
	}

}
