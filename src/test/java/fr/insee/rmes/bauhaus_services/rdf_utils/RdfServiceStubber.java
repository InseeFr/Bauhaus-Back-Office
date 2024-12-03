package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;

public record RdfServiceStubber(RdfService rdfService) {

    public void injectRepoGestion(RepositoryGestion repoGestion) {
        rdfService.repoGestion=repoGestion;
    }

    public void injectStampsRestrictionsService(StampsRestrictionsService stampsRestrictionsService) {
        rdfService.stampsRestrictionsService=stampsRestrictionsService;
    }

    public void injectConfig(Config config) {
        rdfService.config=config;
    }
}

