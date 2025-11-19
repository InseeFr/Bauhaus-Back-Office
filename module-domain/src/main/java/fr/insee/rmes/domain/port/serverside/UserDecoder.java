package fr.insee.rmes.domain.port.serverside;

import fr.insee.rmes.domain.auth.User;
import fr.insee.rmes.domain.exceptions.RmesException;

import java.util.Optional;

public interface UserDecoder {

    Optional<User> fromPrincipal(Object principal) throws RmesException;
}
