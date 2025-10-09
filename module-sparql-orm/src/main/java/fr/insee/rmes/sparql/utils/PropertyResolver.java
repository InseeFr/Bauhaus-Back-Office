package fr.insee.rmes.sparql.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class PropertyResolver {
    
    private static Environment environment;
    
    @Autowired
    public void setEnvironment(final Environment env) {
        environment = env;
    }
    
    public static String resolve(final String value) {
        if (null == environment || null == value) {
            return value;
        }
        
        // Résoudre les expressions Spring ${...} multiples
        String resolved = value;
        while (resolved.contains("${")) {
            final int start = resolved.indexOf("${");
            final int end = resolved.indexOf('}', start);
            if (-1 == end) break;
            
            final String propertyName = resolved.substring(start + 2, end);
            final String propertyValue = PropertyResolver.environment.getProperty(propertyName, "");
            resolved = resolved.substring(0, start) + propertyValue + resolved.substring(end + 1);
        }
        
        return resolved;
    }
}