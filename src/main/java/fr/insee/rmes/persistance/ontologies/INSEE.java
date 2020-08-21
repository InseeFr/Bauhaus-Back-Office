package fr.insee.rmes.persistance.ontologies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class INSEE {
	
	public static final String NAMESPACE = "http://rdf.insee.fr/def/base#";

	/**
	 * The recommended prefix for the INSEE namespace: "insee"
	 */
	public static final String PREFIX = "insee";
	
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);
	
	public static final IRI DISSEMINATIONSTATUS;
	
	public static final IRI ADDITIONALMATERIAL;
	public static final IRI LEGALMATERIAL;
	public static final IRI VALIDFROM;
	public static final IRI VALIDUNTIL;
	public static final IRI SIMILARITY_NOTE;
	public static final IRI DIFFERENCE_NOTE;
	
	public static final IRI FAMILY;
	public static final IRI OPERATION;
	public static final IRI SERIES;
	public static final IRI INDICATOR;
	public static final IRI DOCUMENT ;
	
	public static final IRI DATA_COLLECTOR;
	
	
	/*TEST REACT*/
	public static final IRI CONCEPT_VERSION;
	public static final IRI IS_VALIDATED;
	public static final IRI VALIDATION_STATE;


	public static final IRI IDENTIFIANT_METIER;
	public static final IRI CODELIST;

	public static final IRI STRUCTURE_CONCEPT ;

	static {
		final ValueFactory f = SimpleValueFactory.getInstance();

		DISSEMINATIONSTATUS = f.createIRI(NAMESPACE, "disseminationStatus");
		
		ADDITIONALMATERIAL = f.createIRI(NAMESPACE, "additionalMaterial");
		LEGALMATERIAL = f.createIRI(NAMESPACE, "legalMaterial");
		VALIDFROM = f.createIRI(NAMESPACE, "validFrom");
		VALIDUNTIL = f.createIRI(NAMESPACE, "validUntil");
		SIMILARITY_NOTE = f.createIRI(NAMESPACE, "similarityNote");
		DIFFERENCE_NOTE = f.createIRI(NAMESPACE, "differenceNote");
		
		CONCEPT_VERSION = f.createIRI(NAMESPACE, "conceptVersion");
		IS_VALIDATED = f.createIRI(NAMESPACE, "isValidated");

		VALIDATION_STATE = f.createIRI(NAMESPACE, "validationState");

		FAMILY = f.createIRI(NAMESPACE,"StatisticalOperationFamily");
		OPERATION = f.createIRI(NAMESPACE,"StatisticalOperation");
		SERIES = f.createIRI(NAMESPACE,"StatisticalOperationSeries");
		DOCUMENT = f.createIRI(NAMESPACE,"document");
		INDICATOR = f.createIRI(NAMESPACE,"StatisticalIndicator");


		DATA_COLLECTOR = f.createIRI(NAMESPACE,"dataCollector");

		IDENTIFIANT_METIER = f.createIRI(NAMESPACE,"identifiantMetier");

		STRUCTURE_CONCEPT = f.createIRI("http://id.insee.fr/concepts/definition/");

		CODELIST = f.createIRI(NAMESPACE, "codeList");
	}
}
