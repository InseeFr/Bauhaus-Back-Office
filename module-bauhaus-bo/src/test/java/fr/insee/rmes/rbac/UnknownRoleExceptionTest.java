package fr.insee.rmes.rbac;

import fr.insee.rmes.modules.users.domain.exceptions.UnknownRoleException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class UnknownRoleExceptionTest {

    @ParameterizedTest
    @ValueSource(strings = {"mockedID","mockedDescription","mockedRepoGestion","mockedLabel","mockedTocken","mockedOperation","mockedFamilies" })
    void shouldReturnExceptionContainingMethodParameters(String roleName){
        UnknownRoleException unknownRoleException= new UnknownRoleException(roleName);
        assertTrue(unknownRoleException.getMessage().contains(roleName));
    }
}