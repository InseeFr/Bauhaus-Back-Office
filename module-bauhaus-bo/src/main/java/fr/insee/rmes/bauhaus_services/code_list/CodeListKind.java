package fr.insee.rmes.bauhaus_services.code_list;

public enum CodeListKind {
    FULL,
    PARTIAL;

    public boolean isPartial() {
        return this == PARTIAL;
    }
}
