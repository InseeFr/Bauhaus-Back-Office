package fr.insee.rmes.config.swagger;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(//add Authorize button in swagger
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfiguration   {

    public static final String DESCRIPTION="Back office de Bauhaus (rmesgncs)";
    public static final String TITLE="Bauhaus";

    @Bean
    public OpenAPI springShopOpenAPI(@Value("${fr.insee.rmes.bauhaus.version}") String version) {
        return new OpenAPI()
                .info((new Info()).version(version));
    }

    @Bean
    public OpenAPI openAPI(@Value("${springdoc.version}") String appVersion) {
        return new OpenAPI()
                .info(new Info()
                        .title(TITLE)
                        .version(appVersion)
                        .description(DESCRIPTION)
                );
    }

}
