package fr.insee.rmes.keycloak;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ServerZone {

    private Zone zone;

    static final Logger logger = LoggerFactory.getLogger(ServerZone.class);

    public ServerZone(){ }

    public ServerZone(String zone) {
        this.zone = Arrays.stream(Zone.values())
                .filter(z->zone.toUpperCase().equals(z.name()))
                .findFirst()
                .orElseGet(()->{
                    logger.warn("No zone found for value {} : this is serverZone set to default zone", zone);
                    return Zone.defaultZone();
                });
    }

    public void setZone(String zone) {
        this.zone = Arrays.stream(Zone.values())
                .filter(z->zone.toUpperCase().equals(z.name()))
                .findFirst()
                .orElseGet(()->{
                    logger.warn("No zone found for value {} : this is serverZone set to default zone", zone);
                    return Zone.defaultZone();
                });
    }



    public static ServerZone defaultZone(){
        return new ServerZone(Zone.defaultZone().name());
    }

    public Zone zone() {
        return zone;
    }

    @Override
    public String toString() {
        return "ServerZone{" +
                "zone=" + zone +
                '}';
    }

    public enum Zone {
        INTERNE, DMZ;

        public static Zone defaultZone() {
            return INTERNE;
        }
    }
}
