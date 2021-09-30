package fr.insee.rmes.bauhaus_services.consutation_gestion;

import fr.insee.rmes.exceptions.RmesException;

public interface ConsultationGestionService {
    String getDetailedConcept(String id) throws RmesException;

    String getAllConcepts() throws RmesException;

    String getAllStructures() throws RmesException;

    String getAllCodesLists() throws RmesException;

    String getCodesList(String notation) throws RmesException;

    String getStructure(String id) throws RmesException;

    String getAllComponents() throws RmesException;

    String getComponent(String id) throws RmesException;
}
