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


	static final Logger logger = LogManager.getLogger(Sugoi.class);

	public static void put(String url)  throws RmesException{
		logger.info("Sugoi, put : {}", url);
		HttpPut httpPut = new HttpPut(url);

		/* ajout des paramètres d’authentification dans la requête */

		
	    UsernamePasswordCredentials creds
	      = new UsernamePasswordCredentials(Config.SUGOI_USER, Config.SUGOI_PASSWORD);
		try {

			httpPut.setHeader("Accept", MediaType.APPLICATION_JSON);
			httpPut.setHeader("Content-type", MediaType.APPLICATION_JSON);
			httpPut.addHeader(new BasicScheme().authenticate(creds, httpPut, null));
			logger.debug("Sugoi, put : {}", httpPut);
			/* Création du client http */
			HttpClient httpclient = HttpClientBuilder.create().build();
			logger.debug("Sugoi, put : {}", httpclient);
			/* Récupération de la réponse */
			HttpResponse response = httpclient.execute(httpPut);
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			readBuffer(reader);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(),e.getClass().getName());
			
		}
	}
	
	public static void delete(String url)  throws RmesException{
		logger.info("Sugoi, delete : {}", url);
		HttpDelete httpDelete = new HttpDelete(url);

		/* ajout des paramètres d’authentification dans la requête */

	    UsernamePasswordCredentials creds
	      = new UsernamePasswordCredentials(Config.SUGOI_USER, Config.SUGOI_PASSWORD);
		try {

			httpDelete.setHeader("Accept", MediaType.APPLICATION_JSON);
			httpDelete.setHeader("Content-type", MediaType.APPLICATION_JSON);
			httpDelete.addHeader(new BasicScheme().authenticate(creds, httpDelete, null));
			logger.debug("Sugoi, delete : {}", httpDelete);
			/* Création du client http */
			HttpClient httpclient = HttpClientBuilder.create().build();
			logger.debug("Sugoi, delete : {}", httpclient);
			/* Récupération de la réponse */
			HttpResponse response = httpclient.execute(httpDelete);
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			readBuffer(reader);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(),e.getClass().getName());
			
		}
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

}
