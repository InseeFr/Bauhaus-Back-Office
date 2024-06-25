package fr.insee.rmes.utils;

import fr.insee.rmes.bauhaus_services.Constants;
import org.json.JSONObject;

import java.util.UUID;

public class IdGenerator {
    public static String generateNextId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}