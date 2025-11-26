package fr.insee.rmes.bauhaus_services.organizations;

import fr.insee.rmes.modules.commons.configuration.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationsServiceImplTest {

    @InjectMocks
    OrganizationsServiceImpl organizationsService;

    @Mock
    OrganizationUtils organizationUtils;

    @Test
    void shouldGetOrganizationJsonString() throws RmesException {
        JSONObject jsonObject = new JSONObject().put("color","blue");
        when(organizationUtils.getOrganizationJson("45")).thenReturn(jsonObject);
        assertNotNull(organizationsService.getOrganizationJsonString("45"));
    }

    @Test
    void shouldGetOrganization() throws RmesException {
        IdLabelTwoLangs idLabelTwoLangs = new IdLabelTwoLangs("id","label1","label2");
        when(organizationUtils.buildOrganizationFromJson(organizationUtils.getOrganizationJson("45"))).thenReturn(idLabelTwoLangs);
        assertNotNull(organizationsService.getOrganization("45"));
    }

    @Test
    void shouldReturnNullWhenGetOrganizationUriById() throws RmesException {
        assertNull(organizationsService.getOrganization(null));
    }
}