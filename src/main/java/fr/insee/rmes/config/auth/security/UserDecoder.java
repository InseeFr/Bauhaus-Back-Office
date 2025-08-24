package fr.insee.rmes.config.auth.security;

import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.domain.exceptions.RmesException;

import java.util.Optional;

public interface UserDecoder {

    Optional<User> fromPrincipal(Object principal) throws RmesException;
}
