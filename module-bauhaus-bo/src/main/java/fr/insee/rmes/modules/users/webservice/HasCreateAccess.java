package fr.insee.rmes.modules.users.webservice;

import fr.insee.rmes.modules.users.domain.model.RBAC;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Droit de créer une ressource du module. Sous la stratégie STAMP, la ressource n'existe pas
 * encore : le timbre de l'utilisateur est comparé aux organisations propriétaires portées par le
 * corps de la requête ({@code dc:creator} ou {@code dc:contributor}, selon l'objet).
 *
 * <pre>{@code
 * @HasCreateAccess(module = RBAC.Module.OPERATION_FAMILY, owners = "#family.contributor()")
 * public ResponseEntity<Object> createFamily(@Valid @RequestBody FamilyRequest family)
 * }</pre>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("@propertiesAccessPrivilegesChecker.hasCreateAccess('{module}', {owners}, authentication.principal)")
public @interface HasCreateAccess {
    RBAC.Module module();

    /** Expression SpEL rendant la collection des organisations propriétaires, en IRI ou en timbre. */
    String owners();
}
