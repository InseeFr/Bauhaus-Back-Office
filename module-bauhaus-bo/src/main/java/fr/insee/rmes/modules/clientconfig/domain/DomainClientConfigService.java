package fr.insee.rmes.modules.clientconfig.domain;

import fr.insee.rmes.modules.clientconfig.domain.model.ClientConfigProperties;
import fr.insee.rmes.modules.clientconfig.domain.port.clientside.ClientConfigService;

import java.util.List;

public class DomainClientConfigService implements ClientConfigService {

    private final String appHost;
    private final String maxLengthScopeNote;
    private final String defaultContributor;
    private final String lg1;
    private final String lg2;
    private final String env;
    private final List<String> activeModules;
    private final List<String> modules;
    private final String version;
    private final List<String> extraMandatoryFields;
    private final String defaultAgencyId;
    private final List<String> colecticaLangs;
    private final boolean enableDevTools;

    public DomainClientConfigService(
            String appHost,
            String maxLengthScopeNote,
            String defaultContributor,
            String lg1,
            String lg2,
            String env,
            List<String> activeModules,
            List<String> modules,
            String version,
            List<String> extraMandatoryFields,
            String defaultAgencyId,
            List<String> colecticaLangs,
            boolean enableDevTools) {
        this.appHost = appHost;
        this.maxLengthScopeNote = maxLengthScopeNote;
        this.defaultContributor = defaultContributor;
        this.lg1 = lg1;
        this.lg2 = lg2;
        this.env = env;
        this.activeModules = activeModules;
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
                lg1,
                lg2,
                getAuthType(env),
                activeModules,
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
