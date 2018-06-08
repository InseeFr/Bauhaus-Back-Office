package fr.insee.rmes.config.swagger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.rmes.config.Config;
import io.swagger.jaxrs.config.BeanConfig;

@SuppressWarnings("serial")
public class SwaggerConfig extends HttpServlet {

	private final static Logger logger = LogManager.getLogger(SwaggerConfig.class);

	@Override
	public void init(ServletConfig config) throws ServletException {
		try {
			super.init(config);
			BeanConfig beanConfig = new BeanConfig();
			beanConfig.setTitle("Bauhaus API");
			beanConfig.setVersion("1.1.2");
			beanConfig.setDescription("Rest Endpoints and services Integration used by Bauhaus");
			beanConfig.setSchemes(new String[] { Config.REQUIRES_SSL ? "https" : "http" });
			beanConfig.setBasePath("/" + Config.SWAGGER_BASEPATH);
			beanConfig.setHost(Config.SWAGGER_HOST);
			beanConfig.setResourcePackage("fr.insee.rmes.webservice");
			beanConfig.setScan(true);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}

}