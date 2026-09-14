package fr.insee.rmes.utils;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public record IdGenerator() {
    public String generateNextId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
