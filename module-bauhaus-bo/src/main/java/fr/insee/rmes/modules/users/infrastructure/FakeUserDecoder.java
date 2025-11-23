package fr.insee.rmes.modules.users.infrastructure;

import fr.insee.rmes.domain.Roles;
import fr.insee.rmes.domain.auth.Source;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.users.domain.model.Stamp;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

public class FakeUserDecoder implements UserDecoder {

    public static final User FAKE_USER = new User("fakeUser", List.of(Roles.ADMIN), new Stamp("fakeStampForDvAndQf"), Source.INSEE);

    @Override
    public Optional<User> fromPrincipal(Object principal) {
        return Optional.of(FAKE_USER);
    }
}
