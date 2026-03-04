package fr.insee.rmes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Bauhaus{

    static void main(String[] args) {
        SpringApplication.run(Bauhaus.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // Add StringHttpMessageConverter to handle text/plain responses
        restTemplate.getMessageConverters().add(0, new org.springframework.http.converter.StringHttpMessageConverter());
        
        return restTemplate;
    }

}
