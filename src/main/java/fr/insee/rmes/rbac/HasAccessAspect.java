package fr.insee.rmes.rbac;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;

@Aspect
@Component
public class HasAccessAspect {

    private final AccessPrivilegesChecker checker;

    public HasAccessAspect(AccessPrivilegesChecker checker) {
        this.checker = checker;
    }

    @Before("@annotation(hasAccess)")
    public void checkPermission(JoinPoint joinPoint, HasAccess hasAccess) throws AccessDeniedException {

        boolean hasPermission = checker.hasAccess(hasAccess.module(), hasAccess.privilege());

        if (!hasPermission) {
            throw new AccessDeniedException("Access Denied: Missing permission");
        }
    }
}
