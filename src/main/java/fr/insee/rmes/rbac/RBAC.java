package fr.insee.rmes.rbac;

public class RBAC {
    public enum Module {
        CONCEPT_CONCEPT,
        CONCEPT_COLLECTION,
        CLASSIFICATION_FAMILY,
        CLASSIFICATION_SERIES,
        CLASSIFICATION_CLASSIFICATION,
        OPERATION_FAMILY,
        OPERATION_SERIES,
        OPERATION_OPERATION,
        OPERATION_INDICATOR,
        OPERATION_SIMS,
        OPERATION_DOCUMENT,
        STRUCTURE_STRUCTURE,
        STRUCTURE_COMPONENT,
        CODESLIST_CODESLIST,
        CODESLIST_PARTIALCODESLIST,
        DATASET_DATASET,
        DATASET_DISTRIBUTION,
        GEOGRAPHY,
        UNKNOWN,
    }

    public enum Privilege {
        CREATE,
        READ,
        UPDATE,
        DELETE,
        PUBLISH,
        UNKNOWN,
    }

    public enum Strategy {
        ALL, STAMP, NONE
    }
}
