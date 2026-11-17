package fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions;

import java.util.Map;

/**
 * Exception thrown when a physical instance has no related study unit,
 * e.g. when resolving the parents of a freshly duplicated physical
 * instance that has not been attached to a study unit yet.
 */
public class StudyUnitNotFoundException extends DdiItemNotFoundException {
    public StudyUnitNotFoundException(String message) {
        super(Code.DDI_STUDY_UNIT_NOT_FOUND, Map.of(), message);
    }
}
