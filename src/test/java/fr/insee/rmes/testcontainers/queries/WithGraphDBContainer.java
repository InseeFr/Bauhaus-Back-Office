package fr.insee.rmes.testcontainers.queries;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;


@Testcontainers
public class WithGraphDBContainer {
    @Container
    static GraphDBContainer container = new GraphDBContainer("ontotext/graphdb:10.6.4");

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        container.withInitFolder("/testcontainers").withRepository("config.ttl");
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String sesameServer = "http://" + container.getHost() + ":" + container.getMappedPort(7200);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.sesameServer", () -> sesameServer);
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.repository", () -> "bauhaus-test");
    }
}
