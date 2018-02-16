package fr.insee.rmes.persistance.service.sesame.operations;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.persistance.service.OperationsService;
import fr.insee.rmes.persistance.service.sesame.operations.series.SerieForList;

@Service
public class OperationsImpl implements OperationsService {

	final static Logger logger = LogManager.getLogger(OperationsImpl.class);

	@Autowired
	RestTemplate restTemplate;

	public List<SerieForList> getSeries() throws Exception {
		String url = String.format("%s/api/search/series", Config.BASE_URI_METADATA_API);	
		ResponseEntity<SerieForList[]> seriesRes = restTemplate.exchange(url, HttpMethod.GET, null, SerieForList[].class);
		logger.info("GET Series");
		return Arrays.asList(seriesRes.getBody());
	}

}
