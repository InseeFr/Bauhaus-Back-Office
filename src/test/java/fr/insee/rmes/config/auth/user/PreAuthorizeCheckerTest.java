package fr.insee.rmes.config.auth.user;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PreAuthorizeCheckerTest {

    @Test
    void shouldCheckIfHasRoleRunsQuickly() {
        PreAuthorizeChecker preAuthorizeChecker = new PreAuthorizeChecker();
        StopWatch watch = new StopWatch();
        watch.start();
        preAuthorizeChecker.hasRole("ignoredTestedRole");
        watch.stop();
        assertTrue(watch.getTime()<=10);
    }

}