package fr.insee.rmes.persistance.service.sesame.concepts.concepts;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.modele.dissemination_status.DisseminationStatus;
import fr.insee.rmes.persistance.service.Constants;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.sparqlQueries.concepts.CollectionsQueries;
import fr.insee.rmes.persistance.sparqlQueries.concepts.ConceptsQueries;
import fr.insee.rmes.utils.JSONUtils;
import fr.insee.rmes.utils.StringComparator;

@Component
public class ConceptsExportBuilder {
	

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
		data.put("prefLabelLg1", general.getString("prefLabelLg1"));
		if (general.has("prefLabelLg2")) {
			data.put("prefLabelLg2", general.getString("prefLabelLg2"));
		}
		data.put("general", editGeneral(general, "concepts"));
		JSONArray links = RepositoryGestion.getResponseAsArray(ConceptsQueries.conceptLinks(id));
		data.put("linksLg1", editLinks(links, 1));
		data.put("linksLg2", editLinks(links, 2));
		JSONObject notes = RepositoryGestion.getResponseAsObject(
				ConceptsQueries.conceptNotesQuery(id, Integer.parseInt(general.getString("conceptVersion"))));
		editNotes(notes, data);
		return data;
	}

	public JSONObject getCollectionData(String id)  throws RmesException{
		JSONObject data = new JSONObject();
		JSONObject json = RepositoryGestion.getResponseAsObject(CollectionsQueries.collectionQuery(id));
		data.put("prefLabelLg1", json.getString("prefLabelLg1"));
		if (json.has("prefLabelLg2")) {
			data.put("prefLabelLg2", json.getString("prefLabelLg2"));
		}
		data.put("general", editGeneral(json, "collections"));
		if (json.has("descriptionLg1")) {
			data.put("descriptionLg1", json.getString("descriptionLg1") + "<p></p>");
		}
		if (json.has("descriptionLg2")) {
			data.put("descriptionLg2", json.getString("descriptionLg2") + "<p></p>");
		}
		JSONArray members = RepositoryGestion.getResponseAsArray(CollectionsQueries.collectionMembersQuery(id));
		String membersLg1 = extractMembers(members, "prefLabelLg1");
		if (!membersLg1.equals("")) {
			data.put("membersLg1", membersLg1);
			data.put("membersLg2", extractMembers(members, "prefLabelLg2"));
		}
		return data;
	}

	private String editGeneral(JSONObject json, String context) {
		StringBuilder xhtml = new StringBuilder("<ul>");
		if (json.has(Constants.ALT_LABEL_LG1)) {
			xhtml.append("<li>Libellé alternatif (" + Config.LG1 + ") : " + json.getString(Constants.ALT_LABEL_LG1) + "</li>");
		}
		if (json.has(Constants.ALT_LABEL_LG2)) {
			xhtml.append("<li>Libellé alternatif (" + Config.LG2 + ") : " + json.getString(Constants.ALT_LABEL_LG2) + "</li>");
		}
		if (json.has("created")) {
			xhtml.append("<li>Date de création : " + toDate(json.getString("created")) + "</li>");
		}
		if (json.has("modified")) {
			xhtml.append("<li>Date de modification : " + toDate(json.getString("modified")) + "</li>");
		}
		if (json.has("valid")) {
			xhtml.append("<li>Date de fin de validité : " + toDate(json.getString("valid")) + "</li>");
		}
		if (json.has("disseminationStatus")) {
			xhtml.append("<li>Statut de diffusion : " + toLabel(json.getString("disseminationStatus")) + "</li>");
		}
		if (json.has("additionalMaterial")) {
			xhtml.append("<li>Document lié : " + json.getString("additionalMaterial") + "</li>");
		}
		if (json.has("creator")) {
			xhtml.append("<li>Timbre propriétaire : " + json.getString("creator") + "</li>");
		}
		if (json.has("contributor")) {
			xhtml.append("<li>Timbre gestionnaire : " + json.getString("contributor") + "</li>");
		}
		if (json.has("isValidated")) {
			xhtml.append("<li>Statut de validation : " + toValidationStatus(json.getString("isValidated"), context) + "</li>");
		}
		if (json.has("conceptVersion")) {
			xhtml.append("<li>Version : " + json.getString("conceptVersion") + "</li>");
		}

		xhtml.append("</ul><p></p>");
		return xhtml.toString();
	}

	private String extractMembers(JSONArray array, String attr) {
		TreeSet<String> list = new TreeSet<String>(new StringComparator());
		for (int i = 0; i < array.length(); i++) {
			JSONObject jsonO = (JSONObject) array.get(i);
			if (jsonO.has(attr)) {
				list.add(jsonO.getString(attr));
			}
		}
		if (list.isEmpty()) {
			return "";
		}
		StringBuilder xhtml = new StringBuilder("<ul>");
		for (String member : list) {
			xhtml.append("<li>" + member + "</li>");
			;
		}
		xhtml.append("</ul><p></p>");
		return xhtml.toString();
	}

	/**
	 * 
	 * @param array
	 * @param language
	 * @return
	 */
	private String editLinks(JSONArray array, int language) {
		TreeSet<String> listParents = new TreeSet<String>(new StringComparator());
		TreeSet<String> listEnfants = new TreeSet<String>(new StringComparator());
		TreeSet<String> listReferences = new TreeSet<String>(new StringComparator());
		TreeSet<String> listSucceed = new TreeSet<String>(new StringComparator());
		TreeSet<String> listReplaces = new TreeSet<String>(new StringComparator());
		for (int i = 0; i < array.length(); i++) {
			JSONObject jsonO = (JSONObject) array.get(i);
			String typeOfLink = jsonO.getString("typeOfLink");
			if (typeOfLink.equals("narrower")) {
				listParents.add(jsonO.getString("prefLabelLg" + language));
			}
			if (typeOfLink.equals("broader")) {
				listEnfants.add(jsonO.getString("prefLabelLg" + language));
			}
			if (typeOfLink.equals("references")) {
				listReferences.add(jsonO.getString("prefLabelLg" + language));
			}
			if (typeOfLink.equals("succeed")) {
				listSucceed.add(jsonO.getString("prefLabelLg" + language));
			}
			if (typeOfLink.equals("related")) {
				listReplaces.add(jsonO.getString("prefLabelLg" + language));
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

		xhtml.append("<p></p>");
		return xhtml.toString();
	}

	private StringBuilder linksByType(StringBuilder xhtml, TreeSet<String> list, String title) {
		if (list.isEmpty()) {
			return xhtml;
		}
		xhtml.append("<U>" + title + " :</U>");
		xhtml.append("<ul>");
		for (String item : list) {
			xhtml.append("<li>" + item + "</li>");
			;
		}
		xhtml.append("</ul><p></p>");
		return xhtml;
	}

	private void editNotes(JSONObject notes, JSONObject data) {
		List<String> noteTypes = Arrays.asList("scopeNoteLg1", "scopeNoteLg2", "definitionLg1", "definitionLg2",
				"editorialNoteLg1", "editorialNoteLg2");
		noteTypes.forEach(noteType -> {
			if (notes.has(noteType)) {
				data.put(noteType, notes.getString(noteType) + "<p></p>");
			}
		});
	}

	private String toLabel(String dsURL) {
		return DisseminationStatus.getEnumLabel(dsURL);
	}

	private String toDate(String dateTime) {
		String dateString = dateTime.substring(8, 10) + "/" + dateTime.substring(5, 7) + "/" + dateTime.substring(0, 4);
		return dateString;
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
