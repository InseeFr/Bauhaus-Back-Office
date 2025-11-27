package fr.insee.rmes.modules.users.domain.exceptions;

import fr.insee.rmes.modules.users.domain.model.RBAC;

public class StampFetchException extends Throwable {
    public StampFetchException(RBAC.Module module, String id) {
        super("We can not fetch the stamp for the objet %s on the module %s".formatted(id, module));
    }
}
