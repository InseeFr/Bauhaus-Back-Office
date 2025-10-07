package fr.insee.rmes.rbac;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static fr.insee.rmes.rbac.RBAC.Module.CONCEPT_CONCEPT;
import static org.junit.jupiter.api.Assertions.*;

class UnknownApplicationExceptionTest {

    @ParameterizedTest
    @ValueSource(strings = {"mockedName","mockedObject","mockedNumber","mockedConcept","mockedRoleName" })
    void shouldReturnStringContainingMethodArguments(String roleName){
        UnknownApplicationException unknownApplicationException= new UnknownApplicationException(CONCEPT_CONCEPT,roleName);
        assertTrue(unknownApplicationException.getMessage().contains(roleName));
    }
}