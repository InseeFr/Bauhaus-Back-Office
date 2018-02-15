package fr.insee.rmes.config;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@PropertySource(value = { "classpath:gncs-core.properties", "classpath:gncs-dev.properties",
		"file:${catalina.base}/webapps/gncs-dev.properties", "file:${catalina.base}/webapps/gncs-qf.properties",
		"file:${catalina.base}/webapps/production.properties", }, ignoreResourceNotFound = true)
public class ApplicationContext {

	@Autowired
	Environment env;
	
	@Bean
	public HttpClientBuilder httpClientBuilder()
			throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
		SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
		return HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).setSSLSocketFactory(sslsf);
	}
	
	@Bean
	public RestTemplate restTemplate() throws KeyManagementException, KeyStoreException, NoSuchAlgorithmException {
		CloseableHttpClient httpClient = httpClientBuilder().build();
		ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		RestTemplate restTemplate = new RestTemplate(requestFactory);
		return restTemplate;
	}

	@PostConstruct
	public void setUp() {

		Config.APP_HOST = env.getProperty("fr.insee.rmes.gncs.concepts.appHost");

		Config.REQUIRES_SSL = Boolean.valueOf(env.getProperty("fr.insee.rmes.gncs.force.ssl"));

		Config.DEFAULT_CONTRIBUTOR = env.getProperty("fr.insee.rmes.gncs.concepts.defaultContributor");
		Config.DEFAULT_MAIL_SENDER = env.getProperty("fr.insee.rmes.gncs.concepts.defaultMailSender");
		Config.MAX_LENGTH_SCOPE_NOTE = env.getProperty("fr.insee.rmes.gncs.concepts.maxLengthScopeNote");

		Config.LG1 = env.getProperty("fr.insee.rmes.gncs.lg1");
		Config.LG2 = env.getProperty("fr.insee.rmes.gncs.lg2");

		Config.PASSWORD_GESTIONNAIRE = env.getProperty("fr.insee.rmes.gncs.gestionnaire.password");
		Config.PASSWORD_PRODUCTEUR = env.getProperty("fr.insee.rmes.gncs.producteur.password");

		Config.CONCEPTS_GRAPH = env.getProperty("fr.insee.rmes.gncs.concepts.graph");
		Config.CONCEPTS_SCHEME = env.getProperty("fr.insee.rmes.gncs.concepts.scheme");
		Config.CONCEPTS_BASE_URI = env.getProperty("fr.insee.rmes.gncs.concepts.baseURI");
		Config.COLLECTIONS_BASE_URI = env.getProperty("fr.insee.rmes.gncs.collections.baseURI");

		Config.SESAME_SERVER_GESTION = env.getProperty("fr.insee.rmes.gncs.sesame.gestion.sesameServer");
		Config.REPOSITORY_ID_GESTION = env.getProperty("fr.insee.rmes.gncs.sesame.gestion.repository");
		Config.BASE_URI_GESTION = env.getProperty("fr.insee.rmes.gncs.sesame.gestion.baseURI");

		Config.SESAME_SERVER_PUBLICATION = env.getProperty("fr.insee.rmes.gncs.sesame.publication.sesameServer");
		Config.REPOSITORY_ID_PUBLICATION = env.getProperty("fr.insee.rmes.gncs.sesame.publication.repository");
		Config.BASE_URI_PUBLICATION = env.getProperty("fr.insee.rmes.gncs.sesame.publication.baseURI");

		Config.BASE_URI_METADATA_API = env.getProperty("fr.insee.rmes.gncs.metadata.api.baseURI");

		Config.SPOC_SERVICE_URL = env.getProperty("fr.insee.rmes.gncs.spoc.url");
		Config.SPOC_USER = env.getProperty("fr.insee.rmes.gncs.spoc.user");
		Config.SPOC_PASSWORD = env.getProperty("fr.insee.rmes.gncs.spoc.password");

		Config.BROKER_URL = env.getProperty("fr.insee.rmes.gncs.broker.url");
		Config.BROKER_USER = env.getProperty("fr.insee.rmes.gncs.broker.user");
		Config.BROKER_PASSWORD = env.getProperty("fr.insee.rmes.gncs.broker.password");

		Config.LDAP_URL = env.getProperty("fr.insee.rmes.gncs.ldap.url");

		Config.IGESA_URL = env.getProperty("fr.insee.rmes.gncs.igesa.url");
		Config.IGESA_APP_ID = env.getProperty("fr.insee.rmes.gncs.igesa.id");
		Config.IGESA_USER = env.getProperty("fr.insee.rmes.gncs.igesa.user");
		Config.IGESA_PASSWORD = env.getProperty("fr.insee.rmes.gncs.igesa.password");

	}

}
