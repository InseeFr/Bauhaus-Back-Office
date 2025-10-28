package fr.insee.rmes.domain.services;

import fr.insee.rmes.domain.auth.User;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.Stamp;
import fr.insee.rmes.domain.port.clientside.UserService;
import fr.insee.rmes.domain.port.serverside.UserDecoder;

public class UserServiceImpl implements UserService {
    private final  UserDecoder userDecoder;

    public UserServiceImpl(UserDecoder userDecoder) {
        this.userDecoder = userDecoder;
    }

    @Override
    public Stamp findStampFrom(Object principal) throws RmesException {
        return this.userDecoder.fromPrincipal(principal).map(User::stamp).orElse(null);
    }
}
