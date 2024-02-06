package fr.insee.rmes.webservice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;


@SpringBootTest(properties = "fr.insee.rmes.bauhaus.activeModules=")
@ExtendWith(MockitoExtension.class)
class UnactiveModulesTest {

    @Autowired(required = false)
    StructureResources structureResources;

    @Test
    @WithMockUser
    void shouldReturnAnErrorIfTheStructureModuleIsNotActive(){
        Assertions.assertNull(structureResources);
    }

}
