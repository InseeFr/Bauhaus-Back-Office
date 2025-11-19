package fr.insee.rmes.domain.port.clientside;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.OrganisationOption;

import java.util.List;
import java.util.Map;

public interface OrganisationService {
    List<String> getStamps() throws RmesException;

    List<OrganisationOption> getOrganisations() throws RmesException;

    OrganisationOption getOrganisation(String identifier) throws RmesException;

    /**
     * Retrieves multiple organizations by their identifiers in a single batch operation
     * @param identifiers List of organization identifiers to retrieve
     * @return Map of identifier to OrganisationOption (only contains found organizations)
     * @throws RmesException if retrieval fails
     */
    Map<String, OrganisationOption> getOrganisationsMap(List<String> identifiers) throws RmesException;
}
