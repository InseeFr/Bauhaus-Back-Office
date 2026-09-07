package fr.insee.rmes.modules.operations.families.domain.exceptions;

public class FamilyNotFoundException extends Exception {

    private final String id;

    public FamilyNotFoundException(String id) {
        super("Family " + id + " doesn't exist");
        this.id = id;
    }

    public String id() {
        return id;
    }
}
