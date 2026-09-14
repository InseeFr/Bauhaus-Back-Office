package fr.insee.rmes.modules.organisations.domain.port.serverside;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;
import fr.insee.rmes.modules.organisations.domain.model.OrganisationOption;
import java.util.List;
import java.util.Map;

@ServerSidePort
public interface OrganisationRepository {

    List<OrganisationOption> getOrganisations() throws RmesException;

    OrganisationOption getOrganisation(String identifier) throws RmesException;

    Map<String, OrganisationOption> getOrganisationsMap(List<String> identifiers) throws RmesException;
}
