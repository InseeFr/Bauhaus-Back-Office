package fr.insee.rmes.config.auth.conditions;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class NoAuthCondition implements Condition {

	private String env;

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		try {
			env = getEnv();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!env.matches("qf|pre-prod|prod"))
			return true;
		return false;
	}

	private String getEnv() throws IOException {
        Properties props = new Properties();
        props.load(getClass()
                .getClassLoader()
                .getResourceAsStream("bauhaus-dev.properties"));
        File f = new File(String.format("%s/webapps/%s", System.getProperty("catalina.base"), "bauhaus-dev.properties"));
        if(f.exists() && !f.isDirectory()) {
            FileReader r = new FileReader(f);
            props.load(r);
            r.close();
        }
        File f2 = new File(String.format("%s/webapps/%s", System.getProperty("catalina.base"), "bauhaus-qf.properties"));
        if(f2.exists() && !f2.isDirectory()) {
            FileReader r2 = new FileReader(f2);
            props.load(r2);
            r2.close();
        }
        File f3 = new File(String.format("%s/webapps/%s", System.getProperty("catalina.base"), "production.properties"));
        if(f3.exists() && !f3.isDirectory()) {
            FileReader r3 = new FileReader(f3);
            props.load(r3);
            r3.close();
        }
        return props.getProperty("fr.insee.rmes.bauhaus.env");
    }

}
