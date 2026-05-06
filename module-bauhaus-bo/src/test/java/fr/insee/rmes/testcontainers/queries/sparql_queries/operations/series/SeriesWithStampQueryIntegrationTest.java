package fr.insee.rmes.testcontainers.queries.sparql_queries.operations.series;

import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.modules.users.domain.model.Stamp;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link OperationSeriesQueries#seriesWithStampQuery(Set, boolean)}.
 *
 * <p>Reproduces the bug behind ticket 1478: a HIE survey manager was unable to list
 * the series for which they were declared as creator. In production, {@code dc:creator}
 * is stored as an organisation IRI ({@code http://bauhaus/organisations/insee/HIE2000069}),
 * while the user's {@code Set<Stamp>} contains the short identifier
 * ({@code HIE2000069}). The original FILTER compared {@code STR(?creators)} (the IRI)
 * to the short identifier and never matched.</p>
 *
 * <p>Series fixtures live in {@code creators-mixed.trig}; organisations live in
 * {@code organizations.trig}.</p>
 */
@Tag("integration")
class SeriesWithStampQueryIntegrationTest extends WithGraphDBContainer {

    private final RepositoryGestion repositoryGestion =
            new RepositoryGestion(getRdfGestionConnectionDetails(),
                    new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    private OperationSeriesQueries operationSeriesQueries;

    @BeforeAll
    static void initData() {
        container.withTrigFiles("organizations.trig");
        container.withTrigFiles("creators-mixed.trig");
    }

    @BeforeEach
    void setUp() {
        operationSeriesQueries = new OperationSeriesQueries(new ConfigStub());
    }

    @Test
    void should_return_series_when_creator_is_stored_as_organisation_iri() throws RmesException {
        Set<Stamp> stamps = Set.of(new Stamp("HIE2000069"));

        List<String> ids = idsOf(operationSeriesQueries.seriesWithStampQuery(stamps, false));

        // sIRI : dc:creator <http://bauhaus/organisations/insee/HIE2000069> (IRI form)
        // sLIT : dc:creator "HIE2000069" (legacy literal stamp)
        // sMIX : has the IRI form among its creators
        assertThat(ids).contains("sIRI", "sLIT", "sMIX");
    }

    @Test
    void should_not_return_series_with_unrelated_creator() throws RmesException {
        Set<Stamp> stamps = Set.of(new Stamp("HIE2000069"));

        List<String> ids = idsOf(operationSeriesQueries.seriesWithStampQuery(stamps, false));

        // sBAD : dc:creator "DOES_NOT_EXIST" (unresolvable literal)
        assertThat(ids).doesNotContain("sBAD");
    }

    @Test
    void admin_sees_all_series_regardless_of_stamps() throws RmesException {
        List<String> ids = idsOf(operationSeriesQueries.seriesWithStampQuery(Set.of(), true));

        assertThat(ids).contains("sIRI", "sLIT", "sMIX", "sBAD");
    }

    private List<String> idsOf(String query) throws RmesException {
        JSONArray result = repositoryGestion.getResponseAsArray(query);
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < result.length(); i++) {
            JSONObject row = result.getJSONObject(i);
            if (row.has("id")) {
                ids.add(row.getString("id"));
            }
        }
        return ids;
    }
}
