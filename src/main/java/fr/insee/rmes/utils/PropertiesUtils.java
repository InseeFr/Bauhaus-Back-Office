
package fr.insee.rmes.utils;

import fr.insee.rmes.bauhaus_services.rdf_utils.UriUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public record PropertiesUtils (Environment environment) implements UriUtils.PropertiesFinder {

    public Optional<String> findByName(String name){
        if (name==null){
            return Optional.empty();
        }
        return Optional.ofNullable(this.environment.getProperty(name));
    }

}
