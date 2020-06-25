package fr.insee.rmes.persistance.ontologies;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public class QB {
	
	public static final String NAMESPACE = "http://purl.org/linked-data/cube#";

	/**
	 * The recommended prefix for the Data Cube namespace: "qb"
	 */
	public static final String PREFIX = "qb";
	
	public static final Namespace NS = new NamespaceImpl(PREFIX, NAMESPACE);
	
	public static final URI DATA_STRUCTURE_DEFINITION;
	
	public static final URI COMPONENT;
	public static final URI COMPONENT_REQUIRED;

	public static final URI COMPONENT_SPECIFICATION;
	public static final URI COMPONENT_ATTACHMENT;
	public static final URI MEASURE;
	public static final URI MEASURE_PROPERTY;
	public static final URI ATTRIBUTE;
	public static final URI ATTRIBUTE_PROPERTY;
	public static final URI DIMENSION;
	public static final URI DIMENSION_PROPERTY;
	
	public static final URI CODE_LIST;
	public static final URI CODED_PROPERTY;
	
	public static final URI CONCEPT;
	public static final URI ORDER;

	
	static {
		final ValueFactory f = ValueFactoryImpl.getInstance();

		DATA_STRUCTURE_DEFINITION = f.createURI(NAMESPACE, "DataStructureDefinition");
		
		COMPONENT = f.createURI(NAMESPACE, "component");
		COMPONENT_SPECIFICATION = f.createURI(NAMESPACE, "ComponentSpecification"); 
		COMPONENT_ATTACHMENT = f.createURI(NAMESPACE, "componentAttachment");
		COMPONENT_REQUIRED = f.createURI(NAMESPACE, "componentRequired");

		MEASURE = f.createURI(NAMESPACE, "measure");
		MEASURE_PROPERTY = f.createURI(NAMESPACE, "MeasureProperty");
		ATTRIBUTE = f.createURI(NAMESPACE, "attribute");
		ATTRIBUTE_PROPERTY = f.createURI(NAMESPACE, "AttributeProperty");
		DIMENSION = f.createURI(NAMESPACE, "dimension");
		DIMENSION_PROPERTY = f.createURI(NAMESPACE, "DimensionProperty");
		
		CODE_LIST = f.createURI(NAMESPACE, "codeList");
		CODED_PROPERTY = f.createURI(NAMESPACE, "CodedProperty");
		
		CONCEPT = f.createURI(NAMESPACE, "concept");
		ORDER = f.createURI(NAMESPACE, "order");

	}

	public static String[] getURIForComponent(){
		return new String[]{MEASURE_PROPERTY.toString(), ATTRIBUTE_PROPERTY.toString(), DIMENSION_PROPERTY.toString()};
	}

}
