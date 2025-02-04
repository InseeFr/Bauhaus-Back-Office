package fr.insee.rmes.model.concepts;

public record ConceptForAdvancedSearch(
        String id,
        String label,
        String created,
        String modified,
        String disseminationStatus,
        String validationStatus,
        String definition,
        String creator,
        String isTopConceptOf,
        String valid,
        String altLabel) {

    public static ConceptForAdvancedSearch appendLabel(ConceptForAdvancedSearch p1, ConceptForAdvancedSearch p2){
        return new ConceptForAdvancedSearch(
                p1.id(),
                p1.label(),
                p1.created(),
                p1.modified(),
                p1.disseminationStatus(),
                p1.validationStatus(),
                p1.definition(),
                p1.creator(),
                p1.isTopConceptOf(),
                p1.valid(),
                p1.altLabel() + " || " + p2.altLabel()
        );
    }
}

