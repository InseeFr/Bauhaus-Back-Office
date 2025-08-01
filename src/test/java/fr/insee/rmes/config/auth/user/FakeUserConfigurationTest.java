package fr.insee.rmes.config.auth.user;

import org.junit.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public class FakeUserConfigurationTest {

    @Test
    public void shouldSetValuesForFakeUserConfiguration(){
        FakeUserConfiguration fakeUserConfiguration = new FakeUserConfiguration();
        fakeUserConfiguration.setName(Optional.of("mockedName"));
        fakeUserConfiguration.setStamp(Optional.of("mockedStamp"));
        fakeUserConfiguration.setRoles(List.of("firstMockedRole","lastMockedRole"));

        boolean actualNameCorrespondToExpectedName = fakeUserConfiguration.name().toString().equals("Optional[mockedName]");
        boolean actualStampCorrespondToExpectedStamp = fakeUserConfiguration.stamp().toString().equals("Optional[mockedStamp]");
        boolean actualRolesCorrespondToExpectedRoles = fakeUserConfiguration.roles().toString().equals("[firstMockedRole, lastMockedRole]");

        assertTrue(actualNameCorrespondToExpectedName &&
                actualStampCorrespondToExpectedStamp &&
                actualRolesCorrespondToExpectedRoles);
    }
}