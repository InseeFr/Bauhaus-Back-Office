package fr.insee.rmes.modules.users.domain;

import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.port.serverside.UserDecoder;
import fr.insee.rmes.modules.users.domain.model.Stamp;
import fr.insee.rmes.modules.users.domain.model.User;
import fr.insee.rmes.modules.users.domain.port.clientside.UserService;

public class DomainUserService implements UserService {
    private final  UserDecoder userDecoder;

    public DomainUserService(UserDecoder userDecoder) {
        this.userDecoder = userDecoder;
    }

    @Override
    public Stamp findStampFrom(Object principal) throws MissingUserInformationException {
        return this.userDecoder.fromPrincipal(principal).map(User::stamp).orElse(null);
    }

    @Override
    public User getUser(Object principal) throws MissingUserInformationException {
        return userDecoder.fromPrincipal(principal).get();
    }
}
