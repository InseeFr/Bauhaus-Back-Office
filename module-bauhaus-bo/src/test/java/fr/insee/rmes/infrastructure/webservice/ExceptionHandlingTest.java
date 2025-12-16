package fr.insee.rmes.infrastructure.webservice;

import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.concepts.collections.domain.exceptions.CollectionsFetchException;
import fr.insee.rmes.modules.concepts.collections.domain.port.clientside.CollectionsService;
import fr.insee.rmes.modules.concepts.collections.webservice.CollectionsResources;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CollectionsResources.class, properties = "fr.insee.rmes.bauhaus.activeModules=concepts",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class))
class ExceptionHandlingTest {

    @MockitoBean
    CollectionsService service;

    @Autowired
    MockMvc mvc;

    @Test
    void unexpectedExceptionShouldRespondStatus500() throws CollectionsFetchException, Exception {
        when(service.getAllCollections()).thenThrow(new RuntimeException("Unepected exception"));
        mvc.perform(get("/concepts/collections").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
