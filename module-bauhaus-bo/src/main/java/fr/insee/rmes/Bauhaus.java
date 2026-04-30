package fr.insee.rmes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Bauhaus{

    static void main(String[] args) {
        SpringApplication.run(Bauhaus.class, args);
    }

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

}
