package fr.insee.rmes.bauhaus_services.organizations;

import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.modules.commons.configuration.swagger.model.IdLabelTwoLangs;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationUtilsTest {

    @InjectMocks
    OrganizationUtils organizationUtils;

    @Mock
    FamOpeSerIndUtils famOpeSerUtils;

    @Test
    void shouldBuildOrganizationFromJson()  {
        JSONObject jsonObject = new JSONObject().put("color","blue");
        IdLabelTwoLangs idLabelTwoLangs = new IdLabelTwoLangs("id","label1","label2");
        when(famOpeSerUtils.buildIdLabelTwoLangsFromJson(jsonObject)).thenReturn(idLabelTwoLangs);
        assertNotNull(organizationUtils.buildOrganizationFromJson(jsonObject));
    }
}