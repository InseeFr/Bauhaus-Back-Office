package fr.insee.rmes.config.swagger;

import javax.ws.rs.ApplicationPath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import fr.insee.rmes.config.Config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@ApplicationPath("/")
@DependsOn("AppContext")
@Configuration
public class OpenApiConfiguration   {

	private static final  Logger logger = LogManager.getLogger(OpenApiConfiguration.class);
	
	@Value("${fr.insee.rmes.bauhaus.version}")
	private String projectVersion;

	@Bean
	public OpenAPI customOpenAPI() {
		Server server = new Server();
		server.setUrl(Config.getSwaggerUrl());
		logger.info("______________________________________________________________________");
		logger.info("____________________SWAGGER HOST : {}_________________________________________________", Config.getSwaggerHost());
		logger.info("____________________SWAGGER BASEPATH : {} _________________________________________________", Config.getSwaggerBasepath());
		logger.info("____________________SWAGGER CONFIG : {} _________________________________________________", Config.getSwaggerUrl());
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
