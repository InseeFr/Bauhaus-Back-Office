package fr.insee.rmes.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
		List<MediaType> acceptHeaders =new ArrayList<>( headers.getAccept());
		acceptHeaders.add(MediaType.APPLICATION_JSON );
		headers.setAccept(acceptHeaders);
		headers.setContentType(MediaType.APPLICATION_JSON );
		return headers;
	}		
	

	public String getResponseAsString(String target, HttpHeaders headers, Map<String, Object> params) {
		if (params == null) return getResponseAsString(target, headers);
		RestTemplate restTemplate = new RestTemplate();
		
		// HttpEntity<String>: To get result as String.
		HttpEntity<String> entity = new HttpEntity<>(headers);

		// Send request with GET method, and Headers, and Params (can't be null).
		ResponseEntity<String> response = restTemplate.exchange(target, HttpMethod.GET, entity, String.class, params);

		return response.getBody();
	}

	public String getResponseAsString(String target, HttpHeaders headers) {
		RestTemplate restTemplate = new RestTemplate();
		
		// HttpEntity<String>: To get result as String.
		HttpEntity<String> entity = new HttpEntity<>(headers);

		// Send request with GET method, and Headers.
		ResponseEntity<String> response = restTemplate.exchange(target, HttpMethod.GET, entity, String.class);

		return response.getBody();
	}

	public void addMultipartContentToHeader(HttpHeaders headers) {
	//	headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		List<MediaType> acceptHeaders =new ArrayList<>( headers.getAccept());
		acceptHeaders.add(MediaType.APPLICATION_JSON );
		headers.setAccept(acceptHeaders);
		//headers.setContentType(MediaType.APPLICATION_XML);
		
	}
	
	  private static Resource getFileAsResource(InputStream fileIs) {
		try {
			Path tempFile = Files.createTempFile("upload-test-file", ".txt");
			 Files.write(tempFile, fileIs.readAllBytes());
		      File file = tempFile.toFile();
		      //to upload in-memory bytes use ByteArrayResource instead
		      return new FileSystemResource(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	     
	  }
	
	public String postForEntity(String target, Object request, HttpHeaders headers, InputStream fileResource) throws RmesException {
		
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("attachments", getFileAsResource(fileResource));
		body.add("request", request);
		//name("request").build(),request,MediaType.APPLICATION_XML_VALUE)
		
		// HttpEntity<MultiValueMap<String, Object>>: To get result as String.
		HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);

		// Send request with POST method, and Headers.
		RestTemplate restTemplate = new RestTemplate();
		
		try {
			ResponseEntity<String> response = restTemplate.postForEntity(target, requestEntity, String.class);
			return response.getBody();
		}catch(Exception e) {
			throw new RmesException(HttpStatus.FAILED_DEPENDENCY, "SPOC error, "+ e.getClass(), e.getMessage());
		}
	}

	
}
