package fr.insee.rmes;

import fr.insee.rmes.config.PropertiesLogger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Bauhaus{

    public static void main(String[] args) {
        configureApplicationBuilder(new SpringApplicationBuilder()).build().run(args);
    }

    public static SpringApplicationBuilder configureApplicationBuilder(SpringApplicationBuilder springApplicationBuilder) {
        return springApplicationBuilder.sources(Bauhaus.class)
                .listeners(new PropertiesLogger());
    }

}
