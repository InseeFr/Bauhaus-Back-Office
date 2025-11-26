package fr.insee.rmes.config;

import fr.insee.rmes.modules.commons.configuration.PropertiesLogger;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PropertiesLoggerTest {

    @Test
    void shouldCompareEnumPrefix(){
        java.util.List<String> properties = List.of(PropertiesLogger.PROPERTY_KEY_FOR_PREFIXES,
                PropertiesLogger.PROPERTY_KEY_FOR_SOURCES_IGNORED,
                PropertiesLogger.PROPERTY_KEY_FOR_PREFIXES,
                PropertiesLogger.PROPERTY_KEY_FOR_SOURCES_SELECT);
        String prefix = "fr.insee.properties.log.";
        int numberOfPropertiesWhichBeginWithThePrefix=0;
        for(String property : properties){
            if(property.startsWith(prefix)){
                numberOfPropertiesWhichBeginWithThePrefix=numberOfPropertiesWhichBeginWithThePrefix+1;
            }
        }
        assertEquals(4,numberOfPropertiesWhichBeginWithThePrefix);

    }
}