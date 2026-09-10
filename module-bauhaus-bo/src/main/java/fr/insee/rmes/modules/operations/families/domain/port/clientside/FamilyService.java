package fr.insee.rmes.modules.operations.families.domain.port.clientside;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;
import fr.insee.rmes.modules.operations.families.domain.exceptions.FamilyAlreadyPublishedException;
import fr.insee.rmes.modules.operations.families.domain.exceptions.FamilyNotFoundException;
import fr.insee.rmes.modules.operations.families.domain.exceptions.FamilyPrefLabelAlreadyUsedException;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamily;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamilySeriesWithReport;
import fr.insee.rmes.modules.operations.families.domain.model.PartialOperationFamily;
import fr.insee.rmes.modules.operations.families.domain.model.commands.CreateFamilyCommand;
import fr.insee.rmes.modules.operations.families.domain.model.commands.UpdateFamilyCommand;
import java.util.List;

@ClientSidePort
public interface FamilyService {
    List<PartialOperationFamily> getFamilies() throws RmesException;

    OperationFamily getFamily(String id) throws RmesException;

    /** Séries de la famille qui portent déjà un rapport de métadonnées. */
    List<OperationFamilySeriesWithReport> getSeriesWithReport(String id) throws RmesException;

    /** @return l'identifiant généré pour la nouvelle famille */
    String createFamily(CreateFamilyCommand command) throws RmesException, FamilyPrefLabelAlreadyUsedException;

    void updateFamily(UpdateFamilyCommand command)
            throws RmesException, FamilyNotFoundException, FamilyPrefLabelAlreadyUsedException;

    void validateFamily(String id) throws RmesException, FamilyAlreadyPublishedException;
}
