package fr.insee.rmes.modules.organisations;

import fr.insee.rmes.modules.organisations.domain.exceptions.OrganisationFetchException;
import fr.insee.rmes.modules.organisations.domain.port.serverside.OrganisationsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Le contrôle doit faire échouer le démarrage du contexte, et rester débrayable par propriété
 * (les contextes de test n'ont pas de base de gestion joignable).
 */
class DefaultContributorValidatorWiringTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(RepositoryConfiguration.class, DefaultContributorValidator.class);

    @Test
    void should_fail_the_context_startup_when_the_contributor_is_unknown() {
        contextRunner
                .withPropertyValues("fr.insee.rmes.bauhaus.defaultContributor=http://bauhaus/organisations/insee/UNKNOWN")
                .run(context -> assertThat(context)
                        .hasFailed()
                        .getFailure()
                        .rootCause()
                        .isInstanceOf(InvalidDefaultContributorException.class)
                        .hasMessageContaining("does not exist in the management database"));
    }

    @Test
    void should_start_when_the_contributor_exists() {
        contextRunner
                .withPropertyValues("fr.insee.rmes.bauhaus.defaultContributor=http://bauhaus/organisations/insee/HIE3014990")
                .run(context -> assertThat(context).hasSingleBean(DefaultContributorValidator.class));
    }

    @Test
    void should_be_disabled_by_property() {
        contextRunner
                .withPropertyValues(
                        "fr.insee.rmes.bauhaus.defaultContributor=http://bauhaus/organisations/insee/UNKNOWN",
                        "fr.insee.rmes.bauhaus.default-contributor-check-enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(DefaultContributorValidator.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class RepositoryConfiguration {
        @Bean
        OrganisationsRepository organisationsRepository() throws OrganisationFetchException {
            OrganisationsRepository repository = mock(OrganisationsRepository.class);
            when(repository.checkIfOrganisationExists(anyString())).thenReturn(false);
            when(repository.checkIfOrganisationExists("http://bauhaus/organisations/insee/HIE3014990")).thenReturn(true);
            return repository;
        }
    }
}
