package fr.insee.rmes.testcontainers.utils;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.bauhaus_services.utils.OrganisationLookup;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link OrganisationLookup} backed by a real GraphDB container.
 * Covers the three behaviours of the helper as specified in the migration plan §6:
 * - IRI passthrough (no repository call needed for known IRIs in graph),
 * - legacy stamp literal resolved via adms:identifier lookup,
 * - unknown stamp/IRI silently dropped.
 */
@Tag("integration")
@AppSpringBootTest
class OrganisationLookupIntegrationTest extends WithGraphDBContainer {

    private static final String KNOWN_IRI = "http://bauhaus/organisations/insee/HIE2000069";
    private static final String KNOWN_STAMP = "HIE2000069";

    @Autowired
    private OrganisationLookup organisationLookup;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.sesameServer", () -> getRdfGestionConnectionDetails().getUrlServer());
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.repository", () -> getRdfGestionConnectionDetails().repositoryId());
    }

    @BeforeAll
    static void initData() {
        container.withTrigFiles("organizations.trig");
    }

    @Test
    void resolve_returnsIriUnchanged_whenInputIsAlreadyAnIri() throws RmesException {
        Optional<String> result = organisationLookup.resolve(KNOWN_IRI);

        assertThat(result).contains(KNOWN_IRI);
    }

    @Test
    void resolve_resolvesLegacyStampToIri_whenStampIsKnown() throws RmesException {
        Optional<String> result = organisationLookup.resolve(KNOWN_STAMP);

        assertThat(result).contains(KNOWN_IRI);
    }

    @Test
    void resolve_returnsEmpty_whenStampIsUnknown() throws RmesException {
        Optional<String> result = organisationLookup.resolve("UNKNOWN-STAMP-XYZ");

        assertThat(result).isEmpty();
    }

    @Test
    void findUnknown_returnsOnlyTheUnknownInputs_acrossMixedFormats() throws RmesException {
        List<String> unknown = organisationLookup.findUnknown(List.of(
                KNOWN_IRI,
                KNOWN_STAMP,
                "http://bauhaus/organisations/insee/DOES_NOT_EXIST",
                "ANOTHER-MISSING-STAMP"
        ));

        assertThat(unknown).containsExactlyInAnyOrder(
                "http://bauhaus/organisations/insee/DOES_NOT_EXIST",
                "ANOTHER-MISSING-STAMP"
        );
    }

    @Test
    void findUnknown_handlesBatchOfHundredInputsWithoutRegression() throws RmesException {
        // Plan §6: 100 inputs in a reasonable time - assert the call returns without error.
        List<String> inputs = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            inputs.add(KNOWN_IRI);
            inputs.add("MISSING-" + i);
        }

        long start = System.currentTimeMillis();
        List<String> unknown = organisationLookup.findUnknown(inputs);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(unknown).hasSize(50);
        assertThat(elapsed)
                .as("100 inputs should resolve without a N+1 explosion (soft limit)")
                .isLessThan(10_000);
    }
}
