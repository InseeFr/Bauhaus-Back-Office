package fr.insee.rmes;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.modules.clientconfig.domain.model.ModuleConfig;
import fr.insee.rmes.modules.clientconfig.domain.model.ModuleSettings;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BauhausConfigurationTest {

    @Test
    void should_expose_the_enabled_modules_in_declaration_order_with_their_identifier() {
        Map<String, ModuleSettings> modules = new LinkedHashMap<>();
        modules.put("concepts", new ModuleSettings(true, true, true));
        modules.put("classifications", new ModuleSettings(true, false, true));

        assertThat(configurationWith(modules).enabledModules())
                .containsExactly(
                        new ModuleConfig("concepts", true, true), new ModuleConfig("classifications", false, true));
    }

    @Test
    void should_leave_out_a_disabled_module() {
        Map<String, ModuleSettings> modules = new LinkedHashMap<>();
        modules.put("concepts", new ModuleSettings(true, true, true));
        modules.put("ddi", new ModuleSettings(false, false, false));

        assertThat(configurationWith(modules).enabledModules())
                .containsExactly(new ModuleConfig("concepts", true, true));
    }

    @Test
    void should_expose_no_module_when_none_is_declared() {
        assertThat(configurationWith(null).enabledModules()).isEmpty();
    }

    private static BauhausConfiguration configurationWith(Map<String, ModuleSettings> modules) {
        return new BauhausConfiguration("dev", false, "http://localhost", modules, "1.0.0", "graph");
    }
}
