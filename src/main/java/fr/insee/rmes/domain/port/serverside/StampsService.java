package fr.insee.rmes.domain.port.serverside;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.Stamp;

import java.util.List;

public interface StampsService {
	
	List<String> getStamps() throws RmesException;

	Stamp findStampFrom(Object principal) throws RmesException;
}
