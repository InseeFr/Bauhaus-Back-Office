package fr.insee.rmes.modules.concepts.concept;

import fr.insee.rmes.modules.concepts.concept.domain.DomainConceptsService;
import fr.insee.rmes.modules.concepts.concept.domain.port.clientside.ConceptsService;
import fr.insee.rmes.modules.concepts.concept.domain.port.serverside.ConceptsRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConceptConfiguration {

    @Bean
    ConceptsService getConceptsService(ConceptsRepository repository) {
        return new DomainConceptsService(repository);
    }
}
