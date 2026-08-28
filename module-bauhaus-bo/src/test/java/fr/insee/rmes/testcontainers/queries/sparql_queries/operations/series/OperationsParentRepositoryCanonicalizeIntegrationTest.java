package fr.insee.rmes.testcontainers.queries.sparql_queries.operations.series;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.bauhaus_services.operations.OperationsParentRepository;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the read-side double-compat behaviour wired via
 * {@link fr.insee.rmes.bauhaus_services.utils.OrganisationLookup#canonicalize}.
 *
 * Covers plan §6 read-side cases (séries):
 * - tout-IRI: the creator IRI is normalised to its short stamp form,
 * - tout-littéral (legacy stamp): the creator is resolved and returned as short form,
 * - mixte: both creators returned in short form,
 * - littéral non résolvable: the creator row is dropped (no null in the list).
 *
 * Series fixtures live in {@code creators-mixed.trig}; organisations live in
 * {@code organizations.trig}.
 */
@Tag("integration")
@AppSpringBootTest
class OperationsParentRepositoryCanonicalizeIntegrationTest extends WithGraphDBContainer {

    private static final String SHORT_HIE_069 = "HIE2000069";
    private static final String SHORT_HIE_076 = "HIE2000076";

    @Autowired
    private OperationsParentRepository operationsParentRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.sesameServer", () -> getRdfGestionConnectionDetails().getUrlServer());
        registry.add("fr.insee.rmes.bauhaus.sesame.gestion.repository", () -> getRdfGestionConnectionDetails().repositoryId());
    }

    @BeforeAll
    static void initData() {
        container.withTrigFiles("organizations.trig");
        container.withTrigFiles("creators-mixed.trig");
    }

    @Test
    void getSeriesCreators_returnsShortStamp_whenStoredAsIri() throws RmesException {
        JSONArray creators = operationsParentRepository.getSeriesCreators("sIRI");

        assertThat(toCreatorList(creators)).containsExactly(SHORT_HIE_069);
    }

    @Test
    void getSeriesCreators_resolvesLegacyLiteralToShortStamp() throws RmesException {
        JSONArray creators = operationsParentRepository.getSeriesCreators("sLIT");

        assertThat(toCreatorList(creators)).containsExactly(SHORT_HIE_069);
    }

    @Test
    void getSeriesCreators_returnsBothInShortForm_whenMixed() throws RmesException {
        JSONArray creators = operationsParentRepository.getSeriesCreators("sMIX");

        assertThat(toCreatorList(creators)).containsExactlyInAnyOrder(
                SHORT_HIE_069,
                SHORT_HIE_076
        );
    }

    @Test
    void getSeriesCreators_dropsRowsWithUnresolvableLiterals() throws RmesException {
        JSONArray creators = operationsParentRepository.getSeriesCreators("sBAD");

        assertThat(toCreatorList(creators)).isEmpty();
    }

    private static List<String> toCreatorList(JSONArray array) {
        List<String> list = new ArrayList<>();
        list.addAll(JSONUtils.jsonArrayToList(array));
        return list;
    }
}
