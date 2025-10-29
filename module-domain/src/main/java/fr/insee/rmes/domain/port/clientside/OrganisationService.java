package fr.insee.rmes.domain.port.clientside;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.OrganisationOption;

import java.util.List;

public interface OrganisationService {
    List<String> getStamps() throws RmesException;

    List<OrganisationOption> getOrganisations() throws RmesException;
}
