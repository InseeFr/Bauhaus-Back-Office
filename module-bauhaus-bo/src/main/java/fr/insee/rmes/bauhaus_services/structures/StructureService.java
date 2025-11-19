package fr.insee.rmes.bauhaus_services.structures;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.structures.structures.domain.model.PartialStructure;

import java.util.List;

public interface StructureService {
	
	List<PartialStructure> getStructures() throws RmesException;

	String getStructuresForSearch() throws  RmesException;

	String getStructureById(String id) throws RmesException;
	
	String setStructure(String body) throws RmesException;
	
	String setStructure(String id, String body) throws RmesException;

    void deleteStructure(String structureId) throws RmesException;

	String getStructureByIdWithDetails(String id) throws RmesException;

	String publishStructureById(String id) throws RmesException;
}
