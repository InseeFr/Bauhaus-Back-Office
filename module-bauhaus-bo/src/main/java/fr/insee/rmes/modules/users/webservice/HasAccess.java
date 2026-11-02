package fr.insee.rmes.modules.users.webservice;

import fr.insee.rmes.modules.users.domain.model.RBAC;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
// `id` est soit l'identifiant brut, soit le record Id d'un @PathVariable : seul son identifiant est contrôlé.
@PreAuthorize(
        "@propertiesAccessPrivilegesChecker.hasAccess('{module}', '{privilege}', #id instanceof T(fr.insee.rmes.modules.commons.configuration.swagger.model.Id) ? #id.identifier() : #id, authentication.principal)")
public @interface HasAccess {
    RBAC.Module module();

    RBAC.Privilege privilege();
}
