package fr.insee.rmes.modules.users.infrastructure;

import fr.insee.rmes.modules.commons.security.PublicEndpoint;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * A {@link RequestMatcher} that matches every request mapped to a handler annotated with
 * {@link fr.insee.rmes.modules.commons.security.PublicEndpoint @PublicEndpoint}.
 * <p>
 * The set of public patterns is resolved lazily, on the first request, from the
 * {@link RequestMappingHandlerMapping}. Resolving lazily (rather than at bean-construction
 * time) avoids the initialization ordering problem between the MVC infrastructure and the
 * security filter chain.
 */
public class LazyPublicEndpointsMatcher implements RequestMatcher {

    /** Never matches; used when no handler is annotated, since {@link OrRequestMatcher} rejects an empty list. */
    private static final RequestMatcher MATCHES_NOTHING = request -> false;

    private final ObjectProvider<RequestMappingHandlerMapping> handlerMappingProvider;
    private final PathPatternRequestMatcher.Builder matcherBuilder = PathPatternRequestMatcher.withDefaults();
    private volatile RequestMatcher delegate;

    public LazyPublicEndpointsMatcher(ObjectProvider<RequestMappingHandlerMapping> handlerMappingProvider) {
        this.handlerMappingProvider = handlerMappingProvider;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        return delegate().matches(request);
    }

    private RequestMatcher delegate() {
        RequestMatcher current = delegate;
        if (current == null) {
            synchronized (this) {
                current = delegate;
                if (current == null) {
                    current = build();
                    delegate = current;
                }
            }
        }
        return current;
    }

    private RequestMatcher build() {
        List<RequestMatcher> matchers = new ArrayList<>();
        handlerMappingProvider.getObject().getHandlerMethods().forEach((info, handler) -> {
            if (isPublic(handler)) {
                matchers.addAll(toMatchers(info));
            }
        });
        return matchers.isEmpty() ? MATCHES_NOTHING : new OrRequestMatcher(matchers);
    }

    private boolean isPublic(HandlerMethod handler) {
        return AnnotatedElementUtils.hasAnnotation(handler.getMethod(), PublicEndpoint.class)
                || AnnotatedElementUtils.hasAnnotation(handler.getBeanType(), PublicEndpoint.class);
    }

    private List<RequestMatcher> toMatchers(RequestMappingInfo info) {
        Set<String> patterns = info.getPathPatternsCondition() != null
                ? info.getPathPatternsCondition().getPatternValues()
                : info.getPatternValues();
        Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
        List<RequestMatcher> result = new ArrayList<>();
        for (String pattern : patterns) {
            if (methods.isEmpty()) {
                result.add(matcherBuilder.matcher(pattern));
            } else {
                for (RequestMethod method : methods) {
                    result.add(matcherBuilder.matcher(HttpMethod.valueOf(method.name()), pattern));
                }
            }
        }
        return result;
    }
}
