package fr.insee.rmes.persistance.ontologies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class QB {
	
	  private QB() {
		    throw new IllegalStateException("Utility class");
	}

	
	public static final String NAMESPACE = "http://purl.org/linked-data/cube#";

	/**
	 * The recommended prefix for the Data Cube namespace: "qb"
	 */
	public static final String PREFIX = "qb";
	
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);
	
	public static final IRI DATA_STRUCTURE_DEFINITION;
	

	public static final IRI COMPONENT;
	public static final IRI COMPONENT_REQUIRED;
	public static final IRI COMPONENT_SPECIFICATION;
	public static final IRI COMPONENT_ATTACHMENT;
	public static final IRI MEASURE;
	public static final IRI MEASURE_PROPERTY;
	public static final IRI ATTRIBUTE;
	public static final IRI ATTRIBUTE_PROPERTY;
	public static final IRI DIMENSION;
	public static final IRI DIMENSION_PROPERTY;

	public static final IRI CODE_LIST;
	public static final IRI CODED_PROPERTY;
	

	public static final IRI CONCEPT;
	public static final IRI ORDER;


	static {
		final ValueFactory f = SimpleValueFactory.getInstance();

		DATA_STRUCTURE_DEFINITION = f.createIRI(NAMESPACE, "DataStructureDefinition");
		

		COMPONENT = f.createIRI(NAMESPACE, "component");
		COMPONENT_SPECIFICATION = f.createIRI(NAMESPACE, "ComponentSpecification"); 
		COMPONENT_ATTACHMENT = f.createIRI(NAMESPACE, "componentAttachment");
		COMPONENT_REQUIRED = f.createIRI(NAMESPACE, "componentRequired");

		MEASURE = f.createIRI(NAMESPACE, "measure");
		MEASURE_PROPERTY = f.createIRI(NAMESPACE, "MeasureProperty");
		ATTRIBUTE = f.createIRI(NAMESPACE, "attribute");
		ATTRIBUTE_PROPERTY = f.createIRI(NAMESPACE, "AttributeProperty");
		DIMENSION = f.createIRI(NAMESPACE, "dimension");
		DIMENSION_PROPERTY = f.createIRI(NAMESPACE, "DimensionProperty");

		CODE_LIST = f.createIRI(NAMESPACE, "codeList");
		CODED_PROPERTY = f.createIRI(NAMESPACE, "CodedProperty");
		
		CONCEPT = f.createIRI(NAMESPACE, "concept");
		ORDER = f.createIRI(NAMESPACE, "order");

	}

	public static String[] getURIForComponent(){
		return new String[]{((SimpleIRI)MEASURE_PROPERTY).toString(), ((SimpleIRI)ATTRIBUTE_PROPERTY).toString(), ((SimpleIRI)DIMENSION_PROPERTY).toString()};

	}

}
