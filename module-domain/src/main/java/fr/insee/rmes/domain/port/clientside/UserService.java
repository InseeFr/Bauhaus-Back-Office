package fr.insee.rmes.domain.port.clientside;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.Stamp;

public interface UserService {
    Stamp findStampFrom(Object principal) throws RmesException;
}
