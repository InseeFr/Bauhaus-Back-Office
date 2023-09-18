package fr.insee.rmes.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration("AppContext")
public class ApplicationContext {

	@Autowired
	Config config;
	
//	@Bean
//	public HttpClientBuilder httpClientBuilder()
//			throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
//		SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
//		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
//		return HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).setSSLSocketFactory(sslsf);
//	}
	

	@PostConstruct
	public void setUp() {
		config.printMajorConfig();
	}

	

}
