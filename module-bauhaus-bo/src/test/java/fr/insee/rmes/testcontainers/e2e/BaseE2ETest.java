package fr.insee.rmes.testcontainers.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.modules.ddi.operations_mirror.OperationsMirrorConfiguration;
import fr.insee.rmes.testcontainers.GraphDbTestProperties;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;

@Tag("integration")
@AppSpringBootTest
public abstract class BaseE2ETest extends WithGraphDBContainer {

    @LocalServerPort
    protected int port;

    @Autowired
    protected RestTestClient restTestClient;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        GraphDbTestProperties.registerGestion(registry);
        // Aucune instance Colectica dans ces tests : le miroir DDI synchrone (actif par défaut
        // dans colectica.yml) ferait échouer en 500 toute création de série ou d'opération.
        registry.add(OperationsMirrorConfiguration.ENABLED_PROPERTY, () -> "false");
    }

    @BeforeAll
    static void initData() {
        container.withTrigFiles("all-operations-and-indicators.trig");
    }
}
