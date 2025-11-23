package fr.insee.rmes.config.auth;

import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import fr.insee.rmes.modules.users.domain.model.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public record UserProviderFromSecurityContext(UserDecoder userDecoder) implements UserProvider{

    @Override
    public Optional<User> findUser() throws MissingUserInformationException {
        return this.userDecoder.fromPrincipal(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }


}
