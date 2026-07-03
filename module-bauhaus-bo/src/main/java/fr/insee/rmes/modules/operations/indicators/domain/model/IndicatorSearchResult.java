package fr.insee.rmes.modules.operations.indicators.domain.model;

import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;

import org.jspecify.annotations.Nullable;

import java.util.List;

public record IndicatorSearchResult(
        IndicatorId id,
        List<LocalisedLabel> prefLabels,
        List<LocalisedLabel> altLabels,
        List<LocalisedLabel> abstracts,
        List<LocalisedLabel> historyNotes,
        @Nullable String accrualPeriodicityCode,
        @Nullable String accrualPeriodicityList,
        List<OrganizationLink> publishers,
        List<OrganizationLink> dataCollectors,
        List<String> creators,
        @Nullable String idSims,
        @Nullable ValidationStatus validationState
) {
}
