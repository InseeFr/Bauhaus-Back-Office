package fr.insee.rmes.config.auth.security;

import fr.insee.rmes.bauhaus_services.StampAuthorizationChecker;
import fr.insee.rmes.rbac.AccessPrivilegesChecker;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static fr.insee.rmes.config.auth.security.CommonSecurityConfiguration.DEFAULT_ROLE_PREFIX;
import static java.util.Objects.requireNonNull;

@Component
public class BauhausMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

    private static final Logger logger= LoggerFactory.getLogger(BauhausMethodSecurityExpressionHandler.class);

    private final StampAuthorizationChecker stampAuthorizationChecker;
    private final StampFromPrincipal stampFromPrincipal;
    private final AccessPrivilegesChecker accessPrivilegesChecker;

    @Autowired
    public BauhausMethodSecurityExpressionHandler(
            StampAuthorizationChecker stampAuthorizationChecker,
            StampFromPrincipal stampFromPrincipal,
            AccessPrivilegesChecker accessPrivilegesChecker) {
        logger.trace("Initializing GlobalMethodSecurityConfiguration with BauhausMethodSecurityExpressionHandler and DefaultRolePrefix = {}", DEFAULT_ROLE_PREFIX);
        this.stampAuthorizationChecker = requireNonNull(stampAuthorizationChecker);
        this.stampFromPrincipal = requireNonNull(stampFromPrincipal);
        this.accessPrivilegesChecker = requireNonNull(accessPrivilegesChecker);
        setDefaultRolePrefix(DEFAULT_ROLE_PREFIX);
    }

    @Override
    public EvaluationContext createEvaluationContext(Supplier<Authentication> authentication, MethodInvocation mi) {
        StandardEvaluationContext context = (StandardEvaluationContext) super.createEvaluationContext(authentication, mi);
        MethodSecurityExpressionOperations delegate = (MethodSecurityExpressionOperations) context.getRootObject().getValue();
        context.setRootObject(SecurityExpressionRootForBauhaus.enrich(delegate, this.stampAuthorizationChecker, this.stampFromPrincipal, this.accessPrivilegesChecker));
        return context;
    }
}