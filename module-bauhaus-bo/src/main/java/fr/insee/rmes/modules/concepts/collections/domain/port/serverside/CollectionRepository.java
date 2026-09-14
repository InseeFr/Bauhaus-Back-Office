package fr.insee.rmes.modules.concepts.collections.domain.port.serverside;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.model.concepts.Collection;
import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;

@ServerSidePort
public interface CollectionRepository {
    String save(Collection collection) throws RmesException;
}
