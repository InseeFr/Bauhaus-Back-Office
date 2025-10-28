package fr.insee.rmes.domain.port.clientside;

import fr.insee.rmes.domain.exceptions.RmesException;

import java.util.List;

public interface OrganisationService {
    List<String> getStamps() throws RmesException;
}
