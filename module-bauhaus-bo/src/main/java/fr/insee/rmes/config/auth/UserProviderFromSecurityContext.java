package fr.insee.rmes.config.auth;

import fr.insee.rmes.config.auth.security.UserDecoder;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public record UserProviderFromSecurityContext(UserDecoder userDecoder) implements UserProvider{

    @Override
    public Optional<User> findUser() throws RmesException {
        return this.userDecoder.fromPrincipal(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }


}
