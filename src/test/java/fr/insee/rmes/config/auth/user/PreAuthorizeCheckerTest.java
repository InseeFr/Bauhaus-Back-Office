package fr.insee.rmes.config.auth.user;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PreAuthorizeCheckerTest {

    @Test
    void shouldCheckIfHasRoleRunsQuickly() {
        PreAuthorizeChecker preAuthorizeChecker = new PreAuthorizeChecker();
        var begin= System.nanoTime();
        preAuthorizeChecker.hasRole("ignoredTestedRole");
        var end = System. nanoTime();
        assertTrue(end-begin<=1000000000);
    }

}