package fr.insee.rmes.config.auth;

import fr.insee.rmes.config.auth.security.UserDecoder;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static java.util.Optional.empty;

@Component
public record UserProvider(UserDecoder userDecoder) {

    static final Logger logger = LoggerFactory.getLogger(UserProvider.class);

    public Optional<User> findUser() throws RmesException {
        return this.userDecoder.fromPrincipal(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    public User findUserDefaultToEmpty() {
        Optional<User> currentUser;
        try {
            currentUser = findUser();
        } catch (RmesException e) {
            logger.info("while authenticating user => default to empty", e);
            currentUser= empty();
        }
        return currentUser.orElse(User.EMPTY_USER);
    }
}
