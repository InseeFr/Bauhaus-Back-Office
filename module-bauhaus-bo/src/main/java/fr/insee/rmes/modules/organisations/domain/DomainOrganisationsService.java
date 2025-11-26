package fr.insee.rmes.modules.organisations.domain;

import fr.insee.rmes.modules.organisations.domain.exceptions.OrganisationFetchException;
import fr.insee.rmes.modules.organisations.domain.model.CompactOrganisation;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationsService;
import fr.insee.rmes.modules.organisations.domain.port.serverside.OrganisationsRepository;

import java.util.List;

public class DomainOrganisationsService implements OrganisationsService {
    private final OrganisationsRepository organisationsRepository;

    public DomainOrganisationsService(OrganisationsRepository organisationsRepository) {
        this.organisationsRepository = organisationsRepository;
    }

    @Override
    public CompactOrganisation getCompactOrganisation(String id) throws OrganisationFetchException {
        return this.organisationsRepository.getCompactOrganisation(id);
    }

    @Override
    public List<CompactOrganisation> getCompactOrganisations(List<String> ids) throws OrganisationFetchException {
        return this.organisationsRepository.getCompactOrganisations(ids);
    }

    @Override
    public boolean checkIfOrganisationExists(String iri) throws OrganisationFetchException {
        return this.organisationsRepository.checkIfOrganisationExists(iri);
    }
}
