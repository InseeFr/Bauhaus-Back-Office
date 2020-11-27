package fr.insee.rmes.bauhaus_services.structures;
import fr.insee.rmes.exceptions.RmesException;

public interface StructureService {
	
	String getStructures() throws RmesException;

	String getStructuresForSearch() throws  RmesException;

	String getStructureById(String id) throws RmesException;
	
	String setStructure(String body) throws RmesException;
	
	String setStructure(String id, String body) throws RmesException;

    void deleteStructure(String structureId) throws RmesException;

	String getStructureByIdWithDetails(String id) throws RmesException;
}
