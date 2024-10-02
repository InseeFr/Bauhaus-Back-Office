package fr.insee.rmes.config;

import fr.insee.rmes.config.auth.AccessControlConfiguration;
import fr.insee.rmes.config.auth.RBACConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = "spring.config.additional-location=classpath:rbac-test.yml")
@ContextConfiguration(classes = {AccessControlConfiguration.class})
@EnableConfigurationProperties(RBACConfiguration.class)
public @interface RBACTest {
}
