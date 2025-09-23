package fr.insee.rmes.config.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus.keycloak-configuration",ignoreUnknownFields = false)
public class KeycloakServerZoneConfiguration {

    private Map<String, ServerZone> zoneByServers;


    public  KeycloakServerZoneConfiguration() {
    }

    public void setZoneByServers(Map<String, ServerZone> zoneByServers) {
        this.zoneByServers = zoneByServers;
    }

    public KeycloakServerZoneConfiguration(Map<String, ServerZone> rdfserver){
        this.zoneByServers =rdfserver;
    }

    public Map<String, ServerZone> rdfserver() {
        return zoneByServers;
    }
}
