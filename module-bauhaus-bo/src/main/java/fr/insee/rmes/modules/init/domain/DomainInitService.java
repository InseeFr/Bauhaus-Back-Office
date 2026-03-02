package fr.insee.rmes.modules.init.domain;

import fr.insee.rmes.BauhausConfiguration;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaConfiguration;
import fr.insee.rmes.modules.init.domain.model.InitProperties;
import fr.insee.rmes.modules.init.domain.port.clientside.InitService;

import java.util.List;

/**
 * Domain service responsible for building initialization properties.
 * Determines the authentication type based on the environment and aggregates
 * configuration from multiple sources.
 */
public class DomainInitService implements InitService {

    private final BauhausConfiguration bauhausConfiguration;
    private final String maxLengthScopeNote;
    private final String defaultContributor;
    private final List<String> extraMandatoryFields;
    private final ColecticaConfiguration colecticaConfiguration;

    public DomainInitService(
            BauhausConfiguration bauhausConfiguration,
            String maxLengthScopeNote,
            String defaultContributor,
            List<String> extraMandatoryFields,
            ColecticaConfiguration colecticaConfiguration) {
        this.bauhausConfiguration = bauhausConfiguration;
        this.maxLengthScopeNote = maxLengthScopeNote;
        this.defaultContributor = defaultContributor;
        this.extraMandatoryFields = extraMandatoryFields;
        this.colecticaConfiguration = colecticaConfiguration;
    }

    @Override
    public InitProperties getInitProperties() {
        return new InitProperties(
                bauhausConfiguration.appHost(),
                defaultContributor,
                maxLengthScopeNote,
                bauhausConfiguration.lg1(),
                bauhausConfiguration.lg2(),
                getAuthType(bauhausConfiguration.env()),
                bauhausConfiguration.activeModules(),
                bauhausConfiguration.modules(),
                bauhausConfiguration.version(),
                extraMandatoryFields,
                colecticaConfiguration.server().defaultAgencyId(),
                colecticaConfiguration.langs(),
                bauhausConfiguration.enableDevTools()
        );
    }

    private String getAuthType(String env) {
        if (env.equals("pre-prod") || env.equals("prod") || env.equals("PROD")) {
            return "OpenIDConnectAuth";
        }
        return "NoAuthImpl";
    }
}
