package fr.insee.rmes.modules.commons.webservice;

import fr.insee.rmes.domain.exceptions.RmesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GenericResources {
	
	protected static final Logger logger = LoggerFactory.getLogger(GenericResources.class);

	protected ResponseEntity<Object> returnRmesException(RmesException e) {
		logger.error(e.getMessage(), e);
		logger.error(e.getDetails(), e);
		return ResponseEntity.status(e.getStatus()).contentType(MediaType.TEXT_PLAIN).body(e.getMessage());
	}

}
