package fr.insee.rmes.persistance.sparql_queries.classifications;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.freemarker.FreeMarkerUtils;
import fr.insee.rmes.graphdb.SparqlLiterals;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ClassificationCorrespondencesQueries {

	public static final String CLASSIFICATIONS = "classifications/";
	private static final String CODES_PATH = "/codes/";

    private final BauhausLanguagesProperties languages;

	public ClassificationCorrespondencesQueries(BauhausLanguagesProperties languages) {
        this.languages = languages;
	}

	public String correspondencesQuery() throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getCorrespondences.ftlh", params);
	}

	public String correspondenceQuery(String id) throws RmesException {
		String[] classificationsIds = id.split("-");
		Map<String, Object> params = new HashMap<>();
		params.put("CORRESPONDENCE_URI_SUFFIX", SparqlLiterals.literal(CODES_PATH + id));
		params.put("FIRST_ID", SparqlLiterals.literal(classificationsIds[0]));
		params.put("SECOND_ID", SparqlLiterals.literal(classificationsIds[1]));
		params.put("FIRST_CLASS_URI_PATTERN", SparqlLiterals.literal(CODES_PATH + classificationsIds[0] + "/"));
		params.put("SECOND_CLASS_URI_PATTERN", SparqlLiterals.literal(CODES_PATH + classificationsIds[1] + "/"));
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		params.put("LG2", SparqlLiterals.literal(languages.lg2()));
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getCorrespondence.ftlh", params);
	}

	public String correspondenceAssociationsQuery(String correspondenceId) throws RmesException {
		Map<String, Object> params = new HashMap<>();
		params.put("CORRESPONDENCE_URI_PATTERN", SparqlLiterals.literal(CODES_PATH + correspondenceId));
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		params.put("LG2", SparqlLiterals.literal(languages.lg2()));
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getCorrespondenceAssociations.ftlh", params);
	}

	public String correspondenceAssociationQuery(String correspondenceId, String associationId) throws RmesException {
		String[] classificationsIds = correspondenceId.split("-");
		String[] itemsIds = associationId.split("-");
		Map<String, Object> params = new HashMap<>();
		params.put("CORRESPONDENCE_URI_SUFFIX", SparqlLiterals.literal(CODES_PATH + correspondenceId));
		params.put("ASSOCIATION_URI_SUFFIX",
				SparqlLiterals.literal(CODES_PATH + correspondenceId + "/association/" + associationId));
		params.put("ASSOCIATION_ID", SparqlLiterals.literal(associationId));
		params.put("SOURCE_CLASS_ID", SparqlLiterals.literal(classificationsIds[0]));
		params.put("TARGET_CLASS_ID", SparqlLiterals.literal(classificationsIds[1]));
		params.put("SOURCE_CLASS_URI_PATTERN", SparqlLiterals.literal(CODES_PATH + classificationsIds[0] + "/"));
		params.put("TARGET_CLASS_URI_PATTERN", SparqlLiterals.literal(CODES_PATH + classificationsIds[1] + "/"));
		params.put("SOURCE_ITEM_ID", SparqlLiterals.literal(itemsIds[0]));
		params.put("TARGET_ITEM_ID", SparqlLiterals.literal(itemsIds[1]));
		params.put("LG1", SparqlLiterals.literal(languages.lg1()));
		params.put("LG2", SparqlLiterals.literal(languages.lg2()));
		return FreeMarkerUtils.buildRequest(CLASSIFICATIONS, "getCorrespondenceAssociation.ftlh", params);
	}
}
