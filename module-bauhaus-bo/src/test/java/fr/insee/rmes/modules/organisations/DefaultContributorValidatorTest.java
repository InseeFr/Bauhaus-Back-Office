package fr.insee.rmes.modules.organisations;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import fr.insee.rmes.modules.organisations.domain.exceptions.OrganisationFetchException;
import fr.insee.rmes.modules.organisations.domain.port.serverside.OrganisationsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultContributorValidatorTest {

    private static final String VALID_IRI = "http://bauhaus/organisations/insee/HIE3014990";

    @Mock
    private OrganisationsRepository organisationsRepository;

    @Test
    void should_accept_an_iri_that_exists_in_the_management_database() throws OrganisationFetchException {
        when(organisationsRepository.checkIfOrganisationExists(VALID_IRI)).thenReturn(true);
        DefaultContributorValidator validator = validatorFor(VALID_IRI);

        assertThatCode(validator::validate).doesNotThrowAnyException();
    }

    @Test
    void should_reject_an_iri_absent_from_the_management_database() throws OrganisationFetchException {
        when(organisationsRepository.checkIfOrganisationExists(VALID_IRI)).thenReturn(false);
        DefaultContributorValidator validator = validatorFor(VALID_IRI);

        assertThatThrownBy(validator::validate)
                .isInstanceOf(InvalidDefaultContributorException.class)
                .hasMessageContaining("fr.insee.rmes.bauhaus.defaultContributor")
                .hasMessageContaining(VALID_IRI)
                .hasMessageContaining("does not exist in the management database");
    }

    @Test
    void should_reject_a_value_that_is_not_an_absolute_uri() {
        DefaultContributorValidator validator = validatorFor("HIE3014990");

        assertThatThrownBy(validator::validate)
                .isInstanceOf(InvalidDefaultContributorException.class)
                .hasMessageContaining("fr.insee.rmes.bauhaus.defaultContributor")
                .hasMessageContaining("HIE3014990")
                .hasMessageContaining("URI absolue");
    }

    @Test
    void should_reject_a_malformed_uri() {
        DefaultContributorValidator validator = validatorFor("http://bauhaus/organisations/ insee");

        assertThatThrownBy(validator::validate)
                .isInstanceOf(InvalidDefaultContributorException.class)
                .hasMessageContaining("URI absolue");
    }

    @Test
    void should_reject_a_blank_value() {
        DefaultContributorValidator validator = validatorFor("   ");

        assertThatThrownBy(validator::validate)
                .isInstanceOf(InvalidDefaultContributorException.class)
                .hasMessageContaining("est vide");
    }

    @Test
    void should_reject_when_the_management_database_cannot_answer() throws OrganisationFetchException {
        OrganisationFetchException cause = new OrganisationFetchException();
        when(organisationsRepository.checkIfOrganisationExists(VALID_IRI)).thenThrow(cause);
        DefaultContributorValidator validator = validatorFor(VALID_IRI);

        assertThatThrownBy(validator::validate)
                .isInstanceOf(InvalidDefaultContributorException.class)
                .hasMessageContaining("n'a pas pu être vérifié")
                .hasCause(cause);
    }

    private DefaultContributorValidator validatorFor(String defaultContributor) {
        return new DefaultContributorValidator(defaultContributor, organisationsRepository);
    }
}
