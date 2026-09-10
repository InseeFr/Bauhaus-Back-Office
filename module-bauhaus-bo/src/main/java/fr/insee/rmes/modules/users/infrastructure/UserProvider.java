package fr.insee.rmes.modules.users.infrastructure;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.User;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface UserProvider {

    Logger logger = LoggerFactory.getLogger(UserProvider.class);

    Optional<User> findUser() throws RmesException, MissingUserInformationException;
}
