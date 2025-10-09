package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.Config;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class ConfigInitializedEventListener {

    private final Config config;

    public ConfigInitializedEventListener(Config config) {
        this.config = config;
    }


    @PostConstruct
    public void handleConfigInitialized() {
        RdfUtils.setConfig(this.config);
    }
}