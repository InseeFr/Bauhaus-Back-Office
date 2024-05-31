package fr.insee.rmes.model;

public class RBAC {
    public enum APPLICATION {
        concept,
        collection,
        family,
        serie,
        operation,
        indicator,
        sims,
        classification
    }

    public enum PRIVILEGE {
        create,
        read,
        update,
        delete,
        publish,
        validate
    }

    public enum STRATEGY {
        ALL, STAMP
    }
}
