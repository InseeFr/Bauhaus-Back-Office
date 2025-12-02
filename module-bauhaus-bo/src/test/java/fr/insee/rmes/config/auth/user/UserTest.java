package fr.insee.rmes.config.auth.user;

import fr.insee.rmes.domain.auth.User;
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
        boolean stampActualCorrespondToStampExpected = Objects.equals(user.stamp().stamp(), "mockedStamp");
        boolean staticAttributesAreNotEquals= !Objects.equals(User.FAKE_USER.stamp().stamp(), User.EMPTY_USER.stamp().stamp());

        assertTrue(roleActualCorrespondToRoleExpected &&
        roleActualDoesntCorrespondToRoleExpected &&
        stampActualCorrespondToStampExpected &&
        staticAttributesAreNotEquals);
    }

}