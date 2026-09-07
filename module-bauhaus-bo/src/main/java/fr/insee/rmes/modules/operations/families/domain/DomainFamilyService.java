package fr.insee.rmes.modules.operations.families.domain;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.operations.families.domain.exceptions.FamilyAlreadyPublishedException;
import fr.insee.rmes.modules.operations.families.domain.exceptions.FamilyNotFoundException;
import fr.insee.rmes.modules.operations.families.domain.exceptions.FamilyPrefLabelAlreadyUsedException;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamily;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamilySeriesWithReport;
import fr.insee.rmes.modules.operations.families.domain.model.PartialOperationFamily;
import fr.insee.rmes.modules.operations.families.domain.model.commands.CreateFamilyCommand;
import fr.insee.rmes.modules.operations.families.domain.model.commands.UpdateFamilyCommand;
import fr.insee.rmes.modules.operations.families.domain.port.clientside.FamilyService;
import fr.insee.rmes.modules.operations.families.domain.port.serverside.OperationFamilyRepository;
import fr.insee.rmes.modules.shared_kernel.domain.model.Language;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

public class DomainFamilyService implements FamilyService {
    static final Logger logger = LoggerFactory.getLogger(DomainFamilyService.class);

    private final OperationFamilyRepository operationFamilyRepository;
    private final Clock clock;

    public DomainFamilyService(OperationFamilyRepository operationFamilyRepository, Clock clock) {
        this.operationFamilyRepository = operationFamilyRepository;
        this.clock = clock;
    }

    @Override
    public List<PartialOperationFamily> getFamilies() throws RmesException {
        logger.info("Starting to get families list");
        return operationFamilyRepository.getFamilies();
    }

    @Override
    public OperationFamily getFamily(String id) throws RmesException {
        logger.info("Starting to get family");
        return operationFamilyRepository.getFullFamily(id);
    }

    @Override
    public List<OperationFamilySeriesWithReport> getSeriesWithReport(String id) throws RmesException {
        return operationFamilyRepository.getSeriesWithReport(id);
    }

    @Override
    public String createFamily(CreateFamilyCommand command) throws RmesException, FamilyPrefLabelAlreadyUsedException {
        String id = operationFamilyRepository.generateId();
        rejectAlreadyUsedPrefLabels(id, command.prefLabelLg1(), command.prefLabelLg2());

        operationFamilyRepository.save(OperationFamily.create(id, command, now()));
        logger.info("Create family : {} - {}", id, command.prefLabelLg1());
        return id;
    }

    @Override
    public void updateFamily(UpdateFamilyCommand command)
            throws RmesException, FamilyNotFoundException, FamilyPrefLabelAlreadyUsedException {
        if (!operationFamilyRepository.exists(command.id())) {
            throw new FamilyNotFoundException(command.id());
        }
        rejectAlreadyUsedPrefLabels(command.id(), command.prefLabelLg1(), command.prefLabelLg2());

        operationFamilyRepository.save(OperationFamily.update(command, nextValidationStatus(command.id()), now()));
        logger.info("Update family : {} - {}", command.id(), command.prefLabelLg1());
    }

    @Override
    public void validateFamily(String id) throws RmesException, FamilyAlreadyPublishedException {
        if (operationFamilyRepository.getValidationStatus(id) == ValidationStatus.VALIDATED) {
            throw new FamilyAlreadyPublishedException(id);
        }
        operationFamilyRepository.publish(id);
        logger.info("Validate family : {}", id);
    }

    private void rejectAlreadyUsedPrefLabels(String id, String prefLabelLg1, String prefLabelLg2)
            throws RmesException, FamilyPrefLabelAlreadyUsedException {
        if (operationFamilyRepository.isPrefLabelAlreadyUsed(id, prefLabelLg1, Language.lg1)) {
            throw new FamilyPrefLabelAlreadyUsedException(Language.lg1);
        }
        if (operationFamilyRepository.isPrefLabelAlreadyUsed(id, prefLabelLg2, Language.lg2)) {
            throw new FamilyPrefLabelAlreadyUsedException(Language.lg2);
        }
    }

    /**
     * Une famille republiée après édition repasse à {@code Modified}, pas à {@code Unpublished} :
     * le graphe de publication contient encore la version précédente.
     */
    private ValidationStatus nextValidationStatus(String id) throws RmesException {
        return operationFamilyRepository.getValidationStatus(id) == ValidationStatus.UNPUBLISHED
                ? ValidationStatus.UNPUBLISHED
                : ValidationStatus.MODIFIED;
    }

    private String now() {
        return LocalDateTime.now(clock).toString();
    }
}
