package fr.insee.rmes.rbac;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static fr.insee.rmes.rbac.RBAC.Module.CONCEPT_CONCEPT;
import static fr.insee.rmes.rbac.RBAC.Privilege.CREATE;
import static org.junit.jupiter.api.Assertions.*;

class UnknownPrivilegeExceptionTest {

    @ParameterizedTest
    @ValueSource(strings = {"mockedCreator","mockedPublisher","mockedLang1","mockedLang2","mockedComponent","mockedStructure" })
    void shouldReturnStringContainingMethodParameters(String roleName){
        UnknownPrivilegeException unknownPrivilegeException= new UnknownPrivilegeException(CREATE,roleName,CONCEPT_CONCEPT);
        assertTrue(unknownPrivilegeException.getMessage().contains(roleName) && unknownPrivilegeException.getMessage().contains(CREATE.toString()) );
    }
}