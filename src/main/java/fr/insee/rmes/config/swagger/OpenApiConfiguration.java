package fr.insee.rmes.config.swagger;

import javax.ws.rs.ApplicationPath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import fr.insee.rmes.config.Config;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@ApplicationPath("/")
@DependsOn("AppContext")
@Configuration
@SecurityScheme(//add Authorize button in swagger
	    name = "bearerAuth",
	    type = SecuritySchemeType.HTTP,
	    bearerFormat = "JWT",
	    scheme = "bearer"
	)
public class OpenApiConfiguration   {

	private static final  Logger logger = LogManager.getLogger(OpenApiConfiguration.class);
	
	@Value("${fr.insee.rmes.bauhaus.version}")
	private String projectVersion;
	
	@Bean
	public OpenAPI customOpenAPI(Config config) {
		Server server = new Server();
		server.setUrl(config.getSwaggerUrl());
		logger.info("______________________________________________________________________");
		logger.info("____________________SWAGGER HOST : {}_________________________________________________", config.getSwaggerHost());
		logger.info("____________________SWAGGER BASEPATH : {} _________________________________________________", config.getSwaggerBasepath());
		logger.info("____________________SWAGGER CONFIG : {} _________________________________________________", config.getSwaggerUrl());
		logger.info("______________________________________________________________________");
		return new OpenAPI()
				.addServersItem(server)
				.info(new Info()
						.title("Bauhaus API")
						.description("Rest Endpoints and services Integration used by Bauhaus")
						.version(projectVersion)
				);
	}

}
