package fr.insee.rmes.modules.init.domain;

import fr.insee.rmes.modules.init.domain.model.InitProperties;
import fr.insee.rmes.modules.init.domain.port.clientside.InitService;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaConfiguration;

import java.util.List;

/**
 * Domain service responsible for building initialization properties.
 * Determines the authentication type based on the environment and aggregates
 * configuration from multiple sources.
 */
public class DomainInitService implements InitService {

    private final String env;
    private final String lg2;
    private final String lg1;
    private final String maxLengthScopeNote;
    private final String defaultContributor;
    private final String appHost;
    private final List<String> activeModules;
    private final List<String> modules;
    private final String version;
    private final List<String> extraMandatoryFields;
    private final ColecticaConfiguration colecticaConfiguration;

    public DomainInitService(
            String env,
            String lg1,
            String lg2,
            String maxLengthScopeNote,
            String defaultContributor,
            String appHost,
            List<String> activeModules,
            List<String> modules,
            String version,
            List<String> extraMandatoryFields,
            ColecticaConfiguration colecticaConfiguration) {
        this.env = env;
        this.lg2 = lg2;
        this.lg1 = lg1;
        this.maxLengthScopeNote = maxLengthScopeNote;
        this.defaultContributor = defaultContributor;
        this.appHost = appHost;
        this.activeModules = activeModules;
        this.modules = modules;
        this.version = version;
        this.extraMandatoryFields = extraMandatoryFields;
        this.colecticaConfiguration = colecticaConfiguration;
    }

    @Override
    public InitProperties getInitProperties() {
        return new InitProperties(
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
                colecticaConfiguration.server().defaultAgencyId(),
                colecticaConfiguration.langs()
        );
    }

    private String getAuthType(String env) {
        if (env.equals("pre-prod") || env.equals("prod") || env.equals("PROD")) {
            return "OpenIDConnectAuth";
        }
        return "NoAuthImpl";
    }
}
