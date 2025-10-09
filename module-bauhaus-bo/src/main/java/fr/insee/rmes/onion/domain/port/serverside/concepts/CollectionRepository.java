package fr.insee.rmes.onion.domain.port.serverside.concepts;

import fr.insee.rmes.model.concepts.Collection;
import fr.insee.rmes.domain.exceptions.RmesException;

public interface CollectionRepository {
    String save(Collection collection) throws RmesException;

}
