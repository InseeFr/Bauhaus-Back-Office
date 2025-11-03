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
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setStampClaim("stampClaim");
        jwtProperties.setRoleClaim("roleClaimKey");
        jwtProperties.setIdClaim("idClaim");
        jwtProperties.setSourceClaim("sourceClaim");
        jwtProperties.getRoleClaimConfig().setRoles("keyForRolesInRoleClaim");
        
        OpenIDConnectSecurityContext openIDConnectSecurityContext = new OpenIDConnectSecurityContext(jwtProperties, true);
        UserDecoder user = openIDConnectSecurityContext.userDecoder();
        commonSecurityConfiguration.stampFromPrincipal(user);
        StampFromPrincipal stampFromPrincipal = commonSecurityConfiguration.stampFromPrincipal(user);
        assertNotNull(stampFromPrincipal);
    }
}