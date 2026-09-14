package fr.insee.rmes.colectica.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ColecticaItemOptions(
        @JsonProperty("namedOptions") List<String> namedOptions) {
    public static ColecticaItemOptions registerOrReplace() {
        return new ColecticaItemOptions(List.of("RegisterOrReplace"));
    }
}
