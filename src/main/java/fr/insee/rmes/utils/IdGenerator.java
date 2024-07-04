package fr.insee.rmes.utils;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public record IdGenerator() {
    public String generateNextId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

}