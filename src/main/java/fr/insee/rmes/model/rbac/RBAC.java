package fr.insee.rmes.model.rbac;

public class RBAC {
    public enum Module {
        CONCEPT,
        COLLECTION,
        FAMILY,
        SERIE,
        OPERATION,
        INDICATOR,
        SIMS,
        CLASSIFICATION,
        DATASET
    }

    public enum Privilege {
        CREATE,
        READ,
        UPDATE,
        DELETE,
        PUBLISH,
        VALIDATE
    }

    public enum Strategy {
        ALL, STAMP
    }
}
