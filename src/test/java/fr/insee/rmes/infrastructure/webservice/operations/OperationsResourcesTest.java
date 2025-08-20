package fr.insee.rmes.infrastructure.webservice.operations;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.OperationsService;
import fr.insee.rmes.config.auth.UserProviderFromSecurityContext;
import fr.insee.rmes.config.auth.security.DefaultSecurityContext;
import fr.insee.rmes.config.auth.user.FakeUserConfiguration;
import fr.insee.rmes.model.operations.Operation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = OperationsResources.class, properties = {
        "fr.insee.rmes.bauhaus.force.ssl = false"
})
@Import({UserProviderFromSecurityContext.class, DefaultSecurityContext.class})
class OperationsResourcesTest {

    @MockitoBean
    protected OperationsService operationsService;

    @MockitoBean
    protected OperationsDocumentationsService documentationsService;

    @MockitoBean
    FakeUserConfiguration fakeUserConfiguration;

    @Autowired
    MockMvc mockMvc;

    @CsvSource({
            "1, '"+MediaType.APPLICATION_JSON_VALUE+"', '{\"id\":\"1\"}'",
            "1, '"+MediaType.APPLICATION_XML_VALUE+"', '<Operation><id>1</id><prefLabelLg1/><prefLabelLg2/><altLabelLg1/><altLabelLg2/><series/><idSims/><created/><modified/><validationState/><year/></Operation>'",
    })
    @ParameterizedTest
    void getOperationByID_resultFormatFitAccessHeader(String id, String mediaType, String expected) throws Exception {
        var operation = new Operation();
        operation.setId(id);
        when(operationsService.getOperationById(id)).thenReturn(operation);

        var response=mockMvc.perform(MockMvcRequestBuilders.get("/operations/operation/"+id)
                        .accept(MediaType.valueOf(mediaType)))
                .andExpect(status().isOk());
        if (MediaType.APPLICATION_JSON_VALUE.equals(mediaType)) {
            response.andExpect(content().json(expected));
        }
        if (MediaType.APPLICATION_XML_VALUE.equals(mediaType)) {
            response.andExpect(content().xml(expected));
        }
    }
}