package fr.insee.rmes.modules.concepts.collections.domain.exceptions;

import fr.insee.rmes.domain.exceptions.RmesException;

public class CollectionPublicationException extends Throwable {
    public CollectionPublicationException(RmesException e) {
        super(e);
    }
}
