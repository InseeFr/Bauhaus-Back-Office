package fr.insee.rmes.bauhaus_services.operations.documentations;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

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
	private RepositoryGestion repoGestion;

	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	@Test
	@Disabled
	void buildDocumentationFromJsonTest() throws RmesException{
		
		// Mocker les méthodes de buildDocumentationFromJson qui font appel à d'autres classes
        when(mockDocumentationsRubricsUtils.buildRubricFromJson(Mockito.any(),true)).thenReturn(new DocumentationRubric());
		
		String source="{\"rubrics\":[],\"idSeries\":\"\",\"labelLg2\":\"Metadata report 9999\",\"labelLg1\":\"Rapport de métadonnées 9999\",\"idOperation\":\"s8888\",\"idIndicator\":\"\",\"id\":\"9999\"}";
		JSONObject jsonDocumentation = new JSONObject(source);
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
		if (!sims.getLabelLg1().equals("Metadata report 9999")) {
			fail("false labelLg2");
		}
	}
}
