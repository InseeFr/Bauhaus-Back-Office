package fr.insee.rmes.modules.operations.indicators.domain.port.clientside;

import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;
import fr.insee.rmes.modules.operations.indicators.domain.exceptions.IndicatorNotFoundException;
import fr.insee.rmes.modules.operations.indicators.domain.exceptions.IndicatorsFetchException;
import fr.insee.rmes.modules.operations.indicators.domain.exceptions.IndicatorsSaveException;
import fr.insee.rmes.modules.operations.indicators.domain.exceptions.InvalidIndicatorCommandException;
import fr.insee.rmes.modules.operations.indicators.domain.model.Indicator;
import fr.insee.rmes.modules.operations.indicators.domain.model.IndicatorId;
import fr.insee.rmes.modules.operations.indicators.domain.model.IndicatorSearchResult;
import fr.insee.rmes.modules.operations.indicators.domain.model.IndicatorWithSims;
import fr.insee.rmes.modules.operations.indicators.domain.model.PartialIndicator;
import fr.insee.rmes.modules.operations.indicators.domain.model.commands.CreateIndicatorCommand;
import fr.insee.rmes.modules.operations.indicators.domain.model.commands.UpdateIndicatorCommand;

import java.util.List;
import java.util.Optional;

@ClientSidePort
public interface IndicatorsService {
    List<PartialIndicator> getAllIndicators() throws IndicatorsFetchException;

    Optional<Indicator> getIndicator(IndicatorId id) throws IndicatorsFetchException;

    IndicatorId createIndicator(CreateIndicatorCommand command) throws IndicatorsSaveException, InvalidIndicatorCommandException, IndicatorsFetchException;

    void updateIndicator(UpdateIndicatorCommand command) throws IndicatorsSaveException, InvalidIndicatorCommandException, IndicatorNotFoundException, IndicatorsFetchException;

    void validateIndicator(IndicatorId id) throws IndicatorsSaveException, IndicatorNotFoundException, IndicatorsFetchException;

    List<IndicatorWithSims> getIndicatorsWithSims() throws IndicatorsFetchException;

    List<IndicatorSearchResult> getIndicatorsForSearch() throws IndicatorsFetchException;
}
