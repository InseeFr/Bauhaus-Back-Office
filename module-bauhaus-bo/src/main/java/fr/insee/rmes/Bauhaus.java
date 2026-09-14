package fr.insee.rmes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Bauhaus {

    static void main(String[] args) {
        SpringApplication.run(Bauhaus.class, args);
    }
}
