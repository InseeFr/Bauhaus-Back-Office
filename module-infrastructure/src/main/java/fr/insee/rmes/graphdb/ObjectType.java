package fr.insee.rmes.graphdb;

import fr.insee.rmes.Constants;
import fr.insee.rmes.graphdb.ontologies.GEO;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.graphdb.ontologies.QB;
import fr.insee.rmes.graphdb.ontologies.SDMX_MM;
import jakarta.validation.constraints.NotNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.ORG;
import org.eclipse.rdf4j.model.vocabulary.SKOS;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static fr.insee.rmes.PropertiesKeys.*;
import static java.util.function.UnaryOperator.identity;

public enum ObjectType {
	CONCEPT(Constants.CONCEPT,SKOS.CONCEPT, CONCEPTS_BASE_URI, identity()),
	COLLECTION(Constants.COLLECTION, SKOS.COLLECTION, COLLECTIONS_BASE_URI,identity()),
	FAMILY(Constants.FAMILY, INSEE.FAMILY, OP_FAMILIES_BASE_URI, identity()),
	SERIES("series", INSEE.SERIES, OP_SERIES_BASE_URI, identity()),
	OPERATION("operation", INSEE.OPERATION, OPERATIONS_BASE_URI, identity()),
	INDICATOR("indicator", INSEE.INDICATOR, PRODUCTS_BASE_URI, identity()),
	DOCUMENTATION("documentation", SDMX_MM.METADATA_REPORT, DOCUMENTATIONS_BASE_URI, identity()),
	DOCUMENT(Constants.DOCUMENT, FOAF.DOCUMENT, DOCUMENTS_BASE_URI, identity()),
	LINK("link", FOAF.DOCUMENT, LINKS_BASE_URI, identity()),
	GEO_STAT_TERRITORY("geoFeature", GEO.FEATURE, DOCUMENTATIONS_GEO_BASE_URI, identity()),
	ORGANIZATION("organization", ORG.ORGANIZATION, null, s->""),
	STRUCTURE("structure", QB.DATA_STRUCTURE_DEFINITION, STRUCTURES_BASE_URI, identity()),
	CODE_LIST(Constants.CODELIST, QB.CODE_LIST, CODE_LIST_BASE_URI, identity()),
	DATASET(Constants.DATASET, DCAT.DATASET, DATASET_BASE_URI, identity()),
	DISTRIBUTION(Constants.DISTRIBUTION, DCAT.DISTRIBUTION, DISTRIBUTION_BASE_URI, identity()),


	MEASURE_PROPERTY("measureProperty", QB.MEASURE_PROPERTY, STRUCTURES_COMPONENTS_BASE_URI, s->s+"mesure"),
	ATTRIBUTE_PROPERTY("attributeProperty", QB.ATTRIBUTE_PROPERTY, STRUCTURES_COMPONENTS_BASE_URI, s->s+"attribut"),

	DIMENSION_PROPERTY("dimensionProperty", QB.DIMENSION_PROPERTY, STRUCTURES_COMPONENTS_BASE_URI, s->s+"dimension"),

	UNDEFINED(Constants.UNDEFINED, null, null,s->"");
	
	private final IRI uri;
	@NotNull
	private final String labelType;
	private final String baseUriPropertyName;
	private final UnaryOperator<String> baseUriModifier;

	ObjectType(final String labelType, final IRI uri, final String baseUriPropertyName, final UnaryOperator<String> baseUriModifier){
		this.uri=uri;
		this.labelType=labelType;
		this.baseUriPropertyName=baseUriPropertyName;
		this.baseUriModifier=baseUriModifier;
	}

	public static Optional<ObjectType> getEnumByLabel(final String labelType) {
		return Arrays.stream(ObjectType.values()).filter(e->e.labelType.equals(labelType)).findAny();
	}



	/**
	 * Get Enum type by URI
	 */
	public static ObjectType getEnum(final IRI uri) {
		return Arrays.stream(ObjectType.values()).filter(e->Objects.equals(e.uri, uri))
				.findAny()
				.orElse(ObjectType.UNDEFINED);
	}
	
	/**
	 * Get label by URI
	 */
	public static String getLabelType(final IRI uri) {
		return ObjectType.getEnum(uri).labelType;
	}


	public String baseUriPropertyName() {
		return baseUriPropertyName;
	}

	public UnaryOperator<String> baseUriModifier() {
		return this.baseUriModifier;
	}

	public String labelType() {
		return this.labelType;
	}
}

