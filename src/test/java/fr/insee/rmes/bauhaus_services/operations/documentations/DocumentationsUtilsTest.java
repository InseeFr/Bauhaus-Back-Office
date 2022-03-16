package fr.insee.rmes.bauhaus_services.operations.documentations;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.model.operations.documentations.DocumentationRubric;

class DocumentationsUtilsTest {

	@InjectMocks
	@Spy
	private DocumentationsUtils documentationsUtils;

    @Mock
    protected DocumentationsRubricsUtils mockDocumentationsRubricsUtils;
    
    @Mock
    protected ParentUtils mockParentUtils;
	
	@Mock
	private RepositoryGestion repoGestion;

	@BeforeEach
	public void init() {
		MockitoAnnotations.openMocks(this);
	}
	@Test
	void buildDocumentationFromJsonTest() throws RmesException{
		
		String[] st = new String[] { Constants.OPERATION_UP, "s8888" };
		
		// Mocker les méthodes de buildDocumentationFromJson qui font appel à d'autres classes
        when(mockDocumentationsRubricsUtils.buildRubricFromJson(Mockito.any(),Mockito.anyBoolean())).thenReturn(new DocumentationRubric());
        when(mockParentUtils.getDocumentationTargetTypeAndId(anyString())).thenReturn(st);
        
		String source="{\"rubrics\":[],\"idSeries\":\"\",\"labelLg2\":\"Metadata report 9999\",\"labelLg1\":\"Rapport de métadonnées 9999\",\"idOperation\":\"s8888\",\"idIndicator\":\"\",\"id\":\"9999\"}";
		JSONObject jsonDocumentation;
		try {
			jsonDocumentation = new JSONObject(source);
		} catch (JSONException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(),
					"JSONException");
		}
		Documentation sims = documentationsUtils.buildDocumentationFromJson(jsonDocumentation,true);
		if (!sims.getId().equals("9999")) {
			fail("false id");
		}
		if (!sims.getIdOperation().equals("s8888")) {
			fail("false idOperation");
		}
		if (!sims.getLabelLg1().equals("Rapport de métadonnées 9999")) {
			fail("false labelLg1");
		}
		if (!sims.getLabelLg2().equals("Metadata report 9999")) {
			fail("false labelLg2");
		}
	}
}
