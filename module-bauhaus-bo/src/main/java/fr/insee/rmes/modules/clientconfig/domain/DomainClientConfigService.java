package fr.insee.rmes.modules.clientconfig.domain;

import fr.insee.rmes.modules.clientconfig.domain.model.ModuleConfig;
import fr.insee.rmes.modules.clientconfig.domain.model.ClientConfigProperties;
import fr.insee.rmes.modules.clientconfig.domain.port.clientside.ClientConfigService;
import fr.insee.rmes.modules.shared_kernel.domain.model.ConfiguredLanguages;

import java.util.List;

public class DomainClientConfigService implements ClientConfigService {

    private final String appHost;
    private final String maxLengthScopeNote;
    private final String defaultContributor;
    private final ConfiguredLanguages languages;
    private final String env;
    private final List<ModuleConfig> modules;
    private final String version;
    private final List<String> extraMandatoryFields;
    private final String defaultAgencyId;
    private final List<String> colecticaLangs;
    private final boolean enableDevTools;

    public DomainClientConfigService(
            String appHost,
            String maxLengthScopeNote,
            String defaultContributor,
            ConfiguredLanguages languages,
            String env,
            List<ModuleConfig> modules,
            String version,
            List<String> extraMandatoryFields,
            String defaultAgencyId,
            List<String> colecticaLangs,
            boolean enableDevTools) {
        this.appHost = appHost;
        this.maxLengthScopeNote = maxLengthScopeNote;
        this.defaultContributor = defaultContributor;
        this.languages = languages;
        this.env = env;
        this.modules = modules;
        this.version = version;
        this.extraMandatoryFields = extraMandatoryFields;
        this.defaultAgencyId = defaultAgencyId;
        this.colecticaLangs = colecticaLangs;
        this.enableDevTools = enableDevTools;
    }

    @Override
    public ClientConfigProperties getClientConfigProperties() {
        return new ClientConfigProperties(
                appHost,
                defaultContributor,
                maxLengthScopeNote,
                languages.lg1(),
                languages.lg2(),
                getAuthType(env),
                modules,
                version,
                extraMandatoryFields,
                defaultAgencyId,
                colecticaLangs,
                enableDevTools
        );
    }

    private String getAuthType(String env) {
        if (env.equals("pre-prod") || env.equals("prod") || env.equals("PROD")) {
            return "OpenIDConnectAuth";
        }
        return "NoAuthImpl";
    }
}
