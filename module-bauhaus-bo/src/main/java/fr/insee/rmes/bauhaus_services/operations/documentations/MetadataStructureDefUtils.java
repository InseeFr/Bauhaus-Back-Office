package fr.insee.rmes.bauhaus_services.operations.documentations;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.IdGenerator;
import fr.insee.rmes.model.operations.documentations.RangeType;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.operations.msd.infrastructure.graphdb.DocumentationQueries;
import fr.insee.rmes.json.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MetadataStructureDefUtils  extends RdfService {

		private static final String RANGE = "range";
		static final Logger logger = LoggerFactory.getLogger(MetadataStructureDefUtils.class);

		private final DocumentationQueries documentationQueries;

		public MetadataStructureDefUtils(RepositoryGestion repoGestion, IdGenerator idGenerator,
										 RepositoryPublication repositoryPublication,
										 PublicationUtils publicationUtils,
										 DocumentationQueries documentationQueries) {
			super(repoGestion, idGenerator, repositoryPublication, publicationUtils);
			this.documentationQueries = documentationQueries;
		}


	public void transformRangeType(JSONObject mas) throws RmesException {
		if (!mas.has(RANGE)) {throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "At least one attribute don't have range", (mas.has("id") ? mas.get(Constants.ID) : mas).toString());}
		String rangeUri = mas.getString(RANGE);
		RangeType type = RangeType.getEnumByRdfType(RdfUtils.toURI(rangeUri));
		mas.put(Constants.RANGE_TYPE, type.getJsonType());
		mas.remove(RANGE);

		if (!type.equals(RangeType.CODELIST)) {
			mas.remove(Constants.CODELIST);
		}

	}

	public Map<String,String> getMetadataAttributesUri() throws RmesException {
		Map<String,String> attributes = new HashMap<>();
		JSONArray attributesList = repoGestion.getResponseAsArray(documentationQueries.getAttributesUriQuery());
		if (!attributesList.isEmpty()) {
			 JSONUtils.stream(attributesList).forEach(attribute -> {
		         if (attribute.has(Constants.ID)&& attribute.has(Constants.URI)) {
		        	 String id = StringUtils.upperCase(attribute.getString(Constants.ID));
		        	 attributes.put(id, attribute.getString(Constants.URI));
		         }
		     });
		}
		return attributes;
	}

}
