package fr.insee.rmes.modules.users.domain.port.clientside;

import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import fr.insee.rmes.modules.users.domain.model.Stamp;
import fr.insee.rmes.modules.users.domain.model.User;

public interface UserService {
    Stamp findStampFrom(Object principal) throws MissingUserInformationException;

    User getUser(Object principal) throws MissingUserInformationException;
}
