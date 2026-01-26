package fr.insee.rmes.modules.concepts.collections;

import fr.insee.rmes.modules.concepts.collections.domain.DomainCollectionsService;
import fr.insee.rmes.modules.concepts.collections.domain.port.clientside.CollectionsService;
import fr.insee.rmes.modules.concepts.collections.domain.port.serverside.CollectionsPublicationRepository;
import fr.insee.rmes.modules.concepts.collections.domain.port.serverside.CollectionsRepository;
import fr.insee.rmes.modules.concepts.collections.domain.RandomIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CollectionsConfiguration {
    @Bean
    CollectionsService getCollectionService(CollectionsRepository repository, CollectionsPublicationRepository collectionsPublicationRepository) {
        return new DomainCollectionsService(repository, collectionsPublicationRepository, new RandomIdGenerator());
    }
}
