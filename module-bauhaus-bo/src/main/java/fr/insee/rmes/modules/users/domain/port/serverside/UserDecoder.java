package fr.insee.rmes.modules.users.domain.port.serverside;

import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.User;

import java.util.Optional;

public interface UserDecoder {

    Optional<User> fromPrincipal(Object principal) throws MissingUserInformationException;
}
