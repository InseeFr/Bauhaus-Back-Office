package fr.insee.rmes;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfServiceStubber;

public interface Stubber {
    static RdfServiceStubber forRdfService(RdfService rdfService) {
        return new RdfServiceStubber(rdfService);
    }
}

