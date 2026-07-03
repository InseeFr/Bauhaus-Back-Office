package fr.insee.rmes.modules.operations.indicators.domain;

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
import fr.insee.rmes.modules.operations.indicators.domain.port.clientside.IndicatorsService;
import fr.insee.rmes.modules.operations.indicators.domain.port.serverside.IndicatorsRepository;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;

import java.util.List;
import java.util.Optional;

public class DomainIndicatorsService implements IndicatorsService {

    private final IndicatorsRepository repository;

    public DomainIndicatorsService(IndicatorsRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PartialIndicator> getAllIndicators() throws IndicatorsFetchException {
        return repository.getIndicators();
    }

    @Override
    public Optional<Indicator> getIndicator(IndicatorId id) throws IndicatorsFetchException {
        return repository.getIndicator(id);
    }

    @Override
    public IndicatorId createIndicator(CreateIndicatorCommand command) throws IndicatorsSaveException, InvalidIndicatorCommandException, IndicatorsFetchException {
        ensurePrefLabelsAreUnique(command.prefLabels(), null);

        IndicatorId id = repository.nextId();
        Indicator indicator = Indicator.create(command, id);
        repository.save(indicator);
        return id;
    }

    @Override
    public void updateIndicator(UpdateIndicatorCommand command) throws IndicatorsSaveException, InvalidIndicatorCommandException, IndicatorNotFoundException, IndicatorsFetchException {
        ensurePrefLabelsAreUnique(command.prefLabels(), command.id());

        Indicator existing = repository.getIndicator(command.id()).orElseThrow(IndicatorNotFoundException::new);
        Indicator updated = Indicator.update(command, existing);
        repository.update(updated);
    }

    @Override
    public void validateIndicator(IndicatorId id) throws IndicatorsSaveException, IndicatorNotFoundException, IndicatorsFetchException {
        repository.getIndicator(id).orElseThrow(IndicatorNotFoundException::new);
        repository.validate(id);
    }

    @Override
    public List<IndicatorWithSims> getIndicatorsWithSims() throws IndicatorsFetchException {
        return repository.getIndicatorsWithSims();
    }

    @Override
    public List<IndicatorSearchResult> getIndicatorsForSearch() throws IndicatorsFetchException {
        return repository.getIndicatorsForSearch();
    }

    private void ensurePrefLabelsAreUnique(List<LocalisedLabel> prefLabels, IndicatorId excludingId) throws InvalidIndicatorCommandException, IndicatorsFetchException {
        for (LocalisedLabel prefLabel : prefLabels) {
            if (repository.existsWithPrefLabel(prefLabel, excludingId)) {
                throw new InvalidIndicatorCommandException("This prefLabel is already used by another indicator.");
            }
        }
    }
}
