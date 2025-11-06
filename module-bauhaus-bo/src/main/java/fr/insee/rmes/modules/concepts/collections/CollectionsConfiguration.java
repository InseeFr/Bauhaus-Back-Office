package fr.insee.rmes.modules.concepts.collections;

import fr.insee.rmes.modules.concepts.collections.domain.DomainCollectionsService;
import fr.insee.rmes.modules.concepts.collections.domain.port.clientside.CollectionService;
import fr.insee.rmes.modules.concepts.collections.domain.port.serverside.CollectionsRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CollectionsConfiguration {
    @Bean
    CollectionService getCollectionService(CollectionsRepository repository){
        return new DomainCollectionsService(repository);
    }
}
