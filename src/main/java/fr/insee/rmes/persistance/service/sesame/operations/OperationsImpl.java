package fr.insee.rmes.persistance.service.sesame.operations;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.persistance.service.OperationsService;
import fr.insee.rmes.persistance.service.sesame.export.Jasper;
import fr.insee.rmes.persistance.service.sesame.operations.series.SerieForList;

@Service
public class OperationsImpl implements OperationsService {

	final static Logger logger = LogManager.getLogger(OperationsImpl.class);

	@Autowired
	RestTemplate restTemplate;

	@Override
	public List<SerieForList> getSeries() throws Exception {
		String url = String.format("%s/api/search/series", Config.BASE_URI_METADATA_API);
		ResponseEntity<SerieForList[]> seriesRes = restTemplate.exchange(url, HttpMethod.GET, null,
				SerieForList[].class);
		logger.info("GET Series");
		return Arrays.asList(seriesRes.getBody());
	}

	@Override
	public String getDataForVarBook(String operationId) throws Exception {
		String url = String.format("%s/api/meta-data/operation/%s/variableBook", Config.BASE_URI_METADATA_API,
				operationId);
		ResponseEntity<String> seriesRes = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
		logger.info("GET data for variable book");
		return seriesRes.getBody();
	}

	@Override
	public Response getVarBookExport(String id, String acceptHeader) throws Exception {
		String xml = getDataForVarBook(id);
		JSONObject json = XML.toJSONObject(xml);

		Jasper jasper = new Jasper();
		InputStream is = jasper.exportVariableBook(json, acceptHeader);
		String fileName = "Dico" + id + jasper.getExtension(acceptHeader);
		ContentDisposition content = ContentDisposition.type("attachment").fileName(fileName).build();
		return Response.ok(is, acceptHeader).header("Content-Disposition", content).build();
	}

}
