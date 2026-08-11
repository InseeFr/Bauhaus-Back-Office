package fr.insee.rmes.modules.checks.domain;

import fr.insee.rmes.modules.checks.domain.model.CheckResult;
import fr.insee.rmes.modules.checks.domain.port.clientside.CheckerService;
import fr.insee.rmes.modules.checks.domain.port.serverside.RuleChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class DomainCheckerService implements CheckerService  {
    private static final Logger logger = LoggerFactory.getLogger(DomainCheckerService.class);
    
    private final List<RuleChecker> checkers;

    public DomainCheckerService(List<RuleChecker> checkers) {
        this.checkers = checkers != null ? checkers : List.of();
    }

    @Override
    public List<CheckResult> checks() {
        logger.info("Starting execution of {} checkers", checkers.size());
        
        return checkers.stream()
                .map(this::safeCheck)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
    
    private Optional<CheckResult> safeCheck(RuleChecker checker) {
        try {
            logger.debug("Executing checker: {}", checker.getClass().getSimpleName());
            Optional<CheckResult> result = checker.check();
            
            if (result.isPresent()) {
                logger.debug("Checker {} returned result: {}", 
                           checker.getClass().getSimpleName(), result.get().getName());
            } else {
                logger.debug("Checker {} returned no result", checker.getClass().getSimpleName());
            }
            
            return result;
        } catch (Exception e) {
            logger.error("Error executing checker: {}", checker.getClass().getSimpleName(), e);
            return Optional.of(new CheckResult(
                    "error_" + checker.getClass().getSimpleName().toLowerCase(), 
                    "Check failed: " + e.getMessage()));
        }
    }
}
