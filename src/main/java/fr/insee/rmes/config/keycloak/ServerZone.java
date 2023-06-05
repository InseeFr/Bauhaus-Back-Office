package fr.insee.rmes.config.keycloak;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class ServerZone {

    private final String zone;

    private final Zone serverZone;

    static final Logger logger = LogManager.getLogger(ServerZone.class);

    public ServerZone(String zone) {
        this.zone = zone;
        serverZone= Arrays.stream(Zone.values())
                .filter(z->zone.toUpperCase().equals(z.name()))
                .findFirst()
                .orElseGet(()->{
                    logger.warn("No zone found for value "+zone+" : this is serverZone set to default zone");
                    return Zone.defaultZone();
                });
    }

    public static ServerZone defaultZone(){
        return new ServerZone(Zone.defaultZone().name());
    }

    public Zone serverZone() {
        return serverZone;
    }

    @Override
    public String toString() {
        return "ServerZone{" +
                "zone=" + serverZone +
                '}';
    }

    public enum Zone {
        INTERNE, DMZ;

        public static Zone defaultZone() {
            return INTERNE;
        }
    }
}
