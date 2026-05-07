package fr.insee.rmes.persistance.sparql_queries.classifications;

import fr.insee.rmes.Config;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ClassificationCorrespondencesQueries {

	public static final String CLASSIFICATIONS = "classifications/";

	private final Config config;

	public ClassificationCorrespondencesQueries(Config config) {
		this.config = config;
	}

	public String correspondencesQuery() throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", config.getLg1());
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getCorrespondences.ftlh", params);
	}

	public String correspondenceQuery(String id) throws RmesException {
		String[] classificationsIds = id.split("-");
		Map<String, Object> params = new HashMap<>();
		params.put("ID", id);
		params.put("FIRST_ID", classificationsIds[0]);
		params.put("SECOND_ID", classificationsIds[1]);
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getCorrespondence.ftlh", params);
	}

	public String correspondenceAssociationsQuery(String correspondenceId) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("CORRESPONDENCE_ID", correspondenceId);
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getCorrespondenceAssociations.ftlh", params);
	}

	public String correspondenceAssociationQuery(String correspondenceId, String associationId) throws RmesException {
		String[] classificationsIds = correspondenceId.split("-");
		String[] itemsIds = associationId.split("-");
		Map<String, Object> params = new HashMap<>();
		params.put("CORRESPONDENCE_ID", correspondenceId);
		params.put("ASSOCIATION_ID", associationId);
		params.put("SOURCE_CLASS_ID", classificationsIds[0]);
		params.put("TARGET_CLASS_ID", classificationsIds[1]);
		params.put("SOURCE_ITEM_ID", itemsIds[0]);
		params.put("TARGET_ITEM_ID", itemsIds[1]);
		params.put("LG1", config.getLg1());
		params.put("LG2", config.getLg2());
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getCorrespondenceAssociation.ftlh", params);
	}
}
