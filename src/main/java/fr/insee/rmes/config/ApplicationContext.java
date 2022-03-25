package fr.insee.rmes.config;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration("AppContext")
@PropertySource(value =  "classpath:bauhaus-core.properties")
@PropertySource(value =  "classpath:bauhaus-dev.properties")
@PropertySource(value =  "file:${catalina.base}/webapps/bauhaus-dev.properties", ignoreResourceNotFound = true)
@PropertySource(value =  "file:${catalina.base}/webapps/bauhaus-qf.properties", ignoreResourceNotFound = true)
@PropertySource(value =  "file:${catalina.base}/webapps/bauhaus-production.properties", ignoreResourceNotFound = true)
@PropertySource(value =  "file:${catalina.base}/webapps/production.properties", ignoreResourceNotFound = true) 
public class ApplicationContext {

	@Autowired
	Config config;
	
	@Bean
	public HttpClientBuilder httpClientBuilder()
			throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
		SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
		return HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).setSSLSocketFactory(sslsf);
	}

	@PostConstruct
	public void setUp() {
		config.printMajorConfig();
	}

	

}
