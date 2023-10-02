package fr.insee.rmes.config.swagger;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final  Logger logger = LoggerFactory.getLogger(OpenApiConfiguration.class);
	
	@Value("${fr.insee.rmes.bauhaus.version}")
	private String projectVersion;
	
	@Bean
	public OpenAPI customOpenAPI(@Value("${fr.insee.rmes.bauhaus.api.ssl}") boolean swaggerUseSSL, @Value("${fr.insee.rmes.bauhaus.api.host}") String swaggerHost, @Value("${fr.insee.rmes.bauhaus.api.basepath}") String swaggerBasepath) {
		Server server = new Server();
		var swaggerUrl = (swaggerUseSSL ? "https" : "http")+ swaggerHost + "/" + swaggerBasepath;
		server.setUrl(swaggerUrl);
		logger.info("____________________SWAGGER CONFIG : {} _________________________________________________", swaggerUrl);
		return new OpenAPI()
				.addServersItem(server)
				.info(new Info()
						.title("Bauhaus API")
						.description("Rest Endpoints and services Integration used by Bauhaus")
						.version(projectVersion)
				);
	}

}
