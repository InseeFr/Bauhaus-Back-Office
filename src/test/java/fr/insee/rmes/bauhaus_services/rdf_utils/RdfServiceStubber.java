package fr.insee.rmes.bauhaus_services.rdf_utils;

public record RdfServiceStubber(RdfService rdfService) {

    public void injectRepoGestion(RepositoryGestion repoGestion) {
        rdfService.repoGestion=repoGestion;
    }
}
