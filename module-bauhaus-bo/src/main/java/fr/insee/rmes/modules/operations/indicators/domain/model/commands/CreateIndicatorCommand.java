package fr.insee.rmes.modules.operations.indicators.domain.model.commands;

import fr.insee.rmes.modules.operations.indicators.domain.exceptions.InvalidIndicatorCommandException;
import fr.insee.rmes.modules.operations.indicators.domain.model.OperationLink;
import fr.insee.rmes.modules.operations.indicators.domain.model.OrganizationLink;
import fr.insee.rmes.modules.shared_kernel.domain.model.Lang;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CreateIndicatorCommand {
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

    public CreateIndicatorCommand(List<LocalisedLabel> prefLabels,
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
                                   @Nullable String idSims) throws InvalidIndicatorCommandException {
        if (prefLabels == null || prefLabels.isEmpty()) {
            throw new InvalidIndicatorCommandException("There are no prefLabels");
        }
        if (prefLabels.stream().noneMatch(l -> l.lang().equals(Lang.defaultLanguage()))) {
            throw new InvalidIndicatorCommandException("The default prefLabel is not provided");
        }
        if (wasGeneratedBy == null || wasGeneratedBy.isEmpty()) {
            throw new InvalidIndicatorCommandException("An indicator should be linked to a series.");
        }

        this.prefLabels = prefLabels;
        this.altLabels = Objects.requireNonNullElse(altLabels, List.of());
        this.abstracts = Objects.requireNonNullElse(abstracts, List.of());
        this.historyNotes = Objects.requireNonNullElse(historyNotes, List.of());
        this.accrualPeriodicityCode = accrualPeriodicityCode;
        this.accrualPeriodicityList = accrualPeriodicityList;
        this.publishers = Objects.requireNonNullElse(publishers, List.of());
        this.contributors = Objects.requireNonNullElse(contributors, List.of());
        this.creators = Objects.requireNonNullElse(creators, List.of());
        this.seeAlso = Objects.requireNonNullElse(seeAlso, List.of());
        this.replaces = Objects.requireNonNullElse(replaces, List.of());
        this.isReplacedBy = Objects.requireNonNullElse(isReplacedBy, List.of());
        this.wasGeneratedBy = wasGeneratedBy;
        this.idSims = idSims;
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
}
