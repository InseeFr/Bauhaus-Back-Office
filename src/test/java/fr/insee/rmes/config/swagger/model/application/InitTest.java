package fr.insee.rmes.config.swagger.model.application;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InitTest {

    @Test
    void shouldCheckAttributesDefaultValuesAreNull() {
        Init init = new Init();
        List<Boolean> actual = List.of(init.appHost == null,
                init.authorizationHost==null,
                init.defaultContributor==null,
                init.defaultMailSender==null,
                init.maxLengthScopeNote==null,
                init.lg1==null,
                init.lg2==null,
                init.authType==null
        );
        List<Boolean> expected = List.of(true, true,true, true,true, true,true, true);
        assertEquals(expected, actual);
    }
    
}