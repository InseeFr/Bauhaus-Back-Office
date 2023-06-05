package fr.insee.rmes.config.keycloak;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus.rdfserver")
public class KeycloakServerZoneConfiguration {

    private Map<String, ServerZone> zonesByServer;

    static final Logger logger = LogManager.getLogger(KeycloakServerZoneConfiguration.class);

    public KeycloakServerZoneConfiguration(Map<String, ServerZone> zonesByServer) {
        this.zonesByServer = zonesByServer;
        logger.info("------- Servers zone configuration ------\n" +
                zonesByServer.entrySet().stream().reduce(new StringBuilder(), (sb,entry)->sb.append(entry.getKey()+ " -> "+entry.getValue()).append("\n"),StringBuilder::append).toString()+
                    "-------------------------------------------");
    }

    public Map<String, ServerZone> zonesByServer() {
        return zonesByServer;
    }
}
