package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.exceptions.InvalidColecticaConfigurationException;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

/**
 * Configuration properties for Colectica API integration.
 *
 * <p>This configuration class manages all settings related to connecting to
 * and interacting with the Colectica metadata repository, including server
 * connection details and the list of mutualized code lists exposed to the UI.
 *
 * <p>Configuration properties are prefixed with {@code fr.insee.rmes.bauhaus.colectica}.
 *
 * <p>Example configuration in properties file:
 * <pre>
 * # Server configuration
 * fr.insee.rmes.bauhaus.colectica.server.baseUrl = https://colectica.example.com
 * fr.insee.rmes.bauhaus.colectica.server.apiPath = /api/v1/
 * fr.insee.rmes.bauhaus.colectica.server.username = myuser
 * fr.insee.rmes.bauhaus.colectica.server.password = mypassword
 *
 * # Mutualized code lists
 * fr.insee.rmes.bauhaus.colectica.mutualized-codes-lists[0].agency-id = fr.insee
 * fr.insee.rmes.bauhaus.colectica.mutualized-codes-lists[0].identifier = fc65a527-a04b-4505-85de-0a181e54dbad
 * fr.insee.rmes.bauhaus.colectica.mutualized-codes-lists[0].version = 1
 * </pre>
 *
 * @param langs List of supported language codes (e.g., "fr-FR", "en-GB")
 * @param server Configuration for the Colectica server instance
 * @param mutualizedCodesLists List of mutualized code lists exposed via the API. Empty or null
 *                             means no mutualized code list — the application starts normally.
 * @see ColecticaInstanceConfiguration
 * @see MutualizedCodeListEntry
 */
@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus.colectica")
public record ColecticaConfiguration(
        List<String> langs,
        ColecticaInstanceConfiguration server,
        List<MutualizedCodeListEntry> mutualizedCodesLists
) {
    public ColecticaConfiguration {
        if (langs == null || langs.isEmpty()) {
            throw new InvalidColecticaConfigurationException("langs cannot be null or empty");
        }

        langs.forEach(lang -> {
            if (!lang.matches("[a-z]{2}-[A-Z]{2}")) {
                throw new InvalidColecticaConfigurationException(
                    "Invalid language code: '%s'. Expected format: xx-XX (e.g., fr-FR, en-GB)".formatted(lang)
                );
            }
        });
    }

    /**
     * Configuration for a single Colectica instance
     */
    public record ColecticaInstanceConfiguration(
            String baseUrl,
            String apiPath,
            Map<String, String> itemTypes,
            String versionResponsibility,
            String itemFormat,
            String authenticationMode,
            String username,
            String password,
            String defaultAgencyId
    ) {
        public ColecticaInstanceConfiguration {
            if (apiPath == null || apiPath.isBlank()) {
                apiPath = "/api/v1/";
            }
            if (authenticationMode == null || authenticationMode.isBlank()) {
                authenticationMode = "password";
            }
        }

        public String baseServerUrl() {
            return baseUrl;
        }

        public String baseApiUrl() {
            return baseUrl + apiPath;
        }
    }

    /**
     * Represents a mutualized code list identifier to be fetched from Colectica.
     *
     * <p>Configuration example:
     * <pre>
     * fr.insee.rmes.bauhaus.colectica.mutualized-codes-lists[0].agency-id = fr.insee
     * fr.insee.rmes.bauhaus.colectica.mutualized-codes-lists[0].identifier = fc65a527-a04b-4505-85de-0a181e54dbad
     * fr.insee.rmes.bauhaus.colectica.mutualized-codes-lists[0].version = 1
     * </pre>
     *
     * @param agencyId Agency ID of the code list (e.g., "fr.insee")
     * @param identifier Identifier of the code list (UUID)
     * @param version Version of the code list
     */
    public record MutualizedCodeListEntry(
            String agencyId,
            String identifier,
            int version
    ) {}
}
