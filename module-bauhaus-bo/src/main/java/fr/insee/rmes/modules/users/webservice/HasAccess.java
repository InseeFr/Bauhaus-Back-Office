package fr.insee.rmes.modules.users.webservice;

import fr.insee.rmes.modules.users.domain.model.RBAC;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("@propertiesAccessPrivilegesChecker.hasAccess('{module}', '{privilege}', #id, authentication.principal)")
public @interface HasAccess {
    RBAC.Module module();

    RBAC.Privilege privilege();
}
