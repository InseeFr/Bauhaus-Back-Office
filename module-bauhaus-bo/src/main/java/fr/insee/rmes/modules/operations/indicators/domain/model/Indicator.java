package fr.insee.rmes.modules.operations.indicators.domain.model;

import fr.insee.rmes.modules.operations.indicators.domain.model.commands.CreateIndicatorCommand;
import fr.insee.rmes.modules.operations.indicators.domain.model.commands.UpdateIndicatorCommand;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class Indicator {

    private static final ValidationStatus DEFAULT_VALIDATION_STATE = ValidationStatus.UNPUBLISHED;

    private final IndicatorId id;
    private final List<LocalisedLabel> prefLabels;
    private final List<LocalisedLabel> altLabels;
    private final List<LocalisedLabel> abstracts;
    private final List<LocalisedLabel> historyNotes;
    private final @Nullable String accrualPeriodicityCode;
    private final @Nullable String accrualPeriodicityList;
    private final List<OrganizationLink> publishers;
    private final List<OrganizationLink> contributors;
    private final List<String> creators;
    private final List<OperationLink> seeAlso;
    private final List<OperationLink> replaces;
    private final List<OperationLink> isReplacedBy;
    private final List<OperationLink> wasGeneratedBy;
    private final @Nullable String idSims;
    private final LocalDateTime created;
    private final @Nullable LocalDateTime modified;
    private final ValidationStatus validationState;

    public Indicator(IndicatorId id,
                      List<LocalisedLabel> prefLabels,
                      List<LocalisedLabel> altLabels,
                      List<LocalisedLabel> abstracts,
                      List<LocalisedLabel> historyNotes,
                      @Nullable String accrualPeriodicityCode,
                      @Nullable String accrualPeriodicityList,
                      List<OrganizationLink> publishers,
                      List<OrganizationLink> contributors,
                      List<String> creators,
                      List<OperationLink> seeAlso,
                      List<OperationLink> replaces,
                      List<OperationLink> isReplacedBy,
                      List<OperationLink> wasGeneratedBy,
                      @Nullable String idSims,
                      LocalDateTime created,
                      @Nullable LocalDateTime modified,
                      ValidationStatus validationState) {
        this.id = id;
        this.prefLabels = prefLabels;
        this.altLabels = altLabels;
        this.abstracts = abstracts;
        this.historyNotes = historyNotes;
        this.accrualPeriodicityCode = accrualPeriodicityCode;
        this.accrualPeriodicityList = accrualPeriodicityList;
        this.publishers = publishers;
        this.contributors = contributors;
        this.creators = creators;
        this.seeAlso = seeAlso;
        this.replaces = replaces;
        this.isReplacedBy = isReplacedBy;
        this.wasGeneratedBy = wasGeneratedBy;
        this.idSims = idSims;
        this.created = created;
        this.modified = modified;
        this.validationState = validationState;
    }

    public static Indicator create(CreateIndicatorCommand command, IndicatorId id) {
        return new Indicator(
                id,
                command.prefLabels(),
                command.altLabels(),
                command.abstracts(),
                command.historyNotes(),
                command.accrualPeriodicityCode().orElse(null),
                command.accrualPeriodicityList().orElse(null),
                command.publishers(),
                command.contributors(),
                command.creators(),
                command.seeAlso(),
                command.replaces(),
                command.isReplacedBy(),
                command.wasGeneratedBy(),
                command.idSims().orElse(null),
                LocalDateTime.now(),
                null,
                DEFAULT_VALIDATION_STATE
        );
    }

    public static Indicator update(UpdateIndicatorCommand command, Indicator existing) {
        ValidationStatus newValidationState = existing.validationState == ValidationStatus.UNPUBLISHED
                ? ValidationStatus.UNPUBLISHED
                : ValidationStatus.MODIFIED;

        return new Indicator(
                existing.id,
                command.prefLabels(),
                command.altLabels(),
                command.abstracts(),
                command.historyNotes(),
                command.accrualPeriodicityCode().orElse(null),
                command.accrualPeriodicityList().orElse(null),
                command.publishers(),
                command.contributors(),
                command.creators(),
                command.seeAlso(),
                command.replaces(),
                command.isReplacedBy(),
                command.wasGeneratedBy(),
                command.idSims().orElse(null),
                existing.created,
                LocalDateTime.now(),
                newValidationState
        );
    }

    public IndicatorId id() {
        return id;
    }

    public List<LocalisedLabel> prefLabels() {
        return prefLabels;
    }

    public List<LocalisedLabel> altLabels() {
        return altLabels;
    }

    public List<LocalisedLabel> abstracts() {
        return abstracts;
    }

    public List<LocalisedLabel> historyNotes() {
        return historyNotes;
    }

    public Optional<String> accrualPeriodicityCode() {
        return Optional.ofNullable(accrualPeriodicityCode);
    }

    public Optional<String> accrualPeriodicityList() {
        return Optional.ofNullable(accrualPeriodicityList);
    }

    public List<OrganizationLink> publishers() {
        return publishers;
    }

    public List<OrganizationLink> contributors() {
        return contributors;
    }

    public List<String> creators() {
        return creators;
    }

    public List<OperationLink> seeAlso() {
        return seeAlso;
    }

    public List<OperationLink> replaces() {
        return replaces;
    }

    public List<OperationLink> isReplacedBy() {
        return isReplacedBy;
    }

    public List<OperationLink> wasGeneratedBy() {
        return wasGeneratedBy;
    }

    public Optional<String> idSims() {
        return Optional.ofNullable(idSims);
    }

    public LocalDateTime created() {
        return created;
    }

    public Optional<LocalDateTime> modified() {
        return Optional.ofNullable(modified);
    }

    public ValidationStatus validationState() {
        return validationState;
    }
}
