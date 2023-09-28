package fr.insee.rmes.config.auth.user;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class PreAuthorizeChecker {
    @PreAuthorize("hasRole(#ignoredTestedRole)")
    public void hasRole(String ignoredTestedRole) {
        //empty method to check that user has role `testedRole` with @PreAuthorize
    }
}
