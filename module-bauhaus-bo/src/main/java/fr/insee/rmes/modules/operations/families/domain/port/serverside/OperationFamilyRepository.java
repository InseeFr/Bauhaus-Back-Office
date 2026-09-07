package fr.insee.rmes.modules.operations.families.domain.port.serverside;

import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;


import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamily;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamilySeries;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamilySeriesWithReport;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamilySubject;
import fr.insee.rmes.modules.operations.families.domain.model.PartialOperationFamily;
import fr.insee.rmes.modules.shared_kernel.domain.model.Language;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;

import java.util.List;

@ServerSidePort
public interface OperationFamilyRepository {
    List<PartialOperationFamily> getFamilies() throws RmesException;
    OperationFamily getFullFamily(String id) throws RmesException;
    OperationFamily getFamily(String id) throws RmesException;
    List<OperationFamilySeries> getFamilySeries(String id) throws RmesException;
    List<OperationFamilySubject> getFamilySubjects(String id) throws RmesException;

    List<OperationFamilySeriesWithReport> getSeriesWithReport(String id) throws RmesException;

    /** Identifiant de la prochaine famille, partagé avec les séries, opérations et indicateurs. */
    String generateId() throws RmesException;

    boolean exists(String id) throws RmesException;

    /** Vrai si une <em>autre</em> famille porte déjà ce libellé dans cette langue. */
    boolean isPrefLabelAlreadyUsed(String id, String prefLabel, Language language) throws RmesException;

    ValidationStatus getValidationStatus(String id) throws RmesException;

    /** Écrit la famille dans le graphe de gestion, en écrasant sa version précédente. */
    void save(OperationFamily family) throws RmesException;

    /** Recopie la famille dans le graphe de publication et la passe à l'état {@code Validated}. */
    void publish(String id) throws RmesException;
}