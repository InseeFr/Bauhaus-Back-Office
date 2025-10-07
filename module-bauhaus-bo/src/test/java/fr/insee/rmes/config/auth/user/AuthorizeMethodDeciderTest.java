package fr.insee.rmes.config.auth.user;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AuthorizeMethodDeciderTest {

    @Test
    public void shouldCheckIfAdminHasRole(){
        PreAuthorizeChecker preAuthorizeChecker = new PreAuthorizeChecker();
        AuthorizeMethodDecider authorizeMethodDecider = new AuthorizeMethodDecider(preAuthorizeChecker);
        assertTrue(authorizeMethodDecider.isAdmin());
    }
}