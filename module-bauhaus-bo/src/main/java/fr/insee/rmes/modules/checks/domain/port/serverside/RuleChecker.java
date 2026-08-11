package fr.insee.rmes.modules.checks.domain.port.serverside;

import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;

import fr.insee.rmes.modules.checks.domain.model.CheckResult;

import java.util.Optional;

@ServerSidePort
public interface RuleChecker {
    Optional<CheckResult> check();
}