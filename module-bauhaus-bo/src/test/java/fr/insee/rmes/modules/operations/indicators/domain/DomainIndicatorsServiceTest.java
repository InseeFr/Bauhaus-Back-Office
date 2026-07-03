package fr.insee.rmes.modules.operations.indicators.domain;

import fr.insee.rmes.modules.operations.indicators.domain.exceptions.IndicatorNotFoundException;
import fr.insee.rmes.modules.operations.indicators.domain.exceptions.IndicatorsFetchException;
import fr.insee.rmes.modules.operations.indicators.domain.exceptions.InvalidIndicatorCommandException;
import fr.insee.rmes.modules.operations.indicators.domain.model.Indicator;
import fr.insee.rmes.modules.operations.indicators.domain.model.IndicatorId;
import fr.insee.rmes.modules.operations.indicators.domain.model.IndicatorSearchResult;
import fr.insee.rmes.modules.operations.indicators.domain.model.IndicatorWithSims;
import fr.insee.rmes.modules.operations.indicators.domain.model.OperationLink;
import fr.insee.rmes.modules.operations.indicators.domain.model.PartialIndicator;
import fr.insee.rmes.modules.operations.indicators.domain.model.commands.CreateIndicatorCommand;
import fr.insee.rmes.modules.operations.indicators.domain.model.commands.UpdateIndicatorCommand;
import fr.insee.rmes.modules.operations.indicators.domain.port.serverside.IndicatorsRepository;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DomainIndicatorsServiceTest {

    static final IndicatorId ID = new IndicatorId("p1651");
    static final List<OperationLink> A_SERIES_LINK = List.of(new OperationLink("s1", "series", List.of()));
    static final LocalisedLabel PREF_LABEL = LocalisedLabel.ofDefaultLanguage("Indicator label");

    static final Indicator INDICATOR = new Indicator(
            ID,
            List.of(PREF_LABEL),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            null,
            null,
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            A_SERIES_LINK,
            null,
            LocalDateTime.of(2024, 1, 1, 0, 0),
            null,
            ValidationStatus.UNPUBLISHED
    );

    IndicatorsRepository repository;
    DomainIndicatorsService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(IndicatorsRepository.class);
        service = new DomainIndicatorsService(repository);
    }

    @Test
    void all_indicators_should_be_returned() throws Throwable {
        PartialIndicator partial = new PartialIndicator(ID, PREF_LABEL, null);
        when(repository.getIndicators()).thenReturn(List.of(partial));

        List<PartialIndicator> result = service.getAllIndicators();

        assertThat(result).containsExactly(partial);
    }

    @Test
    void found_indicator_should_be_returned() throws Throwable {
        when(repository.getIndicator(ID)).thenReturn(Optional.of(INDICATOR));

        Optional<Indicator> result = service.getIndicator(ID);

        assertThat(result).contains(INDICATOR);
    }

    @Test
    void indicator_id_should_be_returned_when_indicator_is_created() throws Throwable {
        when(repository.existsWithPrefLabel(eq(PREF_LABEL), eq(null))).thenReturn(false);
        when(repository.nextId()).thenReturn(ID);
        CreateIndicatorCommand command = new CreateIndicatorCommand(
                List.of(PREF_LABEL),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                A_SERIES_LINK,
                null
        );

        IndicatorId createdId = service.createIndicator(command);

        verify(repository, times(1)).save(any());
        assertThat(createdId).isEqualTo(ID);
    }

    @Test
    void creation_should_fail_when_prefLabel_already_exists() throws Throwable {
        when(repository.existsWithPrefLabel(eq(PREF_LABEL), eq(null))).thenReturn(true);
        CreateIndicatorCommand command = createCommand();

        assertThrows(InvalidIndicatorCommandException.class, () -> service.createIndicator(command));
        verify(repository, never()).save(any());
    }

    @Test
    void indicator_should_be_updated() throws Throwable {
        when(repository.existsWithPrefLabel(eq(PREF_LABEL), eq(ID))).thenReturn(false);
        when(repository.getIndicator(ID)).thenReturn(Optional.of(INDICATOR));
        UpdateIndicatorCommand command = updateCommand();

        service.updateIndicator(command);

        verify(repository, times(1)).update(any());
    }

    @Test
    void update_should_fail_when_indicator_does_not_exist() throws Throwable {
        when(repository.existsWithPrefLabel(eq(PREF_LABEL), eq(ID))).thenReturn(false);
        when(repository.getIndicator(ID)).thenReturn(Optional.empty());
        UpdateIndicatorCommand command = updateCommand();

        assertThrows(IndicatorNotFoundException.class, () -> service.updateIndicator(command));
        verify(repository, never()).update(any());
    }

    @Test
    void update_should_fail_when_prefLabel_already_exists() throws Throwable {
        when(repository.existsWithPrefLabel(eq(PREF_LABEL), eq(ID))).thenReturn(true);
        UpdateIndicatorCommand command = updateCommand();

        assertThrows(InvalidIndicatorCommandException.class, () -> service.updateIndicator(command));
        verify(repository, never()).update(any());
    }

    @Test
    void indicator_should_be_validated() throws Throwable {
        when(repository.getIndicator(ID)).thenReturn(Optional.of(INDICATOR));

        service.validateIndicator(ID);

        verify(repository, times(1)).validate(ID);
    }

    @Test
    void validation_should_fail_when_indicator_does_not_exist() throws Throwable {
        when(repository.getIndicator(ID)).thenReturn(Optional.empty());

        assertThrows(IndicatorNotFoundException.class, () -> service.validateIndicator(ID));
        verify(repository, never()).validate(any());
    }

    @Test
    void indicators_with_sims_should_be_returned() throws Throwable {
        IndicatorWithSims withSims = new IndicatorWithSims(PREF_LABEL, "1234");
        when(repository.getIndicatorsWithSims()).thenReturn(List.of(withSims));

        List<IndicatorWithSims> result = service.getIndicatorsWithSims();

        assertThat(result).containsExactly(withSims);
    }

    @Test
    void indicators_for_search_should_be_returned() throws Throwable {
        IndicatorSearchResult searchResult = new IndicatorSearchResult(
                ID, List.of(PREF_LABEL), List.of(), List.of(), List.of(),
                null, null, List.of(), List.of(), List.of(), null, ValidationStatus.UNPUBLISHED
        );
        when(repository.getIndicatorsForSearch()).thenReturn(List.of(searchResult));

        List<IndicatorSearchResult> result = service.getIndicatorsForSearch();

        assertThat(result).containsExactly(searchResult);
    }

    private CreateIndicatorCommand createCommand() {
        try {
            return new CreateIndicatorCommand(
                    List.of(PREF_LABEL),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    null,
                    null,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    A_SERIES_LINK,
                    null
            );
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private UpdateIndicatorCommand updateCommand() {
        try {
            return new UpdateIndicatorCommand(
                    ID.value(),
                    List.of(PREF_LABEL),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    null,
                    null,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    A_SERIES_LINK,
                    null
            );
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
