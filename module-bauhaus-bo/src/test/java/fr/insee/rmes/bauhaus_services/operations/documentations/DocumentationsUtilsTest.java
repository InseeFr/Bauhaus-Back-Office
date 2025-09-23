package fr.insee.rmes.bauhaus_services.operations.documentations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.model.operations.documentations.DocumentationRubric;
import fr.insee.rmes.model.operations.documentations.MAS;
import fr.insee.rmes.model.operations.documentations.MSD;
import fr.insee.rmes.onion.infrastructure.graphdb.operations.queries.DocumentationQueries;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentationsUtilsTest {

	@Mock
	private RepositoryGestion repoGestion;

	@Mock
	private RepositoryPublication repositoryPublication;

	@Mock
	private Config config;

	@Mock
	private ParentUtils parentUtils;

	@Mock
	private DocumentationsRubricsUtils documentationsRubricsUtils;

	@Mock
	private DocumentationPublication documentationPublication;

	@InjectMocks
	private DocumentationsUtils documentationsUtils;


	@Test
	void shouldThrowRmesNotFoundExceptionWhenGetDocumentationByIdSims() throws RmesException {

		when(config.getLg1()).thenReturn("fr");
		when(config.getLg2()).thenReturn("en");
		when(config.getDocumentationsGraph()).thenReturn("http://rdf.insee.fr/graphes/documentations");
		when(config.getMsdGraph()).thenReturn("http://rdf.insee.fr/graphes/msd");
		when(config.getCodeListGraph()).thenReturn("http://rdf.insee.fr/graphes/codelists");
		when(config.getMsdConceptsGraph()).thenReturn("http://rdf.insee.fr/graphes/msd-concepts");

		// Configurer la variable statique dans GenericQueries
		GenericQueries.setConfig(config);

		String idSims ="2025";
		when(repoGestion.getResponseAsObject(DocumentationQueries.getDocumentationTitleQuery(idSims))).thenReturn(new JSONObject());
		RmesException exception = assertThrows(RmesNotFoundException.class, () -> documentationsUtils.getDocumentationByIdSims(idSims));
		assertTrue(exception.getDetails().contains("Documentation not found"));
	}

	@Test
	void shouldThrowRmesNotAcceptableExceptionWhenDeleteMetadataReport() throws RmesException {
		String id ="2025";
		String[] days = {"yesterday","today","tomorrow"};
		when(parentUtils.getDocumentationTargetTypeAndId(id)).thenReturn(days);
		RmesException exception = assertThrows(RmesNotAcceptableException.class, () -> documentationsUtils.deleteMetadataReport(id));
		assertTrue(exception.getDetails().contains("Only a sims that documents a series can be deleted"));
	}


	@Test
	void shouldThrowRmesNotFoundExceptionIfParentTargetIsUnpublished() throws RmesException {
		String[] target = {"series", ""};
		when(parentUtils.getDocumentationTargetTypeAndId("1")).thenReturn(target);
		RmesException exception = assertThrows(RmesNotFoundException.class, () -> documentationsUtils.publishMetadataReport("1"));
		assertTrue(exception.getDetails().contains("target not found for this Sims"));
	}

	@Test
	void shouldThrowRmesBadRequestExceptionIfParentTargetIsUnpublished() throws RmesException {
		String[] target = {"series", "seriesExample"};
		when(parentUtils.getDocumentationTargetTypeAndId("1")).thenReturn(target);
		when(parentUtils.getValidationStatus("seriesExample")).thenReturn(ValidationStatus.UNPUBLISHED.toString());
		RmesException exception = assertThrows(RmesBadRequestException.class, () -> documentationsUtils.publishMetadataReport("1"));
		assertTrue(exception.getDetails().contains("This metadataReport cannot be published before its target is published. "));
	}

	@Test
	void shouldBuildDocumentationFromJsonTest() throws RmesException{

		JSONObject letterA = new JSONObject().put("A","anExampleOfLetterA");
		JSONObject letterB = new JSONObject().put("B","anExampleOfLetterB");
		JSONObject letterC = new JSONObject().put("C","anExampleOfLetterC");
		JSONArray alphabet = new JSONArray().put(letterA).put(letterB).put(letterC);

		DocumentationRubric docA = new DocumentationRubric();
		DocumentationRubric docB = new DocumentationRubric();
		DocumentationRubric docC = new DocumentationRubric();
		docA.setIdAttribute("idAttributeA");
		docB.setIdAttribute("idAttributeB");
		docC.setIdAttribute("idAttributeC");

		String[] st = new String[] {Constants.OPERATION_UP, "s8888"};

		when(documentationsRubricsUtils.buildRubricFromJson(letterA,true)).thenReturn(docA);
		when(documentationsRubricsUtils.buildRubricFromJson(letterB,true)).thenReturn(docB);
		when(documentationsRubricsUtils.buildRubricFromJson(letterC,true)).thenReturn(docC);
		when(parentUtils.getDocumentationTargetTypeAndId(anyString())).thenReturn(st);

		JSONObject jsonSims = new JSONObject()
										.put("rubrics", alphabet)
										.put("idSeries", "")
										.put(Constants.ID, "9999")
										.put("idOperation", "s8888")
										.put(Constants.LABEL_LG1, "Rapport de métadonnées 9999")
										.put(Constants.LABEL_LG2, "Metadata report 9999");

		Documentation sims = documentationsUtils.buildDocumentationFromJson(jsonSims,true);

		boolean idAttributeAValue = "IDATTRIBUTEA".equals(sims.getRubrics().getFirst().getIdAttribute());
		boolean idAttributeBValue = "IDATTRIBUTEB".equals(sims.getRubrics().get(1).getIdAttribute());
		boolean idAttributeCValue = "IDATTRIBUTEC".equals(sims.getRubrics().getLast().getIdAttribute());

		assertTrue(idAttributeAValue && idAttributeBValue && idAttributeCValue);
	}

	@Test
	void shouldThrowARmesNotAcceptableExceptionWhenSetMetadataReport(){
		String id="idExample";
		String body="bodyExample";
		boolean create = false;
		RmesNotAcceptableException exception = assertThrows(RmesNotAcceptableException.class, () -> documentationsUtils.setMetadataReport(id,body,create));
		assertTrue(exception.getDetails().contains("{\"code\":861,\"details\":\"IOException: cannot parse input\""));
	}

	@Test
	void shouldBuildMSDRubricFromJson(){
		JSONObject jsonMsdRubric = new JSONObject().put("idMas","idMasValue").put("masLabelLg1","masLabelLg1Value").put("masLabelLg2","masLabelLg2Value").put("idParent","idParentValue").put("isPresentational",true);
		MAS mas =documentationsUtils.buildMSDRubricFromJson(jsonMsdRubric);
		boolean idMasIsCorrect = Objects.equals(mas.getIdMas(), jsonMsdRubric.getString("idMas"));
		boolean masLabelLg1IsCorrect = Objects.equals(mas.getMasLabelLg1(), jsonMsdRubric.getString("masLabelLg1"));
		boolean masLabelLg2IsCorrect = Objects.equals(mas.getMasLabelLg2(), jsonMsdRubric.getString("masLabelLg2"));
		boolean idParentIsCorrect = Objects.equals(mas.getIdParent(), jsonMsdRubric.getString("idParent"));
		boolean isPresentationalIsCorrect = mas.getIsPresentational()==jsonMsdRubric.getBoolean("isPresentational");
		assertTrue(idMasIsCorrect && masLabelLg1IsCorrect && masLabelLg2IsCorrect && idParentIsCorrect && isPresentationalIsCorrect);
	}

	@Test
	void shouldBuildMSDFromJson(){
		JSONObject jsonMsdRubricOne = new JSONObject().put("idMas","idMasValue").put("Bauhaus-Back-Office","online");
		JSONObject jsonMsdRubricTwo = new JSONObject().put("masLabelLg1","masLabelLg1Value").put("exceptionExample","RmesExceptionExample").put("idParent","idParentValue");
		JSONArray jsonArray = new JSONArray().put(jsonMsdRubricOne).put(jsonMsdRubricTwo);
		MSD mds = documentationsUtils.buildMSDFromJson(jsonArray);
		boolean verifyVariableIdMas = Objects.equals(mds.getMasList().getFirst().getIdMas(), jsonMsdRubricOne.getString("idMas"));
		boolean verifyVariableMasLabelLg1 = Objects.equals(mds.getMasList().getLast().getMasLabelLg1(),jsonMsdRubricTwo.getString("masLabelLg1"));
		boolean verifyVariableIdParent = Objects.equals(mds.getMasList().getLast().getIdParent(),jsonMsdRubricTwo.getString("idParent"));
		assertTrue(verifyVariableIdMas && verifyVariableMasLabelLg1 && verifyVariableIdParent );
	}
}
