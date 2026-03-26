package fr.insee.rmes.modules.clientconfig.domain.model;

import java.util.List;

public record ClientConfigProperties(
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
}
