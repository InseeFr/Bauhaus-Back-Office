package fr.insee.rmes.bauhaus_services.consutation_gestion;

import fr.insee.rmes.exceptions.RmesException;

public interface ConsultationGestionService {
    String getDetailedConcept(String id) throws RmesException;

    String getAllConcepts() throws RmesException;
}
