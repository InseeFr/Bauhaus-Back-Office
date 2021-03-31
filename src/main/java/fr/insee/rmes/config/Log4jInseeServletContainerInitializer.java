package fr.insee.rmes.config;


import fr.insee.rmes.utils.PropertiesUtils;
import org.apache.logging.log4j.web.Log4jServletContainerInitializer;
import org.apache.logging.log4j.web.Log4jWebSupport;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class Log4jInseeServletContainerInitializer extends Log4jServletContainerInitializer {

    private static final String WEBAPPS = "%s/webapps/%s";

    private static final String CATALINA_BASE = "catalina.base";

    @Override
    public void onStartup(final Set<Class<?>> classes, final ServletContext servletContext) throws ServletException {
        servletContext.setInitParameter(Log4jWebSupport.LOG4J_CONFIG_LOCATION,findLog4jConfFile());
        switchOnLog4jServletContainerInitializer(servletContext);
        super.onStartup(classes, servletContext);
        switchOffLog4jServletContainerInitializer(servletContext);
    }

    private void switchOnLog4jServletContainerInitializer(ServletContext servletContext) {
        servletContext.setInitParameter(Log4jWebSupport.IS_LOG4J_AUTO_INITIALIZATION_DISABLED,"false");
    }

    private void switchOffLog4jServletContainerInitializer(ServletContext servletContext) {
        servletContext.setInitParameter(Log4jWebSupport.IS_LOG4J_AUTO_INITIALIZATION_DISABLED,"true");
    }

    private String findLog4jConfFile() {

        Path bauhausDevPropsInClassPath;
        try {
            bauhausDevPropsInClassPath=Paths.get(getClass().getClassLoader().getResource("bauhaus-dev.properties").toURI());
        } catch (URISyntaxException e) {
            bauhausDevPropsInClassPath=null;
        }
        return Stream.of(Paths.get(String.format(WEBAPPS, System.getProperty(CATALINA_BASE), "production.properties")),
                Paths.get(String.format(WEBAPPS, System.getProperty(CATALINA_BASE), "bauhaus-production.properties")),
                Paths.get(String.format(WEBAPPS, System.getProperty(CATALINA_BASE), "bauhaus-qf.properties")),
                Paths.get(String.format(WEBAPPS, System.getProperty(CATALINA_BASE), "bauhaus-dev.properties")),
                bauhausDevPropsInClassPath)
                .filter(Objects::nonNull)
                .map(p->PropertiesUtils.readPropertyFromFile("fr.insee.rmes.bauhaus.log.configuration",p))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse("log4j2.xml");
    }
}
