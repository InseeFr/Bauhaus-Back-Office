package fr.insee.rmes.modules.codeslists;

import fr.insee.rmes.domain.codeslist.CodesListRepository;
import fr.insee.rmes.domain.codeslist.CodesListService;
import fr.insee.rmes.domain.codeslist.CodesListServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for CodesListService beans following hexagonal architecture.
 * This class is responsible for wiring domain services with their dependencies.
 */
@Configuration
public class CodesListConfiguration {
    
    /**
     * Creates the domain service bean, injecting the repository port.
     * 
     * @param codesListRepository The repository implementation (provided by infrastructure)
     * @return The domain service implementation
     */
    @Bean
    public CodesListService codesListService(CodesListRepository codesListRepository) {
        return new CodesListServiceImpl(codesListRepository);
    }
}