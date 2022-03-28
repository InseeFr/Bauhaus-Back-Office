package fr.insee.rmes.external_services.authentication.user_roles_manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.rmes.config.Config;

public class Sugoi {

	 private Sugoi() {
		    throw new IllegalStateException("Utility class");
	}


	static final Logger logger = LogManager.getLogger(Sugoi.class);

	public static void put(String url)  throws ClientProtocolException, IOException, AuthenticationException{
		logger.info("Sugoi, put : {}", url);
		HttpPut httpPut = new HttpPut(url);

		/* ajout des paramètres d’authentification dans la requête */

		httpPut.setEntity(new StringEntity("test post"));
	    UsernamePasswordCredentials creds
	      = new UsernamePasswordCredentials(Config.SUGOI_USER, Config.SUGOI_PASSWORD);
		try {

			httpPut.setHeader("Accept", "application/json");
			httpPut.setHeader("Content-type", "application/json");
			httpPut.addHeader(new BasicScheme().authenticate(creds, httpPut, null));
			//logger.info("Sugoi, put : {}", httpPut);
			/* Création du client http */
			HttpClient httpclient = HttpClientBuilder.create().build();
			//logger.info("Sugoi, put : {}", httpclient);
			/* Récupération de la réponse */
			HttpResponse response = httpclient.execute(httpPut);
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			readBuffer(reader);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	public static void delete(String url)  throws ClientProtocolException, IOException, AuthenticationException{
		logger.info("Sugoi, delete : {}", url);
		HttpDelete httpDelete = new HttpDelete(url);

		/* ajout des paramètres d’authentification dans la requête */

	    UsernamePasswordCredentials creds
	      = new UsernamePasswordCredentials(Config.SUGOI_USER, Config.SUGOI_PASSWORD);
		try {

			httpDelete.setHeader("Accept", "application/json");
			httpDelete.setHeader("Content-type", "application/json");
			httpDelete.addHeader(new BasicScheme().authenticate(creds, httpDelete, null));
			//logger.info("Sugoi, delete : {}", httpDelete);
			/* Création du client http */
			HttpClient httpclient = HttpClientBuilder.create().build();
			//logger.info("Sugoi, delete : {}", httpclient);
			/* Récupération de la réponse */
			HttpResponse response = httpclient.execute(httpDelete);
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			readBuffer(reader);
		} catch (Exception e) {
			logger.error(e.getMessage());
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
