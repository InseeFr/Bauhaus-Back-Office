package fr.insee.rmes.config.freemarker;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FreemarkerConfigTest {

    @Test
    void shouldInitFreemarkerConfig(){
        var begin= System.nanoTime();
        FreemarkerConfig.init();
        var end = System. nanoTime();
        assertTrue(end-begin<=1000000000);
    }
}