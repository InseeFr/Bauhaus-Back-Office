package fr.insee.rmes.domain.port.clientside;

import fr.insee.rmes.domain.model.checks.CheckResult;

import java.util.List;

public interface CheckerService {
    List<CheckResult> checks();
}
