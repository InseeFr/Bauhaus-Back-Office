package fr.insee.rmes.config.auth.security;

import fr.insee.rmes.bauhaus_services.StampAuthorizationChecker;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import static java.util.Objects.requireNonNull;

public class BauhausMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

	private final StampAuthorizationChecker stampAuthorizationChecker;
	private final StampFromPrincipal stampFromPrincipal;

	public BauhausMethodSecurityExpressionHandler(StampAuthorizationChecker stampAuthorizationChecker, StampFromPrincipal stampFromPrincipal) {
		this.stampAuthorizationChecker = requireNonNull(stampAuthorizationChecker);
        this.stampFromPrincipal = requireNonNull(stampFromPrincipal);
    }

	@Override
	protected MethodSecurityExpressionOperations createSecurityExpressionRoot(Authentication authentication, MethodInvocation invocation) {
		return SecurityExpressionRootForBauhaus.enrich(super.createSecurityExpressionRoot(authentication, invocation), this.stampAuthorizationChecker, this.stampFromPrincipal);
	}
/* for spring security 6
	@Override
	public EvaluationContext createEvaluationContext(Supplier<Authentication> authentication, MethodInvocation mi) {
		StandardEvaluationContext context = (StandardEvaluationContext) super.createEvaluationContext(authentication, mi);
		MethodSecurityExpressionOperations delegate = (MethodSecurityExpressionOperations) context.getRootObject().getValue();
		context.setRootObject(SecurityExpressionRootForBauhaus.enrich(delegate, this.stampAuthorizationChecker, this.userDecoder));
		return context;
	}*/
}