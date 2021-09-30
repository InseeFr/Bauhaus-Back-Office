package fr.insee.rmes.persistance.ontologies;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;


public class EVOC {
	
	  private EVOC() {
		    throw new IllegalStateException("Utility class");
	}


	public static final String NAMESPACE = "http://eurovoc.europa.eu/schema#";

	/**
	 * The recommended prefix for the pav namespace: "evoc"
	 */
	public static final String PREFIX = "evoc";
	
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);
	
	public static final IRI NOTE_LITERAL;
	
	static {
		final ValueFactory f = SimpleValueFactory.getInstance();

		NOTE_LITERAL = f.createIRI(NAMESPACE, "noteLiteral");
	
	}
	
}
