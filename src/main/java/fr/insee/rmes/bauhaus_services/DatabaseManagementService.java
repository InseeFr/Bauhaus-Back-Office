package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.exceptions.RmesException;

import java.io.IOException;

public interface DatabaseManagementService {
    void clearGraph() throws RmesException, IOException;
}
