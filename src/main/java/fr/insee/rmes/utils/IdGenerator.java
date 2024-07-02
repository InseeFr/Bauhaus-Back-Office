package fr.insee.rmes.utils;

import java.util.UUID;

public class IdGenerator {
    public String generateNextId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public IdGenerator() {
    }
}