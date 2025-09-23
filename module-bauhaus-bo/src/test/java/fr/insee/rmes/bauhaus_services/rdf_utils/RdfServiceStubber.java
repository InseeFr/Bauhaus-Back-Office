package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.Config;
import fr.insee.rmes.infrastructure.rdf_utils.RepositoryGestion;

public record RdfServiceStubber(RdfService rdfService) {

    public void injectRepoGestion(RepositoryGestion repoGestion) {
        rdfService.repoGestion=repoGestion;
    }


    public void injectConfig(Config config) {
        rdfService.config=config;
    }
}

