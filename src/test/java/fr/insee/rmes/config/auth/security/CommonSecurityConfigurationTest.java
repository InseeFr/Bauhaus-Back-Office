package fr.insee.rmes.config.auth.security;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CommonSecurityConfigurationTest {

    Optional<String> myOptional = Optional.of("allowed Origin");
    CommonSecurityConfiguration commonSecurityConfiguration = new CommonSecurityConfiguration(myOptional ,"appHost") ;

    @Test
    void shouldReturnANewWebMvcConfigurer(){
        WebMvcConfigurer webMvcConfigurer = commonSecurityConfiguration.corsConfigurer();
        assertNotNull(webMvcConfigurer);
    }

    @Test
    void shouldReturnStampFromPrincipal(){
        OpenIDConnectSecurityContext openIDConnectSecurityContext = new OpenIDConnectSecurityContext("stampClaim", "roleClaimKey", "idClaim", true,"keyForRolesInRoleClaim");
        UserDecoder user = openIDConnectSecurityContext.userDecoder();
        commonSecurityConfiguration.stampFromPrincipal(user);
        StampFromPrincipal stampFromPrincipal = commonSecurityConfiguration.stampFromPrincipal(user);
        assertNotNull(stampFromPrincipal);
    }
}