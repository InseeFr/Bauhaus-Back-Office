package fr.insee.rmes.bauhaus_services.concepts.concepts;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.dissemination_status.DisseminationStatus;
import fr.insee.rmes.persistance.sparql_queries.concepts.CollectionsQueries;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptsQueries;
import fr.insee.rmes.utils.JSONUtils;
import fr.insee.rmes.utils.StringComparator;
import fr.insee.rmes.utils.XhtmlTags;


@Component
public class ConceptsExportBuilder  extends RdfService {

	private static final String CONCEPT_VERSION = "conceptVersion";
	@Autowired 
	ConceptsUtils conceptsUtils;

	public JSONObject getConceptData(String id) throws RmesException {
		JSONObject data = new JSONObject();
		JSONObject general = conceptsUtils.getConceptById(id);
		if (general.has(Constants.ALT_LABEL_LG1)) {
			general.put(Constants.ALT_LABEL_LG1, JSONUtils.jsonArrayOfStringToString(general.getJSONArray(Constants.ALT_LABEL_LG1)));
		} else {
			general.remove(Constants.ALT_LABEL_LG1);
		}
		if (general.has(Constants.ALT_LABEL_LG2)) {
			general.put(Constants.ALT_LABEL_LG2, JSONUtils.jsonArrayOfStringToString(general.getJSONArray(Constants.ALT_LABEL_LG2)));
		} else {
			general.remove(Constants.ALT_LABEL_LG2);
		}
		data.put(Constants.PREF_LABEL_LG1, general.getString(Constants.PREF_LABEL_LG1));
		if (general.has(Constants.PREF_LABEL_LG2)) {
			data.put(Constants.PREF_LABEL_LG2, general.getString(Constants.PREF_LABEL_LG2));
		}
		data.put("general", editGeneral(general, "concepts"));
		JSONArray links = repoGestion.getResponseAsArray(ConceptsQueries.conceptLinks(id));
		data.put("linksLg1", editLinks(links, 1));
		data.put("linksLg2", editLinks(links, 2));
		JSONObject notes = repoGestion.getResponseAsObject(
				ConceptsQueries.conceptNotesQuery(id, Integer.parseInt(general.getString(CONCEPT_VERSION))));
		editNotes(notes, data);
		return data;
	}

	public JSONObject getCollectionData(String id)  throws RmesException{
		JSONObject data = new JSONObject();
		JSONObject json = repoGestion.getResponseAsObject(CollectionsQueries.collectionQuery(id));
		data.put(Constants.PREF_LABEL_LG1, json.getString(Constants.PREF_LABEL_LG1));
		if (json.has(Constants.PREF_LABEL_LG2)) {
			data.put(Constants.PREF_LABEL_LG2, json.getString(Constants.PREF_LABEL_LG2));
		}
		data.put("general", editGeneral(json, "collections"));
		if (json.has(Constants.DESCRIPTION_LG1)) {
			data.put(Constants.DESCRIPTION_LG1, json.getString(Constants.DESCRIPTION_LG1) + XhtmlTags.PARAGRAPH);
		}
		if (json.has(Constants.DESCRIPTION_LG2)) {
			data.put(Constants.DESCRIPTION_LG2, json.getString(Constants.DESCRIPTION_LG2) + XhtmlTags.PARAGRAPH);
		}
		JSONArray members = repoGestion.getResponseAsArray(CollectionsQueries.collectionMembersQuery(id));
		String membersLg1 = extractMembers(members, Constants.PREF_LABEL_LG1);
		if (!membersLg1.equals("")) {
			data.put("membersLg1", membersLg1);
			data.put("membersLg2", extractMembers(members, Constants.PREF_LABEL_LG2));
		}
		return data;
	}

	private String editGeneral(JSONObject json, String context) {
		StringBuilder xhtml = new StringBuilder(XhtmlTags.OPENLIST);
		if (json.has(Constants.ALT_LABEL_LG1)) {
			xhtml.append(XhtmlTags.inListItem("Libellé alternatif (" + Config.LG1 + ") : " + json.getString(Constants.ALT_LABEL_LG1)));
		}
		if (json.has(Constants.ALT_LABEL_LG2)) {
			xhtml.append(XhtmlTags.inListItem("Libellé alternatif (" + Config.LG2 + ") : " + json.getString(Constants.ALT_LABEL_LG2) ));
		}
		if (json.has("created")) {
			xhtml.append(XhtmlTags.inListItem("Date de création : " + toDate(json.getString("created")) ));
		}
		if (json.has("modified")) {
			xhtml.append(XhtmlTags.inListItem("Date de modification : " + toDate(json.getString("modified")) ));
		}
		if (json.has("valid")) {
			xhtml.append(XhtmlTags.inListItem("Date de fin de validité : " + toDate(json.getString("valid")) ));
		}
		if (json.has("disseminationStatus")) {
			xhtml.append(XhtmlTags.inListItem("Statut de diffusion : " + toLabel(json.getString("disseminationStatus")) ));
		}
		if (json.has("additionalMaterial")) {
			xhtml.append(XhtmlTags.inListItem("Document lié : " + json.getString("additionalMaterial") ));
		}
		if (json.has("creator")) {
			xhtml.append(XhtmlTags.inListItem("Timbre propriétaire : " + json.getString("creator") ));
		}
		if (json.has("contributor")) {
			xhtml.append(XhtmlTags.inListItem("Timbre gestionnaire : " + json.getString("contributor") ));
		}
		if (json.has("isValidated")) {
			xhtml.append(XhtmlTags.inListItem("Statut de validation : " + toValidationStatus(json.getString("isValidated"), context) ));
		}
		if (json.has(CONCEPT_VERSION)) {
			xhtml.append(XhtmlTags.inListItem("Version : " + json.getString(CONCEPT_VERSION) ));
		}

		xhtml.append(XhtmlTags.CLOSELIST.concat(XhtmlTags.PARAGRAPH));
		return xhtml.toString();
	}

	private String extractMembers(JSONArray array, String attr) {
		TreeSet<String> list = new TreeSet<>(new StringComparator());
		for (int i = 0; i < array.length(); i++) {
			JSONObject jsonO = (JSONObject) array.get(i);
			if (jsonO.has(attr)) {
				list.add(jsonO.getString(attr));
			}
		}
		if (list.isEmpty()) {
			return "";
		}
		StringBuilder xhtml = new StringBuilder(XhtmlTags.OPENLIST);
		for (String member : list) {
			xhtml.append(XhtmlTags.inListItem(member));
		}
		xhtml.append(XhtmlTags.CLOSELIST.concat(XhtmlTags.PARAGRAPH));
		return xhtml.toString();
	}

	/**
	 * 
	 * @param array
	 * @param language
	 * @return
	 */
	private String editLinks(JSONArray array, int language) {
		TreeSet<String> listParents = new TreeSet<>(new StringComparator());
		TreeSet<String> listEnfants = new TreeSet<>(new StringComparator());
		TreeSet<String> listReferences = new TreeSet<>(new StringComparator());
		TreeSet<String> listSucceed = new TreeSet<>(new StringComparator());
		TreeSet<String> listReplaces = new TreeSet<>(new StringComparator());
		for (int i = 0; i < array.length(); i++) {
			JSONObject jsonO = (JSONObject) array.get(i);
			String typeOfLink = jsonO.getString("typeOfLink");
			if (typeOfLink.equals("narrower")) {
				listParents.add(jsonO.getString(Constants.PREF_LABEL_LG + language));
			}
			if (typeOfLink.equals("broader")) {
				listEnfants.add(jsonO.getString(Constants.PREF_LABEL_LG + language));
			}
			if (typeOfLink.equals("references")) {
				listReferences.add(jsonO.getString(Constants.PREF_LABEL_LG + language));
			}
			if (typeOfLink.equals("succeed")) {
				listSucceed.add(jsonO.getString(Constants.PREF_LABEL_LG + language));
			}
			if (typeOfLink.equals("related")) {
				listReplaces.add(jsonO.getString(Constants.PREF_LABEL_LG + language));
			}
		}
		if (listParents.isEmpty() && listEnfants.isEmpty() && listReferences.isEmpty() && listSucceed.isEmpty()
				&& listReplaces.isEmpty()) {
			return "";
		}
		StringBuilder xhtml = new StringBuilder("");
		switch (language) {
			case 1:
				linksByType(xhtml, listParents, "Concept parent");
				linksByType(xhtml, listEnfants, "Concept enfant");
				linksByType(xhtml, listReferences, "Concept référencé");
				linksByType(xhtml, listSucceed, "Succède à");
				linksByType(xhtml, listReplaces, "Concept lié");
				break;
			case 2:
				linksByType(xhtml, listParents, "Parent concept");
				linksByType(xhtml, listEnfants, "Child concept");
				linksByType(xhtml, listReferences, "Referenced concept");
				linksByType(xhtml, listSucceed, "Succeeds");
				linksByType(xhtml, listReplaces, "Linked concept");
				break;
			default:
				break;
		}

		xhtml.append(XhtmlTags.PARAGRAPH);
		return xhtml.toString();
	}

	private StringBuilder linksByType(StringBuilder xhtml, TreeSet<String> list, String title) {
		if (list.isEmpty()) {
			return xhtml;
		}
		xhtml.append(XhtmlTags.inUpperCase(title +" :"));
		xhtml.append(XhtmlTags.OPENLIST);
		for (String item : list) {
			xhtml.append(XhtmlTags.inListItem(item));
		}
		xhtml.append(XhtmlTags.CLOSELIST.concat(XhtmlTags.PARAGRAPH));
		return xhtml;
	}

	private void editNotes(JSONObject notes, JSONObject data) {
		List<String> noteTypes = Arrays.asList("scopeNoteLg1", "scopeNoteLg2", "definitionLg1", "definitionLg2",
				"editorialNoteLg1", "editorialNoteLg2");
		noteTypes.forEach(noteType -> {
			if (notes.has(noteType)) {
				data.put(noteType, notes.getString(noteType) + XhtmlTags.PARAGRAPH);
			}
		});
	}

	private String toLabel(String dsURL) {
		return DisseminationStatus.getEnumLabel(dsURL);
	}

	private String toDate(String dateTime) {
		return dateTime.substring(8, 10) + "/" + dateTime.substring(5, 7) + "/" + dateTime.substring(0, 4);
	}
	
	private String toValidationStatus(String boolStatus, String context) {
		if (boolStatus.equals("true")) {
			if (context.equals("concepts")) {
				return "Validé";
			} else {
				return "Validée";
			}
		} else {
			return "Provisoire";
		}
	}

}
