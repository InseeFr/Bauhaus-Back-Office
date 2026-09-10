package fr.insee.rmes.modules.commons.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller handler (or a whole controller) as publicly accessible, i.e. exempt
 * from authentication.
 * <p>
 * The decision lives next to the endpoint instead of in a central URL list. At startup the
 * {@code publicEndpointsMatcher} bean scans the {@code RequestMappingHandlerMapping} for
 * handlers carrying this annotation and exposes them as a {@code permitAll()} matcher to the
 * {@code SecurityFilterChain}.
 * <p>
 * Fail-safe by design: an endpoint <em>without</em> this annotation stays authenticated.
 * <p>
 * Lives in the shared {@code commons.security} package because it is a cross-cutting marker
 * used by controllers of every module (the {@code webservice} layer) and read by the security
 * {@code infrastructure} ({@code LazyPublicEndpointsMatcher}); a neutral home keeps the
 * hexagonal rule "infrastructure must not depend on webservice" satisfied.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PublicEndpoint {}
