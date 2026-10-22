package fr.insee.rmes.testcontainers.queries.sparql_queries.operations.series;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.modules.operation.series.infrastructure.PublicationToGestionIriRewriter;
import fr.insee.rmes.modules.operation.series.infrastructure.graphdb.GraphDbSeriesCreatorsAdapter;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Créateurs des séries tels que les compare le filtrage STAMP des fichiers DDI : ils doivent
 * s'écrire comme le timbre de l'utilisateur ({@code HIE…}), que {@code dc:creator} soit stocké en
 * IRI d'organisation ou en littéral. Même règle que {@code getSeriesWithStampQuery} (ticket 1478).
 *
 * <p>Séries dans {@code creators-mixed.trig}, organisations dans {@code organizations.trig}.</p>
 */
@Tag("integration")
class SeriesCreatorsIntegrationTest extends WithGraphDBContainer {

    private static final String PUBLICATION_SERIES = "http://id.insee.fr/operations/serie/";

    private final GraphDbSeriesCreatorsAdapter adapter = new GraphDbSeriesCreatorsAdapter(
            new RepositoryGestion(
                    getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED)),
            GraphsPropertiesStub.stub(),
            new PublicationToGestionIriRewriter("http://id.insee.fr/", "http://bauhaus/"));

    @BeforeAll
    static void initData() {
        container.withTrigFiles("organizations.trig");
        container.withTrigFiles("creators-mixed.trig");
    }

    @Test
    void should_return_the_organisation_identifier_when_the_creator_is_stored_as_an_iri() {
        Map<String, List<String>> creators = adapter.getCreatorsForSeries(List.of(PUBLICATION_SERIES + "sIRI"));

        assertThat(creators.get(PUBLICATION_SERIES + "sIRI")).containsExactly("HIE2000069");
    }

    @Test
    void should_keep_a_creator_stored_as_a_literal() {
        Map<String, List<String>> creators = adapter.getCreatorsForSeries(List.of(PUBLICATION_SERIES + "sLIT"));

        assertThat(creators.get(PUBLICATION_SERIES + "sLIT")).containsExactly("HIE2000069");
    }

    @Test
    void should_return_both_creators_when_iri_and_literal_are_mixed() {
        Map<String, List<String>> creators = adapter.getCreatorsForSeries(List.of(PUBLICATION_SERIES + "sMIX"));

        assertThat(creators.get(PUBLICATION_SERIES + "sMIX")).containsExactlyInAnyOrder("HIE2000069", "HIE2000076");
    }
}
