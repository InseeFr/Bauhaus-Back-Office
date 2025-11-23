package fr.insee.rmes.config.auth;

import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static java.util.Optional.empty;


public interface UserProvider {

    Logger logger = LoggerFactory.getLogger(UserProvider.class);

    Optional<User> findUser() throws RmesException, MissingUserInformationException;

    default User findUserDefaultToEmpty() {
        Optional<User> currentUser;
        try {
            currentUser = findUser();
        } catch (RmesException | MissingUserInformationException e) {
            logger.info("while authenticating user => default to empty", e);
            currentUser= empty();
        }
        return currentUser.orElse(User.EMPTY_USER);
    }
}
