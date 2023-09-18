package fr.insee.rmes.webservice;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GenericResources {
	
	@Autowired
	Config config;
	
	protected static final Logger logger = LoggerFactory.getLogger(GenericResources.class);

	protected ResponseEntity<Object> returnRmesException(RmesException e) {
		logger.error(e.getMessage(), e);
		return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getDetails());
	}

}
