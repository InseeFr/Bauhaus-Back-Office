package fr.insee.rmes.modules.clientconfig.webservice;

import fr.insee.rmes.modules.clientconfig.domain.model.ModuleConfig;
import fr.insee.rmes.modules.clientconfig.domain.model.ClientConfigProperties;

import java.util.List;

public record ClientConfigResponse(
        String appHost,
        String defaultContributor,
        String maxLengthScopeNote,
        String lg1,
        String lg2,
        String authType,
        List<ModuleConfig> modules,
        String version,
        List<String> extraMandatoryFields,
        String defaultAgencyId,
        List<String> colecticaLangs,
        boolean enableDevTools
) {
    public static ClientConfigResponse fromDomain(ClientConfigProperties properties) {
        return new ClientConfigResponse(
                properties.appHost(),
                properties.defaultContributor(),
                properties.maxLengthScopeNote(),
                properties.lg1(),
                properties.lg2(),
                properties.authType(),
                properties.modules(),
                properties.version(),
                properties.extraMandatoryFields(),
                properties.defaultAgencyId(),
                properties.colecticaLangs(),
                properties.enableDevTools()
        );
    }
}
