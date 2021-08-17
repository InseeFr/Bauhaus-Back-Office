package fr.insee.rmes.config.swagger;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletConfig;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.DependsOn;

import fr.insee.rmes.config.Config;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@ApplicationPath("/")
@DependsOn("AppContext")
public class SwaggerConfig extends ResourceConfig   {

	private static final  Logger logger = LogManager.getLogger(SwaggerConfig.class);

	public SwaggerConfig(@Context ServletConfig servletConfig) {
		super();
		OpenAPI openApi = new OpenAPI();

		Info info = new Info().title("Bauhaus API").version("3.0.6").description("Rest Endpoints and services Integration used by Bauhaus");
		openApi.info(info);

		Server server = new Server();
		logger.info("______________________________________________________________________");
		logger.info("____________________SWAGGER HOST : {}_________________________________________________", Config.SWAGGER_HOST);
		logger.info("____________________SWAGGER BASEPATH : {} _________________________________________________", Config.SWAGGER_BASEPATH);
		logger.info("____________________SWAGGER CONFIG : {} _________________________________________________", Config.SWAGGER_URL);
		logger.info("______________________________________________________________________");
		server.url(Config.SWAGGER_URL);
		openApi.addServersItem(server);

		SwaggerConfiguration oasConfig = new SwaggerConfiguration().openAPI(openApi)
				.resourcePackages(Stream.of("fr.insee.rmes.webservice").collect(Collectors.toSet()))
				.prettyPrint(true);
		String oasConfigString = oasConfig.toString();
		logger.debug("SWAGGER : {}", oasConfigString);
		
		OpenApiResource openApiResource = new OpenApiResource();
 		openApiResource.setOpenApiConfiguration(oasConfig);
		register(openApiResource);
		register(MultiPartFeature.class);
		
	}

}