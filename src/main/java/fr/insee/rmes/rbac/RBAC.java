package fr.insee.rmes.rbac;

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
