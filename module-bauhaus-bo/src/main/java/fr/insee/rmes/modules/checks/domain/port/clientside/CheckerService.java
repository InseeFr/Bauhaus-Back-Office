package fr.insee.rmes.modules.checks.domain.port.clientside;

import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;

import fr.insee.rmes.modules.checks.domain.model.CheckResult;

import java.util.List;

@ClientSidePort
public interface CheckerService {
    List<CheckResult> checks();
}
