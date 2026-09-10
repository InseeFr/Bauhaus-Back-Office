package fr.insee.rmes.modules.organisations.domain.port.clientside;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;
import fr.insee.rmes.modules.organisations.domain.model.OrganisationOption;
import java.util.List;
import java.util.Map;

@ClientSidePort
public interface OrganisationService {

    List<String> getStamps() throws RmesException;

    List<OrganisationOption> getOrganisations() throws RmesException;

    OrganisationOption getOrganisation(String identifier) throws RmesException;

    Map<String, OrganisationOption> getOrganisationsMap(List<String> identifiers) throws RmesException;
}
