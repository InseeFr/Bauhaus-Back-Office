package fr.insee.rmes.bauhaus_services.rdf_utils;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.persistance.ontologies.GEO;
import fr.insee.rmes.persistance.ontologies.INSEE;
import fr.insee.rmes.persistance.ontologies.ORG;
import fr.insee.rmes.persistance.ontologies.QB;
import fr.insee.rmes.persistance.ontologies.SDMX_MM;

@Component
public enum ObjectType {
	CONCEPT{
		@Override
		public String getLabelType() {return Constants.CONCEPT;}
		@Override
		public IRI getUri() {return SKOS.CONCEPT;}
		@Override
		public String getBaseUri() {return config.getConceptsBaseUri();}
	},
	COLLECTION{
		@Override
		public String getLabelType() {return Constants.COLLECTION;}
		@Override
		public IRI getUri() {return SKOS.COLLECTION;}
		@Override
		public String getBaseUri() {return config.getCollectionsBaseUri();}
	},
	FAMILY{
		@Override
		public String getLabelType() {return Constants.FAMILY;}
		@Override
		public IRI getUri() {return INSEE.FAMILY;}
		@Override
		public String getBaseUri() {return config.getOpFamiliesBaseUri();}
	},
	SERIES{
		@Override
		public String getLabelType() {return "series";}
		@Override
		public IRI getUri() {return INSEE.SERIES;}
		@Override
		public String getBaseUri() {return config.getOpSeriesBaseUri();}
	},
	OPERATION{
		@Override
		public String getLabelType() {return "operation";}
		@Override
		public IRI getUri() {return INSEE.OPERATION;}
		@Override
		public String getBaseUri() {return config.getOperationsBaseUri();}
	},
	INDICATOR{
		@Override
		public String getLabelType() {return "indicator";}
		@Override
		public IRI getUri() {return INSEE.INDICATOR;}
		@Override
		public String getBaseUri() {return config.getProductsBaseUri();}
	},
	DOCUMENTATION{
		@Override
		public String getLabelType() {return "documentation";}
		@Override
		public IRI getUri() {return SDMX_MM.METADATA_REPORT;}
		@Override
		public String getBaseUri() {return config.getDocumentationsBaseUri();}
	},
	DOCUMENT{
		@Override
		public String getLabelType() {return Constants.DOCUMENT;}
		@Override
		public IRI getUri() {return FOAF.DOCUMENT;}
		@Override
		public String getBaseUri() {return config.getDocumentsBaseUri();}
	},
	LINK{
		@Override
		public String getLabelType() {return "link";}
		@Override
		public IRI getUri() {return FOAF.DOCUMENT;}
		@Override
		public String getBaseUri() {return config.getLinksBaseUri();}
	},
	GEO_STAT_TERRITORY{
		@Override
		public String getLabelType() {return "geoFeature";}
		@Override
		public IRI getUri() {return GEO.FEATURE;}
		@Override
		public String getBaseUri() {return config.getDocumentationsGeoBaseUri();}
	},
	ORGANIZATION{
		@Override
		public String getLabelType() {return "organization";}
		@Override
		public IRI getUri() {return ORG.ORGANIZATION;}
		@Override
		public String getBaseUri() {return "";}
	},
	STRUCTURE{
		@Override
		public String getLabelType() {return "structure";}
		@Override
		public IRI getUri() {return QB.DATA_STRUCTURE_DEFINITION;}
		@Override
		public String getBaseUri() {return config.getStructuresBaseUri();}
	},
	CODE_LIST{
		@Override
		public String getLabelType() {return Constants.CODELIST;}
		@Override
		public IRI getUri() {return QB.CODE_LIST;}
		@Override
		public String getBaseUri() {return config.getCodeListBaseUri();}
	},
	MEASURE_PROPERTY{
		@Override
		public String getLabelType() {return "measureProperty";}
		@Override
		public IRI getUri() {return QB.MEASURE_PROPERTY;}
		@Override
		public String getBaseUri() {return config.getStructuresComponentsBaseUri()  + "mesure";}
	},
	ATTRIBUTE_PROPERTY{
		@Override
		public String getLabelType() {return "attributeProperty";}
		@Override
		public IRI getUri() {return QB.ATTRIBUTE_PROPERTY;}
		@Override
		public String getBaseUri() {return config.getStructuresComponentsBaseUri() + "attribut";}
	},
	DIMENSION_PROPERTY{
		@Override
		public String getLabelType() {return "dimensionProperty";}
		@Override
		public IRI getUri() {return QB.DIMENSION_PROPERTY;}
		@Override
		public String getBaseUri() {return config.getStructuresComponentsBaseUri() + "dimension";}
	},


	UNDEFINED{
		@Override
		public String getLabelType() {return Constants.UNDEFINED;}
		@Override
		public IRI getUri() {return null;}
		@Override
		public String getBaseUri() {return "";}
	};
	
	
	@Autowired 
	static Config config;
	

	ObjectType(){
	}

	public abstract IRI getUri() ;
	public abstract String getLabelType() ;
	public abstract String getBaseUri(); 
	

	
	private static Map<String, ObjectType> lookupLabel = new HashMap<>();
	private static Map<IRI, ObjectType> lookupUri = new HashMap<>();

	static {
		// Populate out lookup when enum is created
		for (ObjectType e : ObjectType.values()) {
			lookupLabel.put(e.getLabelType(), e);
			lookupUri.put(e.getUri(), e);
		}
	}
	
	/**
	 * Get Enum type by label
	 * @param label
	 * @return
	 */
	public static ObjectType getEnum(String labelType) {
		return lookupLabel.get(labelType)!=null ? lookupLabel.get(labelType): UNDEFINED;
	}
	
	/**
	 * Get URI by label
	 * @param label
	 * @return
	 */
	public static IRI getUri(String labelType) {
		return getEnum(labelType).getUri();
	}
	
	/**
	 * Get URI by label
	 * @param label
	 * @return
	 */
	public static String getBaseUri(String labelType) {
		return getEnum(labelType).getBaseUri();
	}

	/**
	 * Get Enum type by URI
	 * @param labelType
	 * @return
	 */
	public static ObjectType getEnum(IRI uri) {
		return lookupUri.get(uri)!=null ? lookupUri.get(uri): UNDEFINED;
	}
	
	/**
	 * Get label by URI
	 * @param labelType
	 * @return
	 */
	public static String getLabelType(IRI uri) {
		return getEnum(uri).getLabelType();
	}

	
	/**
	 * Get label by URI
	 * @param label
	 * @return
	 */
	public static String getCompleteUriGestion(String labelType, String id) {
		String baseUri = getBaseUri(labelType);
		return config.getBaseUriGestion() + baseUri + "/" + id;
	}
	
	public String getBaseUriPublication(){
		return config.getBaseUriPublication() + this.getBaseUri() ;
	}

}

