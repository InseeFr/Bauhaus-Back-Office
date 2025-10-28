package fr.insee.rmes.domain.port.serverside;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.OrganisationOption;

import java.util.List;

public interface OrganisationRepository {
    List<OrganisationOption> getOrganisations() throws RmesException;
}
