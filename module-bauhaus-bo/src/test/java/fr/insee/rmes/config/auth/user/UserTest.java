package fr.insee.rmes.config.auth.user;

import fr.insee.rmes.modules.users.domain.model.User;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCheckUserAttributesValues() {
        User user = new User("mockedId",List.of("firstMockedRole","lastMockedRole"),"mockedStamp");
        boolean roleActualCorrespondToRoleExpected = user.hasRole("lastMockedRole");
        boolean roleActualDoesntCorrespondToRoleExpected = !user.hasRole("mocked");
        boolean stampActualCorrespondToStampExpected = Objects.equals(user.getStamp(), "mockedStamp");

        assertTrue(roleActualCorrespondToRoleExpected &&
        roleActualDoesntCorrespondToRoleExpected &&
        stampActualCorrespondToStampExpected );
    }

}