package fr.insee.rmes.external_services.authentication.user_roles_manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.rmes.config.Config;

public class Igesa {

	static final Logger logger = LogManager.getLogger(Igesa.class);

	public static void post(String url) {
		logger.info("Igesa, post : {}", url);
		HttpPost httppost = new HttpPost(url);

		/* ajout des paramètres d’authentification dans la requête */
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("j_username", Config.IGESA_USER));
		params.add(new BasicNameValuePair("j_password", Config.IGESA_PASSWORD));
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params));

			/* Création du client http */
			HttpClient httpclient = HttpClientBuilder.create().build();

			/* Récupération de la réponse */
			HttpResponse response = httpclient.execute(httppost);
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
