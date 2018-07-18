package fr.insee.rmes.persistance.service.sesame.ontologies;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

// TODO Add @see tags
/**
 * Vocabulary constants for XKOS.
 * 
 * @author Franck Cotton
 */
public class XKOS {

	/**
	 * The XKOS namespace: http://rdf-vocabulary.ddialliance.org/xkos#
	 */
	public static final String NAMESPACE = "http://rdf-vocabulary.ddialliance.org/xkos#";

	/**
	 * The recommended prefix for the SKOS namespace: "xkos"
	 */
	public static final String PREFIX = "xkos";

	/**
	 * An immutable {@link Namespace} constant that represents the XKOS namespace.
	 */
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);

	/* OWL classes */

	/**
	 * The xkos:ClassificationLevel class
	 */
	public static final URI CLASSIFICATION_LEVEL;

	/**
	 * The xkos:ConceptAssociation class
	 */
	public static final URI CONCEPT_ASSOCIATION;

	/**
	 * The xkos:Correspondence class
	 */
	public static final URI CORRESPONDENCE;

	/**
	 * The xkos:ExplanatoryNote class
	 * 
	 * <strong>Experimental</strong>
	 */
	public static final URI EXPLANATORY_NOTE;
	

	/* OWL properties */

	/**
	 * The xkos:belongsTo property.
	 */
	public static final URI CASE_LAW;
	
	/**
	 * The xkos:belongsTo property.
	 */
	public static final URI BELONGS_TO;

	/**
	 * The xkos:maxLength property.
	 */
	public static final URI MAX_LENGTH;

	/**
	 * The xkos:coreContentNote property.
	 */
	public static final URI CORE_CONTENT_NOTE;

	/**
	 * The xkos:additionalContentNote property.
	 */
	public static final URI ADDITIONAL_CONTENT_NOTE;

	/**
	 * The xkos:exclusionNote property.
	 */
	public static final URI EXCLUSION_NOTE;

	/**
	 * The xkos:organisedBy property.
	 */
	public static final URI ORGANISED_BY ;

	/**
	 * The xkos:plainText property.
	 * 
	 * <strong>Experimental</strong>
	 */
	public static final URI PLAIN_TEXT;
	
	/**
	 * The xkos:madeOf property.
	 */
	public static final URI MADE_OF ;
	
	/**
	 * The xkos:sourceConcept property.
	 */
	public static final URI SOURCE_CONCEPT ;
	
	/**
	 * The xkos:targetConcept property.
	 */
	public static final URI TARGET_CONCEPT ;
	
	/**
	 * The xkos:compares property.
	 */
	public static final URI COMPARES ;
	

	static {
		final ValueFactory f = ValueFactoryImpl.getInstance();

		CLASSIFICATION_LEVEL = f.createURI(NAMESPACE, "ClassificationLevel");
		CONCEPT_ASSOCIATION = f.createURI(NAMESPACE, "ConceptAssociation");
		CORRESPONDENCE = f.createURI(NAMESPACE, "Correspondence");
		EXPLANATORY_NOTE = f.createURI(NAMESPACE, "ExplanatoryNote");
		
		BELONGS_TO = f.createURI(NAMESPACE, "belongsTo");
		CASE_LAW = f.createURI(NAMESPACE, "caseLaw");
		MAX_LENGTH = f.createURI(NAMESPACE, "maxLength");
		CORE_CONTENT_NOTE = f.createURI(NAMESPACE, "coreContentNote");
		ADDITIONAL_CONTENT_NOTE = f.createURI(NAMESPACE, "additionalContentNote");
		EXCLUSION_NOTE = f.createURI(NAMESPACE, "exclusionNote");
		PLAIN_TEXT = f.createURI(NAMESPACE, "plainText");
		MADE_OF = f.createURI(NAMESPACE, "madeOf");
		SOURCE_CONCEPT = f.createURI(NAMESPACE, "sourceConcept");
		TARGET_CONCEPT = f.createURI(NAMESPACE, "targetConcept");
		COMPARES = f.createURI(NAMESPACE, "compares");
		
		ORGANISED_BY = f.createURI(NAMESPACE, "organisedBy");
	}
}