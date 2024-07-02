package fr.insee.rmes.utils;

import java.util.UUID;

public class IdGenerator {
    public static String generateNextId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}