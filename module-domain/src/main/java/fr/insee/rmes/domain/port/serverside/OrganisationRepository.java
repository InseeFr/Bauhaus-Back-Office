package fr.insee.rmes.domain.port.serverside;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.OrganisationOption;

import java.util.List;
import java.util.Map;

public interface OrganisationRepository {
    List<OrganisationOption> getOrganisations() throws RmesException;

    OrganisationOption getOrganisation(String identifier) throws RmesException;

    Map<String, OrganisationOption> getOrganisationsMap(List<String> identifiers) throws RmesException;
}
