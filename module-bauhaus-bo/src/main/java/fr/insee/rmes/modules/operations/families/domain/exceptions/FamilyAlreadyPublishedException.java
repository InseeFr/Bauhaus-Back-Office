package fr.insee.rmes.modules.operations.families.domain.exceptions;

public class FamilyAlreadyPublishedException extends Exception {

    private final String id;

    public FamilyAlreadyPublishedException(String id) {
        super("Family " + id + " is already published");
        this.id = id;
    }

    public String id() {
        return id;
    }
}
