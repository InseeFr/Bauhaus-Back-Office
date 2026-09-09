package fr.insee.rmes.modules.clientconfig.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ModuleSettingsTest {

    private static final String MODULES_PROPERTY = "fr.insee.rmes.bauhaus.modules";

    @ParameterizedTest(name = "show={0}, directAccess={1} -> show={2}, directAccess={3}")
    @CsvSource(nullValues = "absent", value = {
            // Rien de déclaré : module pleinement ouvert.
            "absent, absent, true,  true",
            // `show` seul : l'accès suit la tuile.
            "true,   absent, true,  true",
            "false,  absent, false, false",
            // `directAccess` seul : la tuile prend la valeur opposée, la seule qui rende
            // le drapeau utile — tuile en maintenance, ou module joignable mais masqué.
            "absent, false,  true,  false",
            "absent, true,   false, true",
            // Les deux déclarés : rien à déduire.
            "true,   true,   true,  true",
            "false,  true,   false, true",
    })
    void should_deduce_the_flag_that_is_not_declared(
            String show, String directAccess, boolean expectedShow, boolean expectedDirectAccess) {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(MODULES_PROPERTY + ".ddi.enabled", "true");
        if (show != null) {
            environment.setProperty(MODULES_PROPERTY + ".ddi.show", show);
        }
        if (directAccess != null) {
            environment.setProperty(MODULES_PROPERTY + ".ddi.directAccess", directAccess);
        }

        assertThat(bindModules(environment))
                .containsExactly(Map.entry("ddi", new ModuleSettings(true, expectedShow, expectedDirectAccess)));
    }

    @Test
    void should_enable_a_fully_open_module_when_nothing_is_declared() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(MODULES_PROPERTY + ".ddi.show", "true");

        assertThat(bindModules(environment)).containsEntry("ddi", new ModuleSettings(true, true, true));
    }

    @Test
    void should_close_everything_of_a_disabled_module_whatever_the_other_flags_say() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(MODULES_PROPERTY + ".ddi.enabled", "false");
        environment.setProperty(MODULES_PROPERTY + ".ddi.show", "true");
        environment.setProperty(MODULES_PROPERTY + ".ddi.direct-access", "true");

        assertThat(bindModules(environment)).containsEntry("ddi", new ModuleSettings(false, false, false));
    }

    @Test
    void should_bind_the_kebab_case_spelling_of_direct_access() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(MODULES_PROPERTY + ".ddi.direct-access", "true");

        assertThat(bindModules(environment)).containsEntry("ddi", new ModuleSettings(true, false, true));
    }

    private static Map<String, ModuleSettings> bindModules(MockEnvironment environment) {
        return Binder.get(environment)
                .bind(MODULES_PROPERTY, Bindable.mapOf(String.class, ModuleSettings.class))
                .orElseGet(Map::of);
    }
}
