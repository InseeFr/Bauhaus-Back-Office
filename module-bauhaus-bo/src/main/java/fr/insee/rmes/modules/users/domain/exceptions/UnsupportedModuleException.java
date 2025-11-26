package fr.insee.rmes.modules.users.domain.exceptions;

import fr.insee.rmes.modules.users.domain.model.RBAC;

public class UnsupportedModuleException extends Throwable {
    public UnsupportedModuleException(RBAC.Module module) {
        super("The %s module is not supported".formatted(module));
    }
}
