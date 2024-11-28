package fr.insee.rmes.bauhaus_services.operations.documentations;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.model.operations.documentations.DocumentationRubric;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
}
