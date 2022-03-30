package fr.insee.rmes.external_services.authentication.user_roles_manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;


public class Sugoi {

	 private Sugoi() {
		    throw new IllegalStateException("Utility class");
	}

	private static Config config;

	static final Logger logger = LogManager.getLogger(Sugoi.class);

	private static void execute(HttpRequestBase httpMethod)  throws RmesException{
		/* add authentication params */
	    UsernamePasswordCredentials creds
	      = new UsernamePasswordCredentials(config.getSugoiUser(), config.getSugoiPassword());
		try {

			httpMethod.setHeader("Accept", MediaType.APPLICATION_JSON);
			httpMethod.setHeader("Content-type", MediaType.APPLICATION_JSON);
			httpMethod.addHeader(new BasicScheme().authenticate(creds, httpMethod, null));
			logger.debug("Sugoi method with creds : {}", httpMethod);
			/* Creation client http */
			HttpClient httpclient = HttpClientBuilder.create().build();
			logger.debug("Sugoi httpClient : {}", httpclient);
			/* getResponse */
			HttpResponse response = httpclient.execute(httpMethod);
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			readBuffer(reader);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(),e.getClass().getName());
			
		}
	}
	
	public static void put(String url)  throws RmesException{
		logger.info("Sugoi, put : {}", url);
		HttpPut httpPut = new HttpPut(url);
		execute(httpPut);
	}
	
	public static void delete(String url)  throws RmesException{
		logger.info("Sugoi, delete : {}", url);
		HttpDelete httpDelete = new HttpDelete(url);
		execute(httpDelete);
	}

	private static String readBuffer(BufferedReader reader) {
		StringBuilder builder = new StringBuilder();
		String aux = "";

		try {
			while ((aux = reader.readLine()) != null) {
				builder.append(aux);
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		String result = builder.toString();
		logger.info(result);
		return result;
	}
	
	public static void setConfig(Config config) {
		Sugoi.config = config;
	}
	

}
