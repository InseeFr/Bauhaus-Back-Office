package fr.insee.rmes.bauhaus_services.concepts.concepts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeSet;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.external_services.export.ExportUtils;
import fr.insee.rmes.model.concepts.ConceptForExport;
import fr.insee.rmes.model.dissemination_status.DisseminationStatus;
import fr.insee.rmes.persistance.sparql_queries.concepts.CollectionsQueries;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptsQueries;
import fr.insee.rmes.utils.JSONUtils;
import fr.insee.rmes.utils.StringComparator;
import fr.insee.rmes.utils.XhtmlTags;
import fr.insee.rmes.utils.XsltUtils;

@Component
public class ConceptsExportBuilder extends RdfService {

	private static final Logger logger = LoggerFactory.getLogger(ConceptsExportBuilder.class);

	private static final String CONCEPT_VERSION = "conceptVersion";
	@Autowired
	ConceptsUtils conceptsUtils;

	public void transformAltLabelListInString(JSONObject general) {
		if (general.has(Constants.ALT_LABEL_LG1)) {
			general.put(Constants.ALT_LABEL_LG1,
					JSONUtils.jsonArrayOfStringToString(general.getJSONArray(Constants.ALT_LABEL_LG1)));
		} else {
			general.remove(Constants.ALT_LABEL_LG1);
		}
		if (general.has(Constants.ALT_LABEL_LG2)) {
			general.put(Constants.ALT_LABEL_LG2,
					JSONUtils.jsonArrayOfStringToString(general.getJSONArray(Constants.ALT_LABEL_LG2)));
		} else {
			general.remove(Constants.ALT_LABEL_LG2);
		}
	}

	public ConceptForExport getConceptData(String id) throws RmesException {

		ConceptForExport concept = null;

		JSONObject general = conceptsUtils.getConceptById(id);
		transformAltLabelListInString(general);

		JSONArray links = repoGestion.getResponseAsArray(ConceptsQueries.conceptLinks(id));
		JSONObject notes = repoGestion.getResponseAsObject(
				ConceptsQueries.conceptNotesQuery(id, Integer.parseInt(general.getString(CONCEPT_VERSION))));

		// Deserialization in the `ConceptForExport` class
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		try {
			concept = mapper.readValue(general.toString(), ConceptForExport.class);
			concept.addLinks(links);
			concept.addNotes(notes);
		} catch (JsonProcessingException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e.getClass().getSimpleName());
		}
		return concept;

	}

	public JSONObject getCollectionData(String id) throws RmesException {
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
			xhtml.append(XhtmlTags.inListItem(
					"Libellé alternatif (" + Config.LG1 + ") : " + json.getString(Constants.ALT_LABEL_LG1)));
		}
		if (json.has(Constants.ALT_LABEL_LG2)) {
			xhtml.append(XhtmlTags.inListItem(
					"Libellé alternatif (" + Config.LG2 + ") : " + json.getString(Constants.ALT_LABEL_LG2)));
		}
		if (json.has("created")) {
			xhtml.append(XhtmlTags.inListItem("Date de création : " + toDate(json.getString("created"))));
		}
		if (json.has("modified")) {
			xhtml.append(XhtmlTags.inListItem("Date de modification : " + toDate(json.getString("modified"))));
		}
		if (json.has("valid")) {
			xhtml.append(XhtmlTags.inListItem("Date de fin de validité : " + toDate(json.getString("valid"))));
		}
		if (json.has("disseminationStatus")) {
			xhtml.append(
					XhtmlTags.inListItem("Statut de diffusion : " + toLabel(json.getString("disseminationStatus"))));
		}
		if (json.has("additionalMaterial")) {
			xhtml.append(XhtmlTags.inListItem("Document lié : " + json.getString("additionalMaterial")));
		}
		if (json.has("creator")) {
			xhtml.append(XhtmlTags.inListItem("Timbre propriétaire : " + json.getString("creator")));
		}
		if (json.has("contributor")) {
			xhtml.append(XhtmlTags.inListItem("Timbre gestionnaire : " + json.getString("contributor")));
		}
		if (json.has("isValidated")) {
			xhtml.append(XhtmlTags.inListItem(
					"Statut de validation : " + toValidationStatus(json.getString("isValidated"), context)));
		}
		if (json.has(CONCEPT_VERSION)) {
			xhtml.append(XhtmlTags.inListItem("Version : " + json.getString(CONCEPT_VERSION)));
		}
		xhtml.append(XhtmlTags.CLOSELIST);
		xhtml.append(XhtmlTags.PARAGRAPH);

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
		xhtml.append(XhtmlTags.CLOSELIST);
		xhtml.append(XhtmlTags.PARAGRAPH);

		return xhtml.toString();
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

	public Response exportAsResponse(Map<String, String> xmlContent, boolean includeEmptyFields, boolean lg1,
			boolean lg2) throws RmesException {
		logger.debug("Begin To export concept");
		String fileName = "export.odt";
		ContentDisposition content = ContentDisposition.type("attachment").fileName(fileName).build();

		InputStream input = exportAsInputStream(xmlContent, includeEmptyFields, lg1, lg2);

		return Response.ok((StreamingOutput) out -> {
			IOUtils.copy(input, out);
			out.flush();
			input.close();
			out.close();
		}).header("Content-Disposition", content).build();

	}

	public InputStream exportAsInputStream(Map<String, String> xmlContent, boolean includeEmptyFields, boolean lg1,
			boolean lg2) throws RmesException {
		logger.debug("Begin To export concept");

		File output = null;
		String fileName = "export.odt";
		InputStream odtFileIS = null;
		InputStream xslFileIS = null;
		InputStream zipToCompleteIS = null;

		try {
			xslFileIS = getClass().getResourceAsStream("/xslTransformerFiles/rmes2odt.xsl");
			odtFileIS = getClass().getResourceAsStream("/xslTransformerFiles/concept/conceptPatternContent.xml");
			zipToCompleteIS = getClass().getResourceAsStream("/xslTransformerFiles/concept/toZipForConcept.zip");

			// prepare output
			output = File.createTempFile(Constants.OUTPUT, ExportUtils.getExtension(Constants.XML));
			output.deleteOnExit();
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
		}

		try (OutputStream osOutputFile = FileUtils.openOutputStream(output);
				PrintStream printStream = new PrintStream(osOutputFile);) {

			Path tempDir = Files.createTempDirectory("forExport");
			Path finalPath = Paths.get(tempDir.toString() + "/" + fileName);

			// Add two params to xmlContents
			String parametersXML = XsltUtils.buildParams(lg1, lg2, includeEmptyFields, Constants.CONCEPT);
			xmlContent.put("parametersFile", parametersXML);

			// transform
			XsltUtils.xsltTransform(xmlContent, odtFileIS, xslFileIS, printStream, tempDir);

			// create odt
			XsltUtils.createOdtFromXml(output, finalPath, zipToCompleteIS, tempDir);

			logger.debug("End To export concept");

			return Files.newInputStream(finalPath);
		} catch (IOException | TransformerException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e.getClass().getSimpleName());
		} finally {
			try {
				if (odtFileIS != null)
					odtFileIS.close();
				if (xslFileIS != null)
					xslFileIS.close();
			} catch (IOException ioe) {
				logger.error(ioe.getMessage());
			}
		}
	}

}
