package fr.insee.rmes.modules.users.infrastructure;

import fr.insee.rmes.modules.commons.configuration.LogRequestFilter;
import fr.insee.rmes.modules.users.webservice.PublicEndpoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Behaviour of the declarative {@link PublicEndpoint @PublicEndpoint} mechanism: a handler
 * carrying the annotation is reachable anonymously, while a sibling handler that does not is
 * rejected. Exercises the real {@link LazyPublicEndpointsMatcher} wired into a filter chain
 * exactly as the production {@code UserConfiguration} wires it.
 */
@WebMvcTest(
        controllers = PublicEndpointMatcherTest.ProbeController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LogRequestFilter.class)
)
@Import({ PublicEndpointMatcherTest.ProbeController.class, PublicEndpointMatcherTest.TestSecurityConfiguration.class })
class PublicEndpointMatcherTest {

    @Autowired
    MockMvc mvc;

    @Test
    void publicEndpointIsReachableWithoutAuthentication() throws Exception {
        mvc.perform(get("/probe/public")).andExpect(status().isOk());
    }

    @Test
    void nonPublicEndpointIsRejectedWithoutAuthentication() throws Exception {
        mvc.perform(get("/probe/private")).andExpect(status().isUnauthorized());
    }

    @RestController
    @RequestMapping("/probe")
    static class ProbeController {

        @PublicEndpoint
        @GetMapping("/public")
        String open() {
            return "ok";
        }

        @GetMapping("/private")
        String secured() {
            return "ok";
        }
    }

    @TestConfiguration
    @ImportAutoConfiguration(ServletWebSecurityAutoConfiguration.class)
    static class TestSecurityConfiguration {

        @Bean
        RequestMatcher publicEndpointsMatcher(ObjectProvider<RequestMappingHandlerMapping> handlerMapping) {
            return new LazyPublicEndpointsMatcher(handlerMapping);
        }

        @Bean
        SecurityFilterChain filterChain(HttpSecurity http, RequestMatcher publicEndpointsMatcher) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .httpBasic(withDefaults())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(publicEndpointsMatcher).permitAll()
                            .anyRequest().authenticated());
            return http.build();
        }
    }
}
