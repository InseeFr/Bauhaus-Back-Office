package fr.insee.rmes.external_services.authentication.user_roles_manager;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Arrays;


public class Sugoi {

	 private Sugoi() {
		    throw new IllegalStateException("Utility class");
	}

	private static Config config;

	static final Logger logger = LoggerFactory.getLogger(Sugoi.class);

	private static void execute(HttpRequestBase httpMethod)  throws RmesException{
		/* add authentication params */
	    UsernamePasswordCredentials creds
	      = new UsernamePasswordCredentials(config.getSugoiUser(), config.getSugoiPassword());
		try {

			httpMethod.setHeader("Accept", MediaType.APPLICATION_JSON_VALUE);
			httpMethod.setHeader("Content-type", MediaType.APPLICATION_JSON_VALUE);
			httpMethod.addHeader(new BasicScheme().authenticate(creds, httpMethod, null));
			logger.debug("Sugoi method with creds : {}", Arrays.toString(httpMethod.getAllHeaders()));
			/* Creation client http */
			HttpClient httpclient = HttpClientBuilder.create().build();
			logger.debug("Sugoi httpClient : {}", httpclient);
			/* getResponse */
			HttpResponse response = httpclient.execute(httpMethod);
			int status = response.getStatusLine().getStatusCode();
			if (status <200 || status>299) {
				throw new RmesException(status, "Error editing data with Sugoi", "Sugoi error");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(),e.getClass().getName());
			
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
	
	public static void setConfig(Config config) {
		Sugoi.config = config;
	}
	

}
