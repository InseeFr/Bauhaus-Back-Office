package fr.insee.rmes.config.auth.security.conditions;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OpenIDConnectAuthCondition implements Condition {

	private String env;

	private static final String WEBAPPS = "%s/webapps/%s";
	private static final String CATALINA_BASE = "catalina.base";
	
	private static final Logger logger = LoggerFactory.getLogger(OpenIDConnectAuthCondition.class);

	
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		try {
			env = getEnv();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return  (env.equals("pre-prod") || env.equals("prod"));
	}

	private String getEnv() throws IOException {
        Properties props = new Properties();
        props.load(getClass()
                .getClassLoader()
                .getResourceAsStream("bauhaus-dev.properties"));
        loadPropertiesIfExist(props, "bauhaus-dev.properties");
        loadPropertiesIfExist(props, "bauhaus-qf.properties");
        loadPropertiesIfExist(props, "bauhaus-production.properties");
        loadPropertiesIfExist(props, "production.properties");
        return props.getProperty("fr.insee.rmes.bauhaus.env");
    }

	
	private void loadPropertiesIfExist(Properties props, String fileName) throws IOException {
		File f = new File(String.format(WEBAPPS, System.getProperty(CATALINA_BASE), fileName));
        if(f.exists() && !f.isDirectory()) {
            try(FileReader r = new FileReader(f)){
            	props.load(r);
            }
        }
	}
}
