package fr.insee.rmes.persistance.ontologies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class INSEE {
    private static final ValueFactory factory = SimpleValueFactory.getInstance();

	private INSEE() {
		throw new IllegalStateException("Utility class");
	}

	public static final String NAMESPACE = "http://rdf.insee.fr/def/base#";

	public static final String PREFIX = "insee";

	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);

	private static IRI createIRI(String suffix) {
		return factory.createIRI(NAMESPACE, suffix);
	}
	public static final IRI LAST_CODE_URI_SEGMENT = INSEE.createIRI("lastCodeUriSegment");
	public static final IRI DISSEMINATIONSTATUS = INSEE.createIRI("disseminationStatus");
	public static final IRI ADDITIONALMATERIAL = INSEE.createIRI("additionalMaterial");
	public static final IRI LEGALMATERIAL = INSEE.createIRI("legalMaterial");
	public static final IRI VALIDFROM = INSEE.createIRI("validFrom");
	public static final IRI VALIDUNTIL = INSEE.createIRI("validUntil");

	public static final IRI FAMILY = INSEE.createIRI("StatisticalOperationFamily");
	public static final IRI OPERATION = INSEE.createIRI("StatisticalOperation");
	public static final IRI SERIES = INSEE.createIRI("StatisticalOperationSeries");
	public static final IRI INDICATOR = INSEE.createIRI("StatisticalIndicator");
	public static final IRI DATA_COLLECTOR = INSEE.createIRI("dataCollector");
	public static final IRI CONCEPT_VERSION = INSEE.createIRI("conceptVersion");
	public static final IRI IS_VALIDATED = INSEE.createIRI("isValidated");
	public static final IRI VALIDATION_STATE = INSEE.createIRI("validationState");
	public static final IRI CODELIST = INSEE.createIRI("codeList");
	public static final IRI STRUCTURE_CONCEPT  = INSEE.createIRI("http://id.insee.fr/concepts/definition/");
	public static final IRI SUBTITLE = INSEE.createIRI("subtitle");
	public static final IRI CONFIDENTIALITY_STATUS = INSEE.createIRI("confidentialityStatus");
	public static final IRI PROCESS_STEP = INSEE.createIRI("processStep");
	public static final IRI ARCHIVE_UNIT = INSEE.createIRI("archiveUnit");
	public static final IRI STATISTICAL_UNIT = INSEE.createIRI("statisticalUnit");
	public static final IRI STRUCTURE = INSEE.createIRI("structure");
	public static final IRI NUM_OBSERVATIONS = INSEE.createIRI("numObservations");

	public static final IRI SPATIAL_RESOLUTION = INSEE.createIRI("spatialResolution");
	public static final IRI SPATIAL_TEMPORAL = INSEE.createIRI("spatialTemporal");
	public static final IRI RUBRIQUE_SANS_OBJECT = INSEE.createIRI("rubriqueSansObjet");
}
