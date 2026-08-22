package fr.insee.rmes.bauhaus_services.organizations;

import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.configuration.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.modules.organisations.infrastructure.graphdb.OrganizationQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationRepositoryTest {

    @InjectMocks
    OrganizationRepository organizationRepository;

    @Mock
    FamOpeSerIndUtils famOpeSerUtils;

    @Mock
    RepositoryGestion repoGestion;

    @Mock
    OrganizationQueries organizationQueries;

    @Test
    void shouldBuildOrganizationFromJson()  {
        JSONObject jsonObject = new JSONObject().put("color","blue");
        IdLabelTwoLangs idLabelTwoLangs = IdLabelTwoLangs.of("id","label1","label2");
        when(famOpeSerUtils.buildIdLabelTwoLangsFromJson(jsonObject)).thenReturn(idLabelTwoLangs);
        assertNotNull(organizationRepository.buildOrganizationFromJson(jsonObject));
    }

    @Test
    void shouldAppendAcronymToBothLabelsAndDropTheAcronymKey() throws RmesException {
        JSONObject sparqlResult = new JSONObject()
                .put("labelLg1", "Direction générale de l'Administration et de la Fonction publique")
                .put("labelLg2", "Directorate-General for Administration and the Civil Service")
                .put("acronym", "DGAFP");
        when(organizationQueries.organizationQuery("HIE2171581")).thenReturn("query");
        when(repoGestion.getResponseAsObject("query")).thenReturn(sparqlResult);

        JSONObject result = organizationRepository.getOrganizationJson("HIE2171581");

        assertThat(result.getString("labelLg1"))
                .isEqualTo("Direction générale de l'Administration et de la Fonction publique (DGAFP)");
        assertThat(result.getString("labelLg2"))
                .isEqualTo("Directorate-General for Administration and the Civil Service (DGAFP)");
        assertThat(result.has("acronym")).isFalse();
    }

    @Test
    void shouldLeaveLabelsUnchangedWhenNoAcronym() throws RmesException {
        JSONObject sparqlResult = new JSONObject()
                .put("labelLg1", "Service des données")
                .put("labelLg2", "Data Department");
        when(organizationQueries.organizationQuery(anyString())).thenReturn("query");
        when(repoGestion.getResponseAsObject("query")).thenReturn(sparqlResult);

        JSONObject result = organizationRepository.getOrganizationJson("ID");

        assertThat(result.getString("labelLg1")).isEqualTo("Service des données");
        assertThat(result.getString("labelLg2")).isEqualTo("Data Department");
    }
}