package fr.insee.rmes.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

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
			this.getEnvironmentProperties();
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
        loadPropertiesIfExist(props, "bauhaus-dev.properties");
        loadPropertiesIfExist(props, "bauhaus-qf.properties");
        loadPropertiesIfExist(props, "production.properties");
        return props;
    }

	private void loadPropertiesIfExist(Properties props, String fileName) throws FileNotFoundException, IOException {
		File f = new File(String.format(WEBAPPS, System.getProperty(CATALINA_BASE), fileName));
        if(f.exists() && !f.isDirectory()) {
            FileReader r = new FileReader(f);
            props.load(r);
            r.close();
        }
	}

}
