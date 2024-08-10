package fr.insee.rmes.config.auth.security;

import fr.insee.rmes.external.services.rbac.RBACService;
import fr.insee.rmes.external.services.rbac.StampChecker;
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

@Component
public class BauhausMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

    private static final Logger logger= LoggerFactory.getLogger(BauhausMethodSecurityExpressionHandler.class);

    private final RBACService rbacService;
    private final UserDecoder userDecoder;
    private final StampChecker stampChecker;

    @Autowired
    public BauhausMethodSecurityExpressionHandler(RBACService rbacService, UserDecoder userDecoder, StampChecker stampChecker) {
        this.rbacService = rbacService;
        this.userDecoder = userDecoder;
        this.stampChecker = stampChecker;
        logger.trace("Initializing GlobalMethodSecurityConfiguration with BauhausMethodSecurityExpressionHandler and DefaultRolePrefix = {}", DEFAULT_ROLE_PREFIX);
        setDefaultRolePrefix(DEFAULT_ROLE_PREFIX);
    }

    @Override
    public EvaluationContext createEvaluationContext(Supplier<Authentication> authentication, MethodInvocation mi) {
        StandardEvaluationContext context = (StandardEvaluationContext) super.createEvaluationContext(authentication, mi);
        MethodSecurityExpressionOperations delegate = (MethodSecurityExpressionOperations) context.getRootObject().getValue();
        context.setRootObject(SecurityExpressionRootForBauhaus.enrich(delegate, this.rbacService, this.userDecoder, this.stampChecker));
        return context;
    }
}