package fr.insee.rmes.modules.concepts.collections.domain.model;

import fr.insee.rmes.modules.concepts.collections.domain.exceptions.InvalidCollectionIdException;

public class CollectionId {
    private final String value;

    public CollectionId(String value) throws InvalidCollectionIdException {
        if(value == null){
            throw new InvalidCollectionIdException("The identifier is null");
        }
        if(value.isEmpty()){
            throw new InvalidCollectionIdException("The identifier is empty");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }
}
