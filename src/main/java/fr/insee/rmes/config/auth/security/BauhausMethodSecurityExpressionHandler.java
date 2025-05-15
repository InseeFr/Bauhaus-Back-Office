package fr.insee.rmes.config.auth.security;

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

    private final StampFromPrincipal stampFromPrincipal;

    @Autowired
    public BauhausMethodSecurityExpressionHandler(
            StampFromPrincipal stampFromPrincipal) {
        logger.trace("Initializing GlobalMethodSecurityConfiguration with BauhausMethodSecurityExpressionHandler and DefaultRolePrefix = {}", DEFAULT_ROLE_PREFIX);
        this.stampFromPrincipal = requireNonNull(stampFromPrincipal);

        setDefaultRolePrefix(DEFAULT_ROLE_PREFIX);
    }

    @Override
    public EvaluationContext createEvaluationContext(Supplier<Authentication> authentication, MethodInvocation mi) {
        StandardEvaluationContext context = (StandardEvaluationContext) super.createEvaluationContext(authentication, mi);
        MethodSecurityExpressionOperations delegate = (MethodSecurityExpressionOperations) context.getRootObject().getValue();
        context.setRootObject(SecurityExpressionRootForBauhaus.enrich(delegate, this.stampFromPrincipal));
        return context;
    }
}