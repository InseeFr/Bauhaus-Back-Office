package fr.insee.rmes.modules.checks;

import fr.insee.rmes.modules.checks.domain.DomainCheckerService;
import fr.insee.rmes.modules.checks.domain.port.clientside.CheckerService;
import fr.insee.rmes.modules.checks.domain.port.serverside.RuleChecker;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChecksConfiguration {
    @Bean
    CheckerService checkService(List<RuleChecker> checkers) {
        return new DomainCheckerService(checkers);
    }
}
