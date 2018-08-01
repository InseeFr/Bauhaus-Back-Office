package fr.insee.rmes.persistance.service.sesame.ontologies;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public class INSEE {
	
	public static final String NAMESPACE = "http://rdf.insee.fr/def/base#";

	/**
	 * The recommended prefix for the INSEE namespace: "insee"
	 */
	public static final String PREFIX = "insee";
	
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);
	
	public static final URI DISSEMINATIONSTATUS;
	
	public static final URI ADDITIONALMATERIAL;
	public static final URI LEGALMATERIAL;
	public static final URI VALIDFROM;
	public static final URI VALIDUNTIL;
	public static final URI SIMILARITY_NOTE;
	public static final URI DIFFERENCE_NOTE;
	
	public static final URI FAMILY;
	public static final URI OPERATION;
	public static final URI SERIES;
	public static final URI INDICATOR;
	
	/*TEST REACT*/
	public static final URI CONCEPT_VERSION;
	public static final URI IS_VALIDATED;
	
	static {
		final ValueFactory f = ValueFactoryImpl.getInstance();

		DISSEMINATIONSTATUS = f.createURI(NAMESPACE, "disseminationStatus");
		
		ADDITIONALMATERIAL = f.createURI(NAMESPACE, "additionalMaterial");
		LEGALMATERIAL = f.createURI(NAMESPACE, "legalMaterial");
		VALIDFROM = f.createURI(NAMESPACE, "validFrom");
		VALIDUNTIL = f.createURI(NAMESPACE, "validUntil");
		SIMILARITY_NOTE = f.createURI(NAMESPACE, "similarityNote");
		DIFFERENCE_NOTE = f.createURI(NAMESPACE, "differenceNote");
		
		CONCEPT_VERSION = f.createURI(NAMESPACE, "conceptVersion");
		IS_VALIDATED = f.createURI(NAMESPACE, "isValidated");
		
		FAMILY = f.createURI(NAMESPACE,"StatisticalOperationFamily");
		OPERATION = f.createURI(NAMESPACE,"StatisticalOperation");
		SERIES = f.createURI(NAMESPACE,"StatisticalOperationSeries");
		INDICATOR = f.createURI(NAMESPACE,"StatisticalIndicator");

	}
	

}
