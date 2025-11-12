package fr.insee.rmes.modules.concepts.collection;

import fr.insee.rmes.modules.concepts.collection.domain.DomainCollectionsService;
import fr.insee.rmes.modules.concepts.collection.domain.port.clientside.CollectionsService;
import fr.insee.rmes.modules.concepts.collection.domain.port.serverside.CollectionsRepository;
import fr.insee.rmes.modules.concepts.collection.domain.RandomIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CollectionsConfiguration {
    @Bean
    CollectionsService getCollectionService(CollectionsRepository repository) {
        return new DomainCollectionsService(repository,  new RandomIdGenerator());
    }
}
