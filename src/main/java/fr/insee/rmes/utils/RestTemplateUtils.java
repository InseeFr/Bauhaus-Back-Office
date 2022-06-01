package fr.insee.rmes.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import fr.insee.rmes.exceptions.RmesException;

@Service
public class RestTemplateUtils {
	
	RestTemplate restTemplate = new RestTemplate();
	
	public HttpHeaders getHeadersWithBasicAuth(String username, String password) {
		// HttpHeaders
		HttpHeaders headers = new HttpHeaders();
					
		// Authentication
		String auth = username + ":" + password;
		byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.US_ASCII));
		String authHeader = "Basic " + new String(encodedAuth);
		headers.set("Authorization", authHeader);
				
		return headers;
	}
	
	public HttpHeaders addJsonContentToHeader(HttpHeaders headers) {
		addAcceptJsonToHeader(headers);
		headers.setContentType(MediaType.APPLICATION_JSON );
		return headers;
	}		
	

	public String getResponseAsString(String target, HttpHeaders headers, Map<String, Object> params) {
		if (params == null) return getResponseAsString(target, headers);		
		// HttpEntity<String>: To get result as String.
		HttpEntity<String> entity = new HttpEntity<>(headers);

		// Send request with GET method, and Headers, and Params (can't be null).
		ResponseEntity<String> response = restTemplate.exchange(target, HttpMethod.GET, entity, String.class, params);

		return response.getBody();
	}

	public String getResponseAsString(String target, HttpHeaders headers) {	
		// HttpEntity<String>: To get result as String.
		HttpEntity<String> entity = new HttpEntity<>(headers);

		// Send request with GET method, and Headers.
		ResponseEntity<String> response = restTemplate.exchange(target, HttpMethod.GET, entity, String.class);

		return response.getBody();
	}

	public void addAcceptJsonToHeader(HttpHeaders headers) {
		List<MediaType> acceptHeaders =new ArrayList<>( headers.getAccept());
		acceptHeaders.add(MediaType.APPLICATION_JSON );
		headers.setAccept(acceptHeaders);
	}
	
	  private static Resource getFileAsResource(InputStream fileIs, String filename) throws RmesException {
		try {
			String tempDir = System.getProperty("java.io.tmpdir");
			Path tempFile = Paths.get(tempDir ,filename);
			Files.write(tempFile, fileIs.readAllBytes());
			File file = tempFile.toFile();
			file.deleteOnExit();
		    return new FileSystemResource(file);
		} catch (IOException e) {
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, "Can't convert file to resource IOException", e.getMessage());
		}
	     
	  }
	
	public String postForEntity(String target, MultiValueMap<String, Object> body, HttpHeaders headers) throws RmesException {

		// HttpEntity<MultiValueMap<String, Object>>: To get result as String.
		HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);
		
		// Send request with POST method, and Headers.
		try {
			ResponseEntity<String> response = restTemplate.postForEntity(target, requestEntity, String.class);
			return response.getBody();
		}catch(Exception e) {
			throw new RmesException(HttpStatus.FAILED_DEPENDENCY, "SPOC error, "+ e.getClass(), e.getMessage());
		}
	}

	public MultiValueMap<String, Object> buildBodyAsMap(String request, InputStream fileResource, String filename) throws RmesException {
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

		//add file to join to body
		HttpHeaders fileheaders = new HttpHeaders();
		fileheaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		body.add("attachments", new HttpEntity<>(getFileAsResource(fileResource, filename), fileheaders));
		
		//add xml content to body
		HttpHeaders contentheaders = new HttpHeaders();
		contentheaders.setContentType(MediaType.APPLICATION_XML);
		body.add("request", new HttpEntity<>(request, contentheaders));
		
		return body;
	}

	
}
