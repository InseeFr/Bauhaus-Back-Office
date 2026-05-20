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
 * connection details and the mutualized codes package whose tree is traversed
 * to expose code lists to the UI.
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
 * # Mutualized codes package (root of the tree to traverse)
 * fr.insee.rmes.bauhaus.colectica.mutualized-codes-package.agency-id = fr.insee
 * fr.insee.rmes.bauhaus.colectica.mutualized-codes-package.identifier = e129238f-485f-40b4-b52f-5702c05d5f40
 * fr.insee.rmes.bauhaus.colectica.mutualized-codes-package.version = 1
 * </pre>
 *
 * @param langs List of supported language codes (e.g., "fr-FR", "en-GB")
 * @param server Configuration for the Colectica server instance
 * @param mutualizedCodesPackage Root package whose tree is recursively traversed to collect
 *                               mutualized code lists. Null means no mutualized code list —
 *                               the application starts normally.
 * @see ColecticaInstanceConfiguration
 * @see PackageRef
 */
@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus.colectica")
public record ColecticaConfiguration(
        List<String> langs,
        ColecticaInstanceConfiguration server,
        PackageRef mutualizedCodesPackage
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
     * Reference to a Colectica item (typically the root package of mutualized codes).
     *
     * <p>Configuration example:
     * <pre>
     * fr.insee.rmes.bauhaus.colectica.mutualized-codes-package.agency-id = fr.insee
     * fr.insee.rmes.bauhaus.colectica.mutualized-codes-package.identifier = e129238f-485f-40b4-b52f-5702c05d5f40
     * fr.insee.rmes.bauhaus.colectica.mutualized-codes-package.version = 1
     * </pre>
     *
     * @param agencyId Agency ID of the item (e.g., "fr.insee")
     * @param identifier Identifier of the item (UUID)
     * @param version Version of the item
     */
    public record PackageRef(
            String agencyId,
            String identifier,
            int version
    ) {}
}
