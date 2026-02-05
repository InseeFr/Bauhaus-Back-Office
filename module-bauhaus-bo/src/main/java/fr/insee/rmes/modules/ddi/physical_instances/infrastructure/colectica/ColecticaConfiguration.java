package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties for Colectica API integration.
 *
 * <p>This configuration class manages all settings related to connecting to
 * and interacting with the Colectica metadata repository, including server
 * connection details and filtering rules.
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
 * # Deny list configuration
 * fr.insee.rmes.bauhaus.colectica.code-list-deny-list[0].agency-id = fr.insee
 * fr.insee.rmes.bauhaus.colectica.code-list-deny-list[0].id = 2a22ba00-a977-4a61-a582-99025c6b0582
 * </pre>
 *
 * @param server Configuration for the Colectica server instance
 * @param codeListDenyList List of code lists to exclude from results (optional)
 * @see ColecticaInstanceConfiguration
 * @see CodeListDenyEntry
 */
@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus.colectica")
public record ColecticaConfiguration(
        ColecticaInstanceConfiguration server,
        List<CodeListDenyEntry> codeListDenyList
) {
    /**
     * Configuration for a single Colectica instance
     */
    public record ColecticaInstanceConfiguration(
            String baseUrl,
            String apiPath,
            List<String> itemTypes,
            String versionResponsibility,
            String itemFormat,
            String username,
            String password,
            String defaultAgencyId
    ) {
        public ColecticaInstanceConfiguration {
            // Set default apiPath if not provided
            if (apiPath == null || apiPath.isBlank()) {
                apiPath = "/api/v1/";
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
     * Represents a code list entry to be excluded from query results.
     *
     * <p>Code lists matching both the agencyId and id will be filtered out
     * when retrieving code lists from the Colectica repository.
     *
     * <p>This is useful for excluding:
     * <ul>
     *   <li>Deprecated or obsolete code lists</li>
     *   <li>Test or temporary code lists</li>
     *   <li>Code lists that should not be visible in the application</li>
     * </ul>
     *
     * <p>Configuration example:
     * <pre>
     * # Exclude a specific code list
     * fr.insee.rmes.bauhaus.colectica.code-list-deny-list[0].agency-id = fr.insee
     * fr.insee.rmes.bauhaus.colectica.code-list-deny-list[0].id = 2a22ba00-a977-4a61-a582-99025c6b0582
     *
     * # Exclude another code list from different agency
     * fr.insee.rmes.bauhaus.colectica.code-list-deny-list[1].agency-id = other.agency
     * fr.insee.rmes.bauhaus.colectica.code-list-deny-list[1].id = another-id-to-exclude
     * </pre>
     *
     * @param agencyId Agency ID of the code list to exclude (e.g., "fr.insee").
     *                 Must not be null or empty.
     * @param id ID of the code list to exclude (e.g., "2a22ba00-a977-4a61-a582-99025c6b0582").
     *           Must not be null or empty.
     * @see ColecticaConfigurationValidator
     * @see DenyListFilter
     */
    public record CodeListDenyEntry(
            String agencyId,
            String id
    ) {}
}
