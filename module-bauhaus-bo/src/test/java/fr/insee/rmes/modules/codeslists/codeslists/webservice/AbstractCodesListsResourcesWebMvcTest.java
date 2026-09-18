package fr.insee.rmes.modules.codeslists.codeslists.webservice;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.modules.codeslists.codeslists.domain.port.clientside.CodesListsService;
import fr.insee.rmes.modules.codeslists.partialcodeslists.webservice.PartialCodeListsResources;
import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Socle des tests du contrat HTTP des contrôleurs de listes de codes, complètes et partielles :
 * services simulés, sécurité désactivée, gestionnaire d'exceptions réel.
 */
@WebMvcTest(
        value = {CodesListsResources.class, PartialCodeListsResources.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class),
        excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
abstract class AbstractCodesListsResourcesWebMvcTest {

    @MockitoBean
    CodeListService codeListService;

    @MockitoBean
    CodesListsService codesListsService;

    @Autowired
    MockMvc mockMvc;
}
