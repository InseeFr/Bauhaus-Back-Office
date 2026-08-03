package fr.insee.rmes.modules.ddi.physical_instances.domain.exceptions;

/**
 * Exception thrown when a physical instance has no related study unit,
 * e.g. when resolving the parents of a freshly duplicated physical
 * instance that has not been attached to a study unit yet.
 */
public class StudyUnitNotFoundException extends RuntimeException {
    public StudyUnitNotFoundException(String message) {
        super(message);
    }
}
