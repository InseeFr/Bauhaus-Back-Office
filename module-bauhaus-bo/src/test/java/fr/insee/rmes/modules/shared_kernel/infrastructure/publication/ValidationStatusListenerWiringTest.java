package fr.insee.rmes.modules.shared_kernel.infrastructure.publication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Tant que le statut de publication n'est écrit que par l'écouteur, un contexte qui en serait privé
 * publierait des objets restant « Unpublished » sans la moindre erreur : le bean doit exister quelle
 * que soit la configuration des modules.
 */
class ValidationStatusListenerWiringTest {

    private static final IRI OBJECT = SimpleValueFactory.getInstance().createIRI("http://bauhaus/object");
    private static final IRI GRAPH = SimpleValueFactory.getInstance().createIRI("http://bauhaus/graph");

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(ScanningConfiguration.class);

    @ParameterizedTest
    @ValueSource(
            strings = {
                "fr.insee.rmes.bauhaus.unrelated=none",
                "fr.insee.rmes.bauhaus.modules.operations.enabled=false",
                "fr.insee.rmes.bauhaus.modules.concepts.enabled=true"
            })
    void should_always_register_the_listener_and_deliver_the_event_to_it(String property) {
        contextRunner.withPropertyValues(property).run(context -> {
            assertThat(context).hasSingleBean(ValidationStatusListener.class);

            context.publishEvent(new ObjectPublished(OBJECT, GRAPH));

            verify(context.getBean(RepositoryGestion.class))
                    .deleteTripletByPredicate(OBJECT, INSEE.VALIDATION_STATE, GRAPH);
        });
    }

    @Configuration(proxyBeanMethods = false)
    @ComponentScan(basePackageClasses = ValidationStatusListener.class)
    static class ScanningConfiguration {
        @Bean
        RepositoryGestion repositoryGestion() {
            return mock(RepositoryGestion.class);
        }
    }
}
