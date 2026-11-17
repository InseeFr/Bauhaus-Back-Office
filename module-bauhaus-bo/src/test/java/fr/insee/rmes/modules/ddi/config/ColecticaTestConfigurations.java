package fr.insee.rmes.modules.ddi.config;

import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaConfiguration;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/** Configuration Colectica minimale partagée par les tests d'intégration des caches. */
final class ColecticaTestConfigurations {

    private ColecticaTestConfigurations() {}

    /** Instance {@code https://example.com}, paquet {@code fr.insee/pkg-1}, cache d'une heure. */
    static ColecticaConfiguration colecticaConfiguration(Map<String, String> itemTypes) {
        var server = new ColecticaConfiguration.ColecticaInstanceConfiguration(
                "https://example.com", "/api/v1/", itemTypes, "resp", "format", "password", "user", "pass", "fr.insee");
        return new ColecticaConfiguration(
                List.of("fr-FR"),
                server,
                new ColecticaConfiguration.PackageRef("fr.insee", "pkg-1", 1),
                Duration.ofHours(1));
    }
}
