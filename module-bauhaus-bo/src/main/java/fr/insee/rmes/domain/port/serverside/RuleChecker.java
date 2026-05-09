package fr.insee.rmes.domain.port.serverside;

import fr.insee.rmes.domain.model.checks.CheckResult;

import java.util.Optional;

public interface RuleChecker {
    Optional<CheckResult> check();
}