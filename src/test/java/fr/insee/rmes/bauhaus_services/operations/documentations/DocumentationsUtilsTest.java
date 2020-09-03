package fr.insee.rmes.bauhaus_services.operations.documentations;

import static org.junit.jupiter.api.Assertions.fail;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.documentations.Documentation;

public class DocumentationsUtilsTest {

	@InjectMocks
	@Spy
	private DocumentationsUtils documentationsUtils;

	@Mock
	private RepositoryGestion repoGestion;

	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	@Test
	void buildDocumentationFromJsonTest() throws RmesException{
		// Attention, mocker les méthodes de buildDocumentationFromJson qui font appel à la base rdf
		String source="{\"rubrics\":[],\"idSeries\":\"\",\"labelLg2\":\"Metadata report 9999\",\"labelLg1\":\"Rapport de métadonnées 9999\",\"idOperation\":\"s8888\",\"idIndicator\":\"\",\"id\":\"9999\"}";
		JSONObject jsonDocumentation = new JSONObject(source);
		Documentation sims = documentationsUtils.buildDocumentationFromJson(jsonDocumentation);
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
