package fr.insee.rmes.testcontainers.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.testcontainers.queries.WithGraphDBContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@Tag("integration")
@AppSpringBootTest
public abstract class BaseE2ETest extends WithGraphDBContainer {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;
    
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.sesameServer", () -> getRdfGestionConnectionDetails().getUrlServer());
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.repository", () -> getRdfGestionConnectionDetails().repositoryId());
    }

    @BeforeAll
    static void initData(){
        container.withTrigFiles("all-operations-and-indicators.trig");
    }
}