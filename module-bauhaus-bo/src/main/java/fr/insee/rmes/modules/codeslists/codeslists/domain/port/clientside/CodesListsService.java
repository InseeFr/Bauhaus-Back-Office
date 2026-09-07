package fr.insee.rmes.modules.codeslists.codeslists.domain.port.clientside;

import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.CodesListAlreadyExistsException;
import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.CodesListIdMismatchException;
import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.CodesListNotFoundException;
import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.CodesListsFetchException;
import fr.insee.rmes.modules.codeslists.codeslists.domain.exceptions.CodesListsSaveException;
import fr.insee.rmes.modules.codeslists.codeslists.domain.model.CodesListId;
import fr.insee.rmes.modules.codeslists.codeslists.domain.model.commands.CreateCodesListCommand;
import fr.insee.rmes.modules.codeslists.codeslists.domain.model.commands.UpdateCodesListCommand;
import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;

@ClientSidePort
public interface CodesListsService {

    CodesListId create(CreateCodesListCommand command)
            throws CodesListAlreadyExistsException, CodesListsFetchException, CodesListsSaveException;

    CodesListId update(CodesListId idFromUrl, UpdateCodesListCommand command)
            throws CodesListIdMismatchException, CodesListNotFoundException, CodesListsFetchException, CodesListsSaveException;
}
