package fr.insee.rmes.config.freemarker;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FreemarkerConfigTest {

    @Test
    public void shouldInitFreemarkerConfig(){
        StopWatch watch = new StopWatch();
        watch.start();
        FreemarkerConfig.init();
        watch.stop();
        assertTrue(watch.getTime()<=500);
    }
}