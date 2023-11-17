package fr.insee.rmes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class Modules {

    @Value("${fr.insee.rmes.bauhaus.activeModules}")
    private List<String> activeModules;

    @Value("${fr.insee.rmes.bauhaus.modules}")
    private List<String> modules;

    public List<String> getActiveModules() {
        return activeModules;
    }

    public List<String> getModules() {
        return modules;
    }
}
