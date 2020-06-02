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

@Configuration("AppContext")
@PropertySource(value = { "classpath:bauhaus-core.properties", "classpath:bauhaus-dev.properties",
		"file:${catalina.base}/webapps/bauhaus-dev.properties", "file:${catalina.base}/webapps/bauhaus-qf.properties",
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
		return new RestTemplate(requestFactory);
	}

	@PostConstruct
	public void setUp() {
		Config.setConfig(env);
	}

	

}
