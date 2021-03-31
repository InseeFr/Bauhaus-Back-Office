package fr.insee.rmes.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.web.Log4jServletContextListener;
import org.apache.logging.log4j.web.Log4jWebSupport;

public class Log4j2ServletContextListener implements ServletContextListener {

	private static final String WEBAPPS = "%s/webapps/%s";

	private static final String CATALINA_BASE = "catalina.base";

	private String log4j2ConfigFile;

	private Log4jServletContextListener listener;

	public Log4j2ServletContextListener() {
		this.listener = new Log4jServletContextListener();
		try {
			//Get the appropriate log4j2.xml file
			this.getEnvironmentProperties();

			//Force a reconfiguration
			//Documentation on https://logging.apache.org/log4j/log4j-2.5/faq.html
			if (!log4j2ConfigFile.equals("log4j2.xml")){//case of external file
				LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
				File file = new File(log4j2ConfigFile);
				context.setConfigLocation(file.toURI());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		servletContextEvent.getServletContext().setInitParameter(Log4jWebSupport.LOG4J_CONFIG_LOCATION,
				log4j2ConfigFile);
		listener.contextInitialized(servletContextEvent);
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		listener.contextDestroyed(servletContextEvent);
	}

	private void getEnvironmentProperties() throws IOException {
		Properties props = this.getProperties();
		String log4JExternalFile = props.getProperty("fr.insee.rmes.bauhaus.log.configuration");
		this.log4j2ConfigFile = "log4j2.xml";
		File f = new File(log4JExternalFile);
		if (f.exists() && !f.isDirectory()) {
			this.log4j2ConfigFile = String.format(log4JExternalFile);
		}
	}
	
	private Properties getProperties() throws IOException {
        Properties props = new Properties();
        props.load(getClass().getClassLoader().getResourceAsStream("bauhaus-dev.properties"));
        props = this.loadIfExists(props, "bauhaus-dev.properties");
        props = this.loadIfExists(props, "bauhaus-qf.properties");
        props = this.loadIfExists(props, "bauhaus-production.properties");
        props = this.loadIfExists(props, "production.properties");
        return props;
    }

	/*
	 * load properties on catalina base
	 */
	private Properties loadIfExists(Properties props, String filename) throws IOException {
		File f = new File(String.format(WEBAPPS, System.getProperty(CATALINA_BASE), filename));
		if (f.exists() && !f.isDirectory()) {
			try (FileReader r = new FileReader(f);){ 
				props.load(r);
				return props;
			}
		}
		return props;
	}


}
