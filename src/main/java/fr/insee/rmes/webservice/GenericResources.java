package fr.insee.rmes.webservice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;

@RestController
public class GenericResources {
	
	@Autowired
	Config config;
	
	protected static final Logger logger = LogManager.getLogger(GenericResources.class);

	protected ResponseEntity<Object> returnRmesException(RmesException e) {
		logger.error(e.getMessage(), e);
		return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
	}

}
