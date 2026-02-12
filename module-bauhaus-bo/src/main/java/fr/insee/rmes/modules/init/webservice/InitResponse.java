package fr.insee.rmes.modules.init.webservice;

import fr.insee.rmes.modules.init.domain.model.InitProperties;

import java.util.List;

/**
 * DTO representing the response of the /init endpoint.
 * This response contains all configuration properties needed by the frontend.
 */
public record InitResponse(
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
    public static InitResponse fromDomain(InitProperties properties) {
        return new InitResponse(
                properties.appHost(),
                properties.defaultContributor(),
                properties.maxLengthScopeNote(),
                properties.lg1(),
                properties.lg2(),
                properties.authType(),
                properties.activeModules(),
                properties.modules(),
                properties.version(),
                properties.extraMandatoryFields(),
                properties.defaultAgencyId(),
                properties.colecticaLangs()
        );
    }
}
