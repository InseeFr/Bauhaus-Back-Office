package fr.insee.rmes.persistance.ontologies;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;


public class EVOC {

	public static final String NAMESPACE = "http://eurovoc.europa.eu/schema#";

	/**
	 * The recommended prefix for the pav namespace: "evoc"
	 */
	public static final String PREFIX = "evoc";
	
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);
	
	public static final URI NOTE_LITERAL;
	
	static {
		final ValueFactory f = ValueFactoryImpl.getInstance();

		NOTE_LITERAL = f.createURI(NAMESPACE, "noteLiteral");
	
	}
	
}
