package fr.insee.rmes.modules.users.webservice;

import fr.insee.rmes.modules.users.domain.model.RBAC;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Droit de créer une ressource du module rattachée à un parent existant dont elle hérite les
 * propriétaires. Sous la stratégie STAMP, ce sont ceux du parent, relus en base, qui sont comparés
 * au timbre de l'utilisateur (une opération n'a pas de {@code dc:creator} propre : ce sont ceux de
 * sa série).
 *
 * <pre>{@code
 * @HasCreateAccessUnder(
 *         module = RBAC.Module.OPERATION_OPERATION,
 *         parentModule = RBAC.Module.OPERATION_SERIES,
 *         parentId = "#body.series().id()")
 * public ResponseEntity<String> createOperation(@Valid @RequestBody OperationCreationRequest body)
 * }</pre>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize(
        "@propertiesAccessPrivilegesChecker.hasCreateAccessUnder('{module}', '{parentModule}', {parentId}, authentication.principal)")
public @interface HasCreateAccessUnder {
    RBAC.Module module();

    RBAC.Module parentModule();

    /** Expression SpEL rendant l'identifiant du parent. */
    String parentId();
}
