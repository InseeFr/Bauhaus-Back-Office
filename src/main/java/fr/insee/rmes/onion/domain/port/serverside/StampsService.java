package fr.insee.rmes.onion.domain.port.serverside;

import fr.insee.rmes.onion.domain.exceptions.RmesException;
<<<<<<<< HEAD:module-bauhaus-bo/src/main/java/fr/insee/rmes/onion/domain/port/serverside/StampsService.java
import fr.insee.rmes.domain.model.Stamp;
========
import fr.insee.rmes.onion.domain.model.Stamp;
>>>>>>>> 2c8e0c39 (feat: init sans object feature (#983)):src/main/java/fr/insee/rmes/onion/domain/port/serverside/StampsService.java

import java.util.List;

public interface StampsService {
	
	List<String> getStamps() throws RmesException;

	Stamp findStampFrom(Object principal) throws RmesException;
}
