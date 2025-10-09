package fr.insee.rmes.onion.application;

import fr.insee.rmes.domain.port.clientside.CheckerService;
import fr.insee.rmes.domain.port.serverside.RuleChecker;
import fr.insee.rmes.domain.services.CheckerServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChecksConfiguration {
    @Bean
    CheckerService checkService(List<RuleChecker> checkers){
        return new CheckerServiceImpl(checkers);
    }
}
