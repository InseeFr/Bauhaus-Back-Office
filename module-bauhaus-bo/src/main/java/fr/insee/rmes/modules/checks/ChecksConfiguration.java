package fr.insee.rmes.modules.checks;

import fr.insee.rmes.modules.checks.domain.port.clientside.CheckerService;
import fr.insee.rmes.modules.checks.domain.port.serverside.RuleChecker;
import fr.insee.rmes.modules.checks.domain.DomainCheckerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChecksConfiguration {
    @Bean
    CheckerService checkService(List<RuleChecker> checkers){
        return new DomainCheckerService(checkers);
    }
}
