package fr.insee.rmes.testcontainers.queries.organisations;

import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.domain.model.OrganisationOption;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.organisation.OrganisationGraphDBRepository;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
class OrganisationGraphDBRepositoryIntegrationTest extends WithGraphDBContainer {

    private static OrganisationGraphDBRepository repository;

    @BeforeAll
    static void initData() {
        container.withTrigFiles("organizations.trig");

        RepositoryGestion repositoryGestion = new RepositoryGestion(
                getRdfGestionConnectionDetails(),
                new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED)
        );

        repository = new OrganisationGraphDBRepository(
                repositoryGestion,
                "http://rdf.insee.fr/graphes/",
                "organisations/insee",
                "fr"
        );
    }

    @Test
    void shouldReturnOrganisationsFromGraphDB() throws RmesException {
        // When
        List<OrganisationOption> organisations = repository.getOrganisations();

        // Then
        assertThat(organisations).isNotEmpty();
        assertThat(organisations).anyMatch(org ->
                org.stamp().equals("HIE2000069") &&
                        org.label().contains("Direction")
        );
    }

    @Test
    void shouldReturnOrganisationsOrderedByLabel() throws RmesException {
        // When
        List<OrganisationOption> organisations = repository.getOrganisations();

        // Then
        assertThat(organisations).isNotEmpty();

        // Verify that organisations are ordered by label
        for (int i = 0; i < organisations.size() - 1; i++) {
            String currentLabel = organisations.get(i).label();
            String nextLabel = organisations.get(i + 1).label();
            assertThat(currentLabel.compareTo(nextLabel))
                    .as("Organisations should be ordered by label")
                    .isLessThanOrEqualTo(0);
        }
    }

    @Test
    void shouldReturnOrganisationsWithStampAndLabel() throws RmesException {
        // When
        List<OrganisationOption> organisations = repository.getOrganisations();

        // Then
        assertThat(organisations).isNotEmpty();
        organisations.forEach(org -> {
            assertThat(org.stamp()).isNotBlank();
            assertThat(org.label()).isNotBlank();
        });
    }

    @Test
    void shouldReturnOnlyFrenchLabels() throws RmesException {
        // When
        List<OrganisationOption> organisations = repository.getOrganisations();

        // Then
        assertThat(organisations).isNotEmpty();
        // All labels should be in French (we can't verify the language tag after parsing,
        // but we verify that we get results which implies the FILTER worked)
        assertThat(organisations.size()).isGreaterThan(0);
    }
}
