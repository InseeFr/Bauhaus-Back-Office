package fr.insee.rmes.graphdb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class PropertyResolver {
    
    private static Environment environment;
    
    @Autowired
    public void setEnvironment(Environment env) {
        PropertyResolver.environment = env;
    }
    
    public static String resolve(String value) {
        if (environment == null || value == null) {
            return value;
        }
        
        // Résoudre les expressions Spring ${...} multiples
        String resolved = value;
        while (resolved.contains("${")) {
            int start = resolved.indexOf("${");
            int end = resolved.indexOf("}", start);
            if (end == -1) break;
            
            String propertyName = resolved.substring(start + 2, end);
            String propertyValue = environment.getProperty(propertyName, "");
            resolved = resolved.substring(0, start) + propertyValue + resolved.substring(end + 1);
        }
        
        return resolved;
    }
}