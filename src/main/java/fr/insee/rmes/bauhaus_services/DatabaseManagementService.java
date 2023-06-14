package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.exceptions.RmesException;

public interface DatabaseManagementService {
    void clearGraph() throws RmesException;
}
