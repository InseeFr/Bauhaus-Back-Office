package fr.insee.rmes.rbac;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("@propertiesAccessPrivilegesChecker.hasAccess('{module}', '{privilege}', #id, authentication.principal)")
public @interface HasAccess {
    RBAC.Module module();
    RBAC.Privilege privilege();
}