package fr.insee.rmes.config.swagger;

import fr.insee.rmes.config.Config;
import io.swagger.v3.jaxrs2.integration.OpenApiServlet;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;


@SuppressWarnings("serial")
@OpenAPIDefinition(
		info = @Info(title="Bauhaus API", version="2.0.0", description="Rest Endpoints and services Integration used by Bauhaus"),
		servers = {
		        @Server(
		                description = "API server",
		                url = "http://localhost:6969/Bauhaus-Back-Office/api" // Config.SWAGGER_URL
		               )
		        }
)
public class SwaggerConfig extends OpenApiServlet  {
	//TODO find a way to configure api definition via servlet and not with the above annotation
	
	
	/*private final static Logger logger = LogManager.getLogger(SwaggerConfig.class);

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		OpenAPI openApi = new OpenAPI();

		Info info = new Info();
		info.setTitle("Bauhaus API");
		info.setVersion("2.0.0");
		info.setDescription("Rest Endpoints and services Integration used by Bauhaus");
		openApi.info(info);

		Server server = new Server();
		server.url(Config.SWAGGER_URL);
		openApi.addServersItem(server);

		SwaggerConfiguration oasConfig = new SwaggerConfiguration().openAPI(openApi)
				.resourcePackages(Stream.of("fr.insee.rmes.webservice").collect(Collectors.toSet()))
				.prettyPrint(true);

		logger.debug("SWAGGER : " + oasConfig.toString());
		try {
			new JaxrsOpenApiContextBuilder<>().servletConfig(config).openApiConfiguration(oasConfig).buildContext(true);
		} catch (OpenApiConfigurationException e) {
			throw new ServletException(e.getMessage(), e);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		
	}*/

}