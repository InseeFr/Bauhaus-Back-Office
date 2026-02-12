package fr.insee.rmes.modules.init.domain.model;

import java.util.List;

/**
 * Domain model representing application initialization properties.
 * These properties are exposed to the frontend via the /init endpoint to configure the application.
 *
 * @param appHost Application host URL
 * @param defaultContributor Default contributor identifier
 * @param maxLengthScopeNote Maximum length for scope notes
 * @param lg1 Primary language code
 * @param lg2 Secondary language code
 * @param authType Authentication type (OpenIDConnectAuth or NoAuthImpl)
 * @param activeModules List of active modules in the application
 * @param modules List of all available modules
 * @param version Application version
 * @param extraMandatoryFields Additional mandatory fields for validation
 * @param defaultAgencyId Default agency identifier for Colectica
 * @param colecticaLangs Supported languages for Colectica integration
 */
public record InitProperties(
        String appHost,
        String defaultContributor,
        String maxLengthScopeNote,
        String lg1,
        String lg2,
        String authType,
        List<String> activeModules,
        List<String> modules,
        String version,
        List<String> extraMandatoryFields,
        String defaultAgencyId,
        List<String> colecticaLangs
) {
}
