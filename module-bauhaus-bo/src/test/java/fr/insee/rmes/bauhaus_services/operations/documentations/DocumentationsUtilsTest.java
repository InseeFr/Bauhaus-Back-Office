package fr.insee.rmes.bauhaus_services.operations.documentations;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.graphdb.ontologies.ADMS;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.model.operations.documentations.DocumentationRubric;
import fr.insee.rmes.model.operations.documentations.MAS;
import fr.insee.rmes.model.operations.documentations.MSD;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.modules.operations.msd.infrastructure.graphdb.DocumentationQueries;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.eclipse.rdf4j.model.Resource;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class DocumentationsUtilsTest {

	@Mock
	private RepositoryGestion repoGestion;

	@Mock
	private RepositoryPublication repositoryPublication;


	@Mock
	private ParentUtils parentUtils;

	@Mock
	private DocumentationsRubricsUtils documentationsRubricsUtils;

	@Mock
	private DocumentationPublication documentationPublication;

	@Mock
	private DocumentationQueries documentationQueries;

	@Mock
	private BauhausLanguagesProperties languages;

	@InjectMocks
	private DocumentationsUtils documentationsUtils;

	@BeforeEach
	void initStaticGraphs() {
		RdfUtils.setGraphs(GraphsPropertiesStub.stub());
		RdfUtils.setBauhausUriBuilder(new BauhausUriBuilder("http://bauhaus/publication/", "http://bauhaus/", p -> Optional.of("/operations")));
	}

	@Test
	void saveRdfMetadataReport_addsAdmsIdentifierTriple() throws RmesException {
		Documentation sims = new Documentation();
		sims.setId("1500");
		IRI target = SimpleValueFactory.getInstance().createIRI("http://bauhaus/operations/series/s1");

		documentationsUtils.saveRdfMetadataReport(sims, target, ValidationStatus.UNPUBLISHED);

		ArgumentCaptor<Model> captor = ArgumentCaptor.forClass(Model.class);
		verify(repoGestion).replaceGraph(any(Resource.class), captor.capture(), any());
		IRI simsURI = RdfUtils.objectIRI(ObjectType.DOCUMENTATION, "1500");
		assertThat(captor.getValue().filter(simsURI, ADMS.HAS_IDENTIFIER, null).objects())
				.containsExactly(SimpleValueFactory.getInstance().createLiteral("1500"));
	}

	@Test
	void shouldThrowRmesNotFoundExceptionWhenGetDocumentationByIdSims() throws RmesException {

		String idSims ="2025";
		when(documentationQueries.getDocumentationTitleQuery(idSims)).thenReturn("mock-title-query");
		when(repoGestion.getResponseAsObject("mock-title-query")).thenReturn(new JSONObject());
		RmesException exception = assertThrows(RmesNotFoundException.class, () -> documentationsUtils.getDocumentationByIdSims(idSims));
		assertTrue(exception.getDetails().contains("Documentation not found"));
	}

	@Test
	void deleteMetadataReport_whenSimsDoesNotExist_shouldThrowNotFound() throws RmesException {
		String id = "unknown";
		when(documentationQueries.getDocumentationTitleQuery(id)).thenReturn("mock-title-query");
		when(repoGestion.getResponseAsObject("mock-title-query")).thenReturn(new JSONObject());

		RmesException exception = assertThrows(RmesNotFoundException.class, () -> documentationsUtils.deleteMetadataReport(id));

		assertTrue(exception.getDetails().contains("Documentation not found"));
	}

	@Test
	void deleteMetadataReport_shouldSucceed_regardlessOfTargetType() throws RmesException {
		// La suppression d'un SIMS est désormais autorisée pour tout type de cible
		// (série, opération ou indicateur) — cf. retrait de la contrainte "Only a sims
		// that documents a series can be deleted".
		String id = "2025";
		when(documentationQueries.getDocumentationTitleQuery(id)).thenReturn("mock-title-query");
		when(repoGestion.getResponseAsObject("mock-title-query")).thenReturn(new JSONObject().put(Constants.LABEL_LG1, "Sims"));
		when(documentationQueries.deleteGraph(any(Resource.class))).thenReturn("delete-graph-query");
		when(repoGestion.executeUpdate("delete-graph-query")).thenReturn(HttpStatus.OK);
		when(repositoryPublication.executeUpdate("delete-graph-query")).thenReturn(HttpStatus.OK);

		HttpStatus result = documentationsUtils.deleteMetadataReport(id);

		assertEquals(HttpStatus.OK, result);
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
		givenMetadataReportState("1", ValidationStatus.UNPUBLISHED);
		when(parentUtils.getValidationStatus("seriesExample")).thenReturn(ValidationStatus.UNPUBLISHED.toString());
		RmesException exception = assertThrows(RmesBadRequestException.class, () -> documentationsUtils.publishMetadataReport("1"));
		assertTrue(exception.getDetails().contains("This metadataReport cannot be published before its target is published. "));
	}

	@Test
	void shouldThrowRmesBadRequestExceptionIfMetadataReportIsAlreadyPublished() throws RmesException {
		String[] target = {"series", "seriesExample"};
		when(parentUtils.getDocumentationTargetTypeAndId("1")).thenReturn(target);
		givenMetadataReportState("1", ValidationStatus.VALIDATED);

		RmesException exception = assertThrows(RmesBadRequestException.class, () -> documentationsUtils.publishMetadataReport("1"));

		assertTrue(exception.getDetails().contains("\"code\":1301"));
		assertTrue(exception.getDetails().contains("MetadataReport: 1"));
		verify(documentationPublication, org.mockito.Mockito.never()).publishSims(anyString());
	}

	private void givenMetadataReportState(String id, ValidationStatus status) throws RmesException {
		String query = "getPublicationState-" + id;
		when(documentationQueries.getPublicationState(id)).thenReturn(query);
		when(repoGestion.getResponseAsObject(query)).thenReturn(new JSONObject().put("state", status.getValue()));
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
	void setMetadataReport_shouldNotRejectWith406_whenCreatingSimsOnSeriesWithOperations() throws RmesException {
		// Une série avec opérations doit pouvoir recevoir un SIMS (cf. ticket #1452).
		String idTarget = "s1234";
		String body = "{\"idTarget\":\"" + idTarget + "\",\"idSeries\":\"" + idTarget + "\"}";

		lenient().when(documentationQueries.getSimsByTarget(idTarget)).thenReturn("q-sims");
		lenient().when(documentationQueries.lastID()).thenReturn("q-last");
		lenient().when(repoGestion.getResponseAsObject(anyString())).thenReturn(new JSONObject());

		try {
			documentationsUtils.setMetadataReport(null, body, true);
		} catch (RmesNotAcceptableException e) {
			if (e.getDetails().contains("Cannot create Sims for a series which already has operations")) {
				fail("La création d'un SIMS sur une série avec opérations ne devrait plus lever 406 : " + e.getDetails());
			}
		} catch (Exception ignored) {
			// d'autres exceptions sont attendues car les mocks ne couvrent pas tout le flow
		}
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
