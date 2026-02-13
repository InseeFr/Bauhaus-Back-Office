package fr.insee.rmes.bauhaus_services.operations.documentations;

import fr.insee.rmes.Config;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.GeographyService;
import fr.insee.rmes.bauhaus_services.code_list.LangService;
import fr.insee.rmes.bauhaus_services.operations.documentations.documents.DocumentsUtils;
import fr.insee.rmes.bauhaus_services.organizations.OrganizationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.UriUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.graphdb.ontologies.DCMITYPE;
import fr.insee.rmes.graphdb.ontologies.SDMX_MM;
import fr.insee.rmes.model.operations.documentations.DocumentationRubric;
import fr.insee.rmes.model.operations.documentations.RangeType;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentationsRubricsUtilsTest {

	@Mock
	private MetadataStructureDefUtils msdUtils;

	@Mock
	private DocumentsUtils docUtils;

	@Mock
	private OrganizationUtils organizationUtils;

	@Mock
	private CodeListService codeListService;

	@Mock
	private LangService langService;

	@Mock
	private GeographyService geoService;

	@Mock
	private Config config;

	@InjectMocks
	private DocumentationsRubricsUtils documentationsRubricsUtils;

	private SimpleValueFactory factory;
	private Model model;
	private Resource graph;

	@BeforeEach
	void setUp() {
		factory = SimpleValueFactory.getInstance();
		model = new LinkedHashModel();
		graph = factory.createIRI("http://test.insee.fr/graphes/documentations/test");

		// Initialize RdfUtils static dependencies (using lenient to avoid unnecessary stubbing warnings)
		lenient().when(config.getBaseGraph()).thenReturn("http://rdf.insee.fr/graphes");
		lenient().when(config.getConceptsGraph()).thenReturn("http://rdf.insee.fr/graphes/concepts");
		lenient().when(config.getDocumentsGraph()).thenReturn("http://rdf.insee.fr/graphes/documents");
		lenient().when(config.getDocumentationsGraph()).thenReturn("http://rdf.insee.fr/graphes/documentations");
		lenient().when(config.getBaseUriGestion()).thenReturn("http://rdf.insee.fr/graphes");

		RdfUtils.setConfig(config);
		RdfUtils.setUriUtils(new UriUtils(
			"http://id.insee.fr/",
			"http://rdf.insee.fr/graphes/",
			p -> Optional.of("/rapport")
		));
	}

	/**
	 * Helper method to setup basic mocks for attribute URI and language configuration.
	 * Format: predicate URI uses "/simsv2fr/attribut/" which will be replaced by "/attribut/{simsId}/" by getAttributeUri.
	 * Language configuration uses lenient() to avoid unnecessary stubbing warnings when not all tests need them.
	 */
	private void setupBasicMocks(String attributeId, String attributeName) throws RmesException {
		Map<String, String> attributesUriList = new HashMap<>();
		String predicateUri = "http://rdf.insee.fr/def/base/simsv2fr/attribut/" + attributeName;
		attributesUriList.put(attributeId, predicateUri);

		when(msdUtils.getMetadataAttributesUri()).thenReturn(attributesUriList);
		lenient().when(config.getLg1()).thenReturn("fr");
		lenient().when(config.getLg2()).thenReturn("en");
	}

	/**
	 * Helper method to setup language service mocks for tests requiring language URIs.
	 */
	private LanguageUris setupLanguageMocks() throws RmesException {
		String lang1Uri = "http://id.insee.fr/codes/langue/fr";
		String lang2Uri = "http://id.insee.fr/codes/langue/en";

		when(langService.getLanguage1()).thenReturn(lang1Uri);
		when(langService.getLanguage2()).thenReturn(lang2Uri);

		return new LanguageUris(lang1Uri, lang2Uri);
	}

	/**
	 * Record to hold language URIs returned by setupLanguageMocks.
	 */
	private record LanguageUris(String lang1Uri, String lang2Uri) {}

	@Test
	void shouldAddRichTextRubricToModelWithSimpleAddTripleString() throws RmesException {
		// Given
		String simsId = "1000";
		String attributeId = "RESUME_LG1";
		String labelLg1 = "Ceci est un test";
		String labelLg2 = "This is a test";

		DocumentationRubric rubric = new DocumentationRubric();
		rubric.setIdAttribute(attributeId);
		rubric.setRangeType(RangeType.RICHTEXT.getJsonType());
		rubric.setLabelLg1(labelLg1);
		rubric.setLabelLg2(labelLg2);

		setupBasicMocks(attributeId, "resume");
		LanguageUris languageUris = setupLanguageMocks();

		// When
		documentationsRubricsUtils.addRubricsToModel(model, simsId, graph, List.of(rubric));

		// Then
		assertFalse(model.isEmpty(), "Le modèle ne devrait pas être vide");

		// Vérifie que le textUri pour lg1 a été créé (utilise "texte" selon la constante TEXT_LG1)
		IRI textUriLg1 = factory.createIRI("http://rdf.insee.fr/def/base/attribut/" + simsId + "/resume/texte");

		assertTrue(model.contains(textUriLg1, RDF.TYPE, DCMITYPE.TEXT),
				"Le modèle devrait contenir le type TEXT pour texte (lg1)");
		assertTrue(model.contains(textUriLg1, DCTERMS.LANGUAGE, factory.createIRI(languageUris.lang1Uri())),
				"Le modèle devrait contenir la langue fr pour texte (lg1)");

		// Vérifie que le textUri pour lg2 a été créé (utilise "text" selon la constante TEXT_LG2)
		IRI textUriLg2 = factory.createIRI("http://rdf.insee.fr/def/base/attribut/" + simsId + "/resume/text");
		assertTrue(model.contains(textUriLg2, RDF.TYPE, DCMITYPE.TEXT),
				"Le modèle devrait contenir le type TEXT pour text (lg2)");
		assertTrue(model.contains(textUriLg2, DCTERMS.LANGUAGE, factory.createIRI(languageUris.lang2Uri())),
				"Le modèle devrait contenir la langue en pour text (lg2)");

		// Vérifie que les valeurs ont été ajoutées avec addTripleString (et non addTripleStringMdToXhtml)
		// Les valeurs devraient être présentes dans le modèle
		boolean hasValueLg1 = model.stream()
				.anyMatch(stmt -> stmt.getSubject().equals(textUriLg1) &&
						stmt.getPredicate().equals(RDF.VALUE));
		boolean hasValueLg2 = model.stream()
				.anyMatch(stmt -> stmt.getSubject().equals(textUriLg2) &&
						stmt.getPredicate().equals(RDF.VALUE));

		assertTrue(hasValueLg1, "Le modèle devrait contenir la valeur pour texte (lg1)");
		assertTrue(hasValueLg2, "Le modèle devrait contenir la valeur pour text (lg2)");
	}

	@Test
	void shouldAddSimpleTextRubricToModel() throws RmesException {
		// Given
		String simsId = "1000";
		String attributeId = "TITLE_LG1";
		String labelLg1 = "Titre en français";
		String labelLg2 = "Title in English";

		DocumentationRubric rubric = new DocumentationRubric();
		rubric.setIdAttribute(attributeId);
		rubric.setRangeType(RangeType.STRING.getJsonType());
		rubric.setLabelLg1(labelLg1);
		rubric.setLabelLg2(labelLg2);

		setupBasicMocks(attributeId, "title");

		// When
		documentationsRubricsUtils.addRubricsToModel(model, simsId, graph, List.of(rubric));

		// Then
		assertFalse(model.isEmpty(), "Le modèle ne devrait pas être vide");

		// Vérifie que l'attribut a été défini comme REPORTED_ATTRIBUTE
		IRI attributeUri = factory.createIRI("http://rdf.insee.fr/def/base/attribut/" + simsId + "/title");
		assertTrue(model.contains(attributeUri, RDF.TYPE, SDMX_MM.REPORTED_ATTRIBUTE),
				"Le modèle devrait contenir le type REPORTED_ATTRIBUTE");

		// Vérifie que les valeurs textuelles ont été ajoutées
		// Le predicateUri réel après transformation par getAttributeUri
		IRI realPredicateUri = factory.createIRI("http://rdf.insee.fr/def/base/attribut/" + simsId + "/title");
		boolean hasTextValue = model.stream()
				.anyMatch(stmt -> stmt.getSubject().equals(attributeUri));

		assertTrue(hasTextValue, "Le modèle devrait contenir les valeurs textuelles");
	}

	@Test
	void shouldNotAddRichTextWhenRubricIsEmpty() throws RmesException {
		// Given
		String simsId = "1000";
		String attributeId = "EMPTY_RUBRIC";

		DocumentationRubric rubric = new DocumentationRubric();
		rubric.setIdAttribute(attributeId);
		rubric.setRangeType(RangeType.RICHTEXT.getJsonType());
		// Pas de labelLg1 ni labelLg2

		setupBasicMocks(attributeId, "emptyRubric");

		// When
		documentationsRubricsUtils.addRubricsToModel(model, simsId, graph, List.of(rubric));

		// Then
		// Le modèle devrait contenir uniquement la relation avec le metadata report,
		// mais pas de contenu rich text
		IRI attributeUri = factory.createIRI("http://rdf.insee.fr/def/base/attribut/" + simsId + "/emptyRubric");
		IRI simsUri = RdfUtils.objectIRI(ObjectType.DOCUMENTATION, simsId);

		assertTrue(model.contains(attributeUri, SDMX_MM.METADATA_REPORT_PREDICATE, simsUri),
				"Le modèle devrait contenir le lien vers le metadata report");

		// Vérifie qu'aucun TEXT n'a été créé
		long textCount = model.stream()
				.filter(stmt -> stmt.getPredicate().equals(RDF.TYPE) &&
						stmt.getObject().equals(DCMITYPE.TEXT))
				.count();

		assertEquals(0, textCount, "Aucun élément TEXT ne devrait être créé pour une rubrique vide");
	}

	@Test
	void shouldAddDateRubricToModel() throws RmesException {
		// Given
		String simsId = "1000";
		String attributeId = "DATE_ATTRIBUTE";

		DocumentationRubric rubric = new DocumentationRubric();
		rubric.setIdAttribute(attributeId);
		rubric.setRangeType(RangeType.DATE.getJsonType());
		rubric.setSingleValue("2024-01-15");

		setupBasicMocks(attributeId, "dateAttribute");

		// When
		documentationsRubricsUtils.addRubricsToModel(model, simsId, graph, List.of(rubric));

		// Then
		assertFalse(model.isEmpty(), "Le modèle ne devrait pas être vide");

		// Vérifie que la date a été ajoutée
		boolean hasDateValue = model.stream()
				.anyMatch(stmt -> stmt.getPredicate().toString().contains("dateAttribute"));

		assertTrue(hasDateValue, "Le modèle devrait contenir la valeur de date");
	}

	@Test
	void shouldAddCodeListRubricToModel() throws RmesException {
		// Given
		String simsId = "1000";
		String attributeId = "CODELIST_ATTRIBUTE";
		String codeListId = "CL_FREQ";
		String code = "M";
		String codeUri = "http://id.insee.fr/codes/freq/M";

		DocumentationRubric rubric = new DocumentationRubric();
		rubric.setIdAttribute(attributeId);
		rubric.setRangeType(RangeType.CODELIST.getJsonType());
		rubric.setCodeList(codeListId);
		rubric.setValue(List.of(code));

		setupBasicMocks(attributeId, "codelistAttribute");
		when(codeListService.getCodeUri(codeListId, code)).thenReturn(codeUri);

		// When
		documentationsRubricsUtils.addRubricsToModel(model, simsId, graph, List.of(rubric));

		// Then
		assertFalse(model.isEmpty(), "Le modèle ne devrait pas être vide");

		// Vérifie que le code a été ajouté
		boolean hasCodeUri = model.stream()
				.anyMatch(stmt -> stmt.getObject().stringValue().equals(codeUri));

		assertTrue(hasCodeUri, "Le modèle devrait contenir l'URI du code");
	}

	@Test
	void shouldAddOrganizationRubricToModel() throws RmesException {
		// Given
		String simsId = "1000";
		String attributeId = "ORG_ATTRIBUTE";
		String orgaId = "DG75-F001";
		String orgaUri = "http://rdf.insee.fr/def/base/organisations/insee/DG75-F001";

		DocumentationRubric rubric = new DocumentationRubric();
		rubric.setIdAttribute(attributeId);
		rubric.setRangeType(RangeType.ORGANIZATION.getJsonType());
		rubric.setSingleValue(orgaId);

		setupBasicMocks(attributeId, "orgAttribute");
		when(organizationUtils.getUri(orgaId)).thenReturn(orgaUri);

		// When
		documentationsRubricsUtils.addRubricsToModel(model, simsId, graph, List.of(rubric));

		// Then
		assertFalse(model.isEmpty(), "Le modèle ne devrait pas être vide");

		// Vérifie que l'organisation a été ajoutée
		boolean hasOrgaUri = model.stream()
				.anyMatch(stmt -> stmt.getObject().stringValue().equals(orgaUri));

		assertTrue(hasOrgaUri, "Le modèle devrait contenir l'URI de l'organisation");
	}

	@Test
	void shouldAddGeographyRubricToModel() throws RmesException {
		// Given
		String simsId = "1000";
		String attributeId = "GEO_ATTRIBUTE";
		String geoUri = "http://id.insee.fr/geo/territoire/01";

		DocumentationRubric rubric = new DocumentationRubric();
		rubric.setIdAttribute(attributeId);
		rubric.setRangeType(RangeType.GEOGRAPHY.getJsonType());
		rubric.setUri(geoUri);

		setupBasicMocks(attributeId, "geoAttribute");

		// When
		documentationsRubricsUtils.addRubricsToModel(model, simsId, graph, List.of(rubric));

		// Then
		assertFalse(model.isEmpty(), "Le modèle ne devrait pas être vide");

		// Vérifie que la géographie a été ajoutée
		boolean hasGeoUri = model.stream()
				.anyMatch(stmt -> stmt.getObject().stringValue().equals(geoUri));

		assertTrue(hasGeoUri, "Le modèle devrait contenir l'URI de la géographie");
	}

	@Test
	void shouldBuildRubricFromJsonWithRichTextAndMarkdownConversion() {
		// Given
		JSONObject jsonRubric = new JSONObject();
		jsonRubric.put(Constants.ID_ATTRIBUTE, "RESUME_LG1");
		jsonRubric.put(Constants.RANGE_TYPE, RangeType.RICHTEXT.getJsonType());
		jsonRubric.put(Constants.LABEL_LG1, "# Titre\n\nCeci est un **test**");
		jsonRubric.put(Constants.LABEL_LG2, "# Title\n\nThis is a **test**");

		// When - forXml = true devrait convertir markdown vers XHTML et échapper le XML
		DocumentationRubric result = documentationsRubricsUtils.buildRubricFromJson(jsonRubric, true);

		// Then
		assertNotNull(result);
		assertEquals("RESUME_LG1", result.getIdAttribute());
		// La conversion markdown vers XHTML transforme # en <h1>, puis escape XML utilise des marqueurs de remplacement
		// Les marqueurs "replacementForInf" et "replacementForSup" sont utilisés temporairement
		assertTrue(result.getLabelLg1().contains("replacementForInf") && result.getLabelLg1().contains("h1"),
				"Le label devrait contenir du HTML échappé avec des marqueurs de remplacement: " + result.getLabelLg1());
		assertTrue(result.getLabelLg2().contains("replacementForInf") && result.getLabelLg2().contains("h1"),
				"Le label devrait contenir du HTML échappé avec des marqueurs de remplacement: " + result.getLabelLg2());
	}

	@Test
	void shouldBuildRubricFromJsonWithoutMarkdownConversion() {
		// Given
		JSONObject jsonRubric = new JSONObject();
		jsonRubric.put(Constants.ID_ATTRIBUTE, "RESUME_LG1");
		jsonRubric.put(Constants.RANGE_TYPE, RangeType.RICHTEXT.getJsonType());
		jsonRubric.put(Constants.LABEL_LG1, "# Titre");
		jsonRubric.put(Constants.LABEL_LG2, "# Title");

		// When - forXml = false ne devrait pas convertir
		DocumentationRubric result = documentationsRubricsUtils.buildRubricFromJson(jsonRubric, false);

		// Then
		assertNotNull(result);
		assertEquals("RESUME_LG1", result.getIdAttribute());
		assertEquals("# Titre", result.getLabelLg1());
		assertEquals("# Title", result.getLabelLg2());
	}

	@Test
	void shouldBuildRubricFromJsonWithSimpleText() {
		// Given
		JSONObject jsonRubric = new JSONObject();
		jsonRubric.put(Constants.ID_ATTRIBUTE, "TITLE");
		jsonRubric.put(Constants.RANGE_TYPE, RangeType.STRING.getJsonType());
		jsonRubric.put(Constants.LABEL_LG1, "Titre simple");
		jsonRubric.put(Constants.LABEL_LG2, "Simple title");

		// When - forXml = true ne devrait pas convertir les STRING en HTML
		DocumentationRubric result = documentationsRubricsUtils.buildRubricFromJson(jsonRubric, true);

		// Then
		assertNotNull(result);
		assertEquals("TITLE", result.getIdAttribute());
		// Pour STRING, les caractères XML sont échappés mais pas de conversion markdown
		assertTrue(result.getLabelLg1().contains("Titre") || result.getLabelLg1().equals("Titre simple"));
	}

	@Test
	void shouldBuildRubricFromJsonWithCodeListValue() {
		// Given
		JSONObject jsonRubric = new JSONObject();
		jsonRubric.put(Constants.ID_ATTRIBUTE, "FREQ");
		jsonRubric.put(Constants.RANGE_TYPE, RangeType.CODELIST.getJsonType());
		jsonRubric.put(Constants.VALUE, "M");
		jsonRubric.put(Constants.CODELIST, "CL_FREQ");

		// When
		DocumentationRubric result = documentationsRubricsUtils.buildRubricFromJson(jsonRubric, false);

		// Then
		assertNotNull(result);
		assertEquals("FREQ", result.getIdAttribute());
		assertEquals("CL_FREQ", result.getCodeList());
		assertEquals("M", result.getSimpleValue());
	}

	@Test
	void shouldBuildRubricFromJsonWithMultipleCodeListValues() {
		// Given
		JSONObject jsonRubric = new JSONObject();
		jsonRubric.put(Constants.ID_ATTRIBUTE, "FREQ");
		jsonRubric.put(Constants.RANGE_TYPE, RangeType.CODELIST.getJsonType());
		JSONArray values = new JSONArray();
		values.put("M");
		values.put("A");
		jsonRubric.put(Constants.VALUE, values);
		jsonRubric.put(Constants.CODELIST, "CL_FREQ");

		// When
		DocumentationRubric result = documentationsRubricsUtils.buildRubricFromJson(jsonRubric, false);

		// Then
		assertNotNull(result);
		assertEquals("FREQ", result.getIdAttribute());
		assertEquals("CL_FREQ", result.getCodeList());
		assertEquals(2, result.getValue().size());
		assertTrue(result.getValue().contains("M"));
		assertTrue(result.getValue().contains("A"));
	}
}