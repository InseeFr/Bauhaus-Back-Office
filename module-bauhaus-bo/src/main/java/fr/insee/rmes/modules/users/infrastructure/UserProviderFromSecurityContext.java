package fr.insee.rmes.modules.users.infrastructure;

import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import java.util.Optional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public record UserProviderFromSecurityContext(UserDecoder userDecoder) implements UserProvider {

    @Override
    public Optional<User> findUser() throws MissingUserInformationException {
        return this.userDecoder.fromPrincipal(
                SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}
