package fr.insee.rmes.persistance.ontologies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

// TODO Add @see tags
/**
 * Vocabulary constants for XKOS.
 * 
 * @author Franck Cotton
 */
public class XKOS {
	
	  private XKOS() {
		    throw new IllegalStateException("Utility class");
	}


	/**
	 * The XKOS namespace: http://rdf-vocabulary.ddialliance.org/xkos#
	 */
	private static final String NAMESPACE = "http://rdf-vocabulary.ddialliance.org/xkos#";

	/**
	 * The recommended prefix for the SKOS namespace: "xkos"
	 */
	private static final String PREFIX = "xkos";

	/**
	 * An immutable {@link Namespace} constant that represents the XKOS namespace.
	 */
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);

	/* OWL classes */

	/**
	 * The xkos:ClassificationLevel class
	 */
	public static final IRI CLASSIFICATION_LEVEL;

	/**
	 * The xkos:ConceptAssociation class
	 */
	public static final IRI CONCEPT_ASSOCIATION;

	/**
	 * The xkos:Correspondence class
	 */
	public static final IRI CORRESPONDENCE;

	/**
	 * The xkos:ExplanatoryNote class
	 * 
	 * <strong>Experimental</strong>
	 */
	public static final IRI EXPLANATORY_NOTE;
	

	/* OWL properties */

	/**
	 * The xkos:belongsTo property.
	 */
	public static final IRI CASE_LAW;
	
	/**
	 * The xkos:belongsTo property.
	 */
	public static final IRI BELONGS_TO;

	/**
	 * The xkos:maxLength property.
	 */
	public static final IRI MAX_LENGTH;

	/**
	 * The xkos:coreContentNote property.
	 */
	public static final IRI CORE_CONTENT_NOTE;

	/**
	 * The xkos:additionalContentNote property.
	 */
	public static final IRI ADDITIONAL_CONTENT_NOTE;

	/**
	 * The xkos:exclusionNote property.
	 */
	public static final IRI EXCLUSION_NOTE;

	/**
	 * The xkos:organisedBy property.
	 */
	public static final IRI ORGANISED_BY ;

	/**
	 * The xkos:plainText property.
	 * 
	 * <strong>Experimental</strong>
	 */
	public static final IRI PLAIN_TEXT;
	
	/**
	 * The xkos:madeOf property.
	 */
	public static final IRI MADE_OF ;
	
	/**
	 * The xkos:sourceConcept property.
	 */
	public static final IRI SOURCE_CONCEPT ;
	
	/**
	 * The xkos:targetConcept property.
	 */
	public static final IRI TARGET_CONCEPT ;
	
	/**
	 * The xkos:compares property.
	 */
	public static final IRI COMPARES ;

	public static final IRI VARIANT;

	public static final IRI BEFORE;

	public static final IRI AFTER;

	static {
		final ValueFactory f = SimpleValueFactory.getInstance();

		CLASSIFICATION_LEVEL = f.createIRI(NAMESPACE, "ClassificationLevel");
		CONCEPT_ASSOCIATION = f.createIRI(NAMESPACE, "ConceptAssociation");
		CORRESPONDENCE = f.createIRI(NAMESPACE, "Correspondence");
		EXPLANATORY_NOTE = f.createIRI(NAMESPACE, "ExplanatoryNote");
		
		BELONGS_TO = f.createIRI(NAMESPACE, "belongsTo");
		CASE_LAW = f.createIRI(NAMESPACE, "caseLaw");
		MAX_LENGTH = f.createIRI(NAMESPACE, "maxLength");
		CORE_CONTENT_NOTE = f.createIRI(NAMESPACE, "coreContentNote");
		ADDITIONAL_CONTENT_NOTE = f.createIRI(NAMESPACE, "additionalContentNote");
		EXCLUSION_NOTE = f.createIRI(NAMESPACE, "exclusionNote");
		PLAIN_TEXT = f.createIRI(NAMESPACE, "plainText");
		MADE_OF = f.createIRI(NAMESPACE, "madeOf");
		SOURCE_CONCEPT = f.createIRI(NAMESPACE, "sourceConcept");
		TARGET_CONCEPT = f.createIRI(NAMESPACE, "targetConcept");
		COMPARES = f.createIRI(NAMESPACE, "compares");
		
		ORGANISED_BY = f.createIRI(NAMESPACE, "organisedBy");

		VARIANT = f.createIRI(NAMESPACE, "variant");
		BEFORE = f.createIRI(NAMESPACE, "before");
		AFTER = f.createIRI(NAMESPACE, "after");

	}
}