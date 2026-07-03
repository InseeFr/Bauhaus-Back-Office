package fr.insee.rmes.modules.operations.indicators.domain.port.serverside;

import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;
import fr.insee.rmes.modules.operations.indicators.domain.exceptions.IndicatorsFetchException;
import fr.insee.rmes.modules.operations.indicators.domain.exceptions.IndicatorsSaveException;
import fr.insee.rmes.modules.operations.indicators.domain.model.Indicator;
import fr.insee.rmes.modules.operations.indicators.domain.model.IndicatorId;
import fr.insee.rmes.modules.operations.indicators.domain.model.IndicatorSearchResult;
import fr.insee.rmes.modules.operations.indicators.domain.model.IndicatorWithSims;
import fr.insee.rmes.modules.operations.indicators.domain.model.PartialIndicator;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@ServerSidePort
public interface IndicatorsRepository {
    List<PartialIndicator> getIndicators() throws IndicatorsFetchException;

    Optional<Indicator> getIndicator(IndicatorId id) throws IndicatorsFetchException;

    boolean existsWithPrefLabel(LocalisedLabel prefLabel, @Nullable IndicatorId excludingId) throws IndicatorsFetchException;

    IndicatorId nextId() throws IndicatorsFetchException;

    void save(Indicator indicator) throws IndicatorsSaveException;

    void update(Indicator indicator) throws IndicatorsSaveException;

    void validate(IndicatorId id) throws IndicatorsSaveException;

    List<IndicatorWithSims> getIndicatorsWithSims() throws IndicatorsFetchException;

    List<IndicatorSearchResult> getIndicatorsForSearch() throws IndicatorsFetchException;
}
