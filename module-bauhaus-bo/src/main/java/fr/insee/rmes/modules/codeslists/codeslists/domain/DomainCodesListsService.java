package fr.insee.rmes.modules.codeslists.codeslists.domain;

import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.CodesListAlreadyExistsException;
import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.CodesListIdMismatchException;
import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.CodesListNotFoundException;
import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.CodesListsFetchException;
import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.CodesListsSaveException;
import fr.insee.rmes.modules.codeslists.codeslists.domain.model.CodesList;
import fr.insee.rmes.modules.codeslists.codeslists.domain.model.CodesListId;
import fr.insee.rmes.modules.codeslists.codeslists.domain.model.PersistedCodesList;
import fr.insee.rmes.modules.codeslists.codeslists.domain.model.commands.CreateCodesListCommand;
import fr.insee.rmes.modules.codeslists.codeslists.domain.model.commands.UpdateCodesListCommand;
import fr.insee.rmes.modules.codeslists.codeslists.domain.port.clientside.CodesListsService;
import fr.insee.rmes.modules.codeslists.codeslists.domain.port.serverside.CodesListsRepository;

public class DomainCodesListsService implements CodesListsService {

    private final CodesListsRepository repository;

    public DomainCodesListsService(CodesListsRepository repository) {
        this.repository = repository;
    }

    @Override
    public CodesListId create(CreateCodesListCommand command)
            throws CodesListAlreadyExistsException, CodesListsFetchException, CodesListsSaveException {
        CodesList codesList = CodesList.create(command);
        if (repository.isIdentityAlreadyTaken(codesList)) {
            throw new CodesListAlreadyExistsException("The identifier, IRI and OWL class should be unique");
        }
        repository.save(codesList);
        return codesList.id();
    }

    @Override
    public CodesListId update(CodesListId idFromUrl, UpdateCodesListCommand command)
            throws CodesListIdMismatchException, CodesListNotFoundException, CodesListsFetchException,
                    CodesListsSaveException {
        if (!idFromUrl.value().equals(command.id())) {
            throw new CodesListIdMismatchException("The id of the list should match the id of the url");
        }

        // Sans ce contrôle le PUT était un upsert silencieux. La recherche se fait par IRI, pas par
        // notation : l'identifiant d'une liste complète reste renommable depuis le front.
        PersistedCodesList persisted = repository
                .findByUriSegment(command.lastListUriSegment())
                .orElseThrow(() -> new CodesListNotFoundException("CodeList not found"));

        CodesList codesList = CodesList.revise(command.attributes(), persisted);
        repository.save(codesList);
        return codesList.id();
    }
}
