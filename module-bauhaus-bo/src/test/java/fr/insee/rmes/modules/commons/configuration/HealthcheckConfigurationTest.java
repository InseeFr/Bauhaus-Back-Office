package fr.insee.rmes.modules.commons.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.modules.commons.healthcheck.DependencyProbe;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/** La sonde Colectica du healthcheck se coupe par propriété, pour les environnements sans Colectica (e2e). */
class HealthcheckConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(HealthcheckConfiguration.class)
            .withBean(ColecticaClient.class, () -> mock(ColecticaClient.class));

    @Test
    void shouldProbeColecticaByDefault() {
        contextRunner.run(context -> assertThat(context)
                .getBean(DependencyProbe.class)
                .extracting(DependencyProbe::name)
                .isEqualTo("Colectica"));
    }

    @Test
    void shouldNotProbeColecticaWhenDisabled() {
        contextRunner
                .withPropertyValues("fr.insee.rmes.bauhaus.colectica.healthcheck-enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(DependencyProbe.class));
    }
}
