package fr.insee.rmes.testcontainers.queries.sparql_queries.operations.series;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
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
 * - tout-IRI: the creator stays an IRI in the JSON output,
 * - tout-littéral (legacy stamp): the creator is resolved to its canonical IRI,
 * - mixte: both IRIs returned, one resolved and one passe-plat,
 * - littéral non résolvable: the creator row is dropped (no null in the list).
 *
 * Series fixtures live in {@code creators-mixed.trig}; organisations live in
 * {@code organizations.trig}.
 */
@Tag("integration")
@AppSpringBootTest
class ParentUtilsCanonicalizeIntegrationTest extends WithGraphDBContainer {

    private static final String CANONICAL_HIE_069 = "http://bauhaus/organisations/insee/HIE2000069";
    private static final String CANONICAL_HIE_076 = "http://bauhaus/organisations/insee/HIE2000076";

    @Autowired
    private ParentUtils parentUtils;

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
    void getSeriesCreators_returnsIriUnchanged_whenStoredAsIri() throws RmesException {
        JSONArray creators = parentUtils.getSeriesCreators("sIRI");

        assertThat(toCreatorList(creators)).containsExactly(CANONICAL_HIE_069);
    }

    @Test
    void getSeriesCreators_resolvesLegacyLiteralToIri() throws RmesException {
        JSONArray creators = parentUtils.getSeriesCreators("sLIT");

        assertThat(toCreatorList(creators)).containsExactly(CANONICAL_HIE_069);
    }

    @Test
    void getSeriesCreators_returnsBothFormatsAsIris_whenMixed() throws RmesException {
        JSONArray creators = parentUtils.getSeriesCreators("sMIX");

        assertThat(toCreatorList(creators)).containsExactlyInAnyOrder(
                CANONICAL_HIE_069,
                CANONICAL_HIE_076
        );
    }

    @Test
    void getSeriesCreators_dropsRowsWithUnresolvableLiterals() throws RmesException {
        JSONArray creators = parentUtils.getSeriesCreators("sBAD");

        assertThat(toCreatorList(creators)).isEmpty();
    }

    private static List<String> toCreatorList(JSONArray array) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            list.add(array.getString(i));
        }
        return list;
    }
}
