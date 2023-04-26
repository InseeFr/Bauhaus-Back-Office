package fr.insee.rmes.webservice;

import fr.insee.rmes.bauhaus_services.keycloak.KeycloakServices;
import fr.insee.rmes.exceptions.RmesException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("token")
public class TokenApi extends GenericResources {
	
	@Autowired
	protected KeycloakServices keycloakServices;

	
	private static final Logger logger = LogManager.getLogger(TokenApi.class);

    @GetMapping(value = "")
    public String getToken() throws RmesException {
		logger.info("GET /token");
		return keycloakServices.getKeycloakAccessToken();
	}

    
}