package fr.insee.rmes.modules.codeslists.codeslists.domain.port.serverside;

import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.CodesListsFetchException;
import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.CodesListsSaveException;
import fr.insee.rmes.modules.codeslists.codeslists.domain.model.CodesList;
import fr.insee.rmes.modules.codeslists.codeslists.domain.model.PersistedCodesList;
import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;

import java.util.Optional;

@ServerSidePort
public interface CodesListsRepository {

    /**
     * Vrai si la notation, l'IRI de la liste ou celle de sa classe OWL est déjà prise.
     */
    boolean isIdentityAlreadyTaken(CodesList codesList) throws CodesListsFetchException;

    /**
     * Recherche par segment d'URI et non par notation : la notation est renommable, l'IRI non.
     */
    Optional<PersistedCodesList> findByUriSegment(String lastListUriSegment) throws CodesListsFetchException;

    void save(CodesList codesList) throws CodesListsSaveException;
}
