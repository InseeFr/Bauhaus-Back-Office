package fr.insee.rmes.modules.operations.indicators.domain.model.commands;

import fr.insee.rmes.modules.operations.indicators.domain.exceptions.InvalidIndicatorCommandException;
import fr.insee.rmes.modules.operations.indicators.domain.exceptions.InvalidIndicatorIdException;
import fr.insee.rmes.modules.operations.indicators.domain.model.IndicatorId;
import fr.insee.rmes.modules.operations.indicators.domain.model.OperationLink;
import fr.insee.rmes.modules.operations.indicators.domain.model.OrganizationLink;
import fr.insee.rmes.modules.shared_kernel.domain.model.LocalisedLabel;

import org.jspecify.annotations.Nullable;

import java.util.List;

public class UpdateIndicatorCommand extends CreateIndicatorCommand {
    private final IndicatorId id;

    public UpdateIndicatorCommand(String id,
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
                                   @Nullable String idSims) throws InvalidIndicatorCommandException, InvalidIndicatorIdException {
        super(prefLabels, altLabels, abstracts, historyNotes, accrualPeriodicityCode, accrualPeriodicityList,
                publishers, contributors, creators, seeAlso, replaces, isReplacedBy, wasGeneratedBy, idSims);
        this.id = new IndicatorId(id);
    }

    public IndicatorId id() {
        return id;
    }
}
