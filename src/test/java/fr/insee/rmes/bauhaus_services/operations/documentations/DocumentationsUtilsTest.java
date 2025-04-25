package fr.insee.rmes.bauhaus_services.operations.documentations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotAcceptableException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.model.operations.documentations.DocumentationRubric;
import fr.insee.rmes.model.operations.documentations.MAS;
import fr.insee.rmes.model.operations.documentations.MSD;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class DocumentationsUtilsTest {

	@InjectMocks
	@Spy
	private DocumentationsUtils documentationsUtils;

    @Mock
    protected DocumentationsRubricsUtils mockDocumentationsRubricsUtils;
    
    @Mock
    protected ParentUtils mockParentUtils;

	@BeforeEach
	public void init() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void shouldThrowExceptionIfParentTargetIsUnpublished() throws RmesException {
		JSONObject operation = new JSONObject();
		operation.put(Constants.ID, "1");
		JSONObject series = new JSONObject();
		series.put("id", "2");
		operation.put("series", series);

		String[] target = {"series", "2"};
		when(mockParentUtils.getDocumentationTargetTypeAndId("1")).thenReturn(target);
		when(mockParentUtils.getValidationStatus("2")).thenReturn(ValidationStatus.UNPUBLISHED.toString());

		assertThrows(
				RmesBadRequestException.class,
				() -> documentationsUtils.publishMetadataReport("1")
		);
	}

	@Test
	void buildDocumentationFromJsonTest() throws RmesException{
		
		String[] st = new String[] { Constants.OPERATION_UP, "s8888" };
		
        when(mockDocumentationsRubricsUtils.buildRubricFromJson(Mockito.any(),Mockito.anyBoolean())).thenReturn(new DocumentationRubric());
        when(mockParentUtils.getDocumentationTargetTypeAndId(anyString())).thenReturn(st);
        
		JSONObject documentation = new JSONObject()
										.put("rubrics", new JSONArray())
										.put("idSeries", "")
										.put("id", "9999")
										.put("idOperation", "s8888")
										.put("labelLg1", "Rapport de métadonnées 9999")
										.put("labelLg2", "Metadata report 9999");

		Documentation sims = documentationsUtils.buildDocumentationFromJson(documentation,true);

		assertEquals("9999", sims.getId());
		assertEquals("s8888", sims.getIdOperation());
		assertEquals("Rapport de métadonnées 9999", sims.getLabelLg1());
		assertEquals("Metadata report 9999", sims.getLabelLg2());
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
