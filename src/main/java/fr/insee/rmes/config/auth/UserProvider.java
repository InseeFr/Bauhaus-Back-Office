package fr.insee.rmes.config.auth;

import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static java.util.Optional.empty;


public interface UserProvider {

    Logger logger = LoggerFactory.getLogger(UserProvider.class);

    Optional<User> findUser() throws RmesException;

    default User findUserDefaultToEmpty() {
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
