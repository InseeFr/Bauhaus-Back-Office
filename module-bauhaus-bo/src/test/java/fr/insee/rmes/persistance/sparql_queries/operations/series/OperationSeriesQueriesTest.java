package fr.insee.rmes.persistance.sparql_queries.operations.series;

import static fr.insee.rmes.persistance.sparql_queries.SparqlQueryNormalizer.normalize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.modules.users.domain.model.Stamp;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OperationSeriesQueriesTest {

    public static final String SERIES_BASE_URI = "operations/serie";

    private String actualRequest;
    private OperationSeriesQueries operationSeriesQueries;

    @BeforeEach
    void setUp() {
        operationSeriesQueries =
                new OperationSeriesQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());
    }

    @Test
    void getCreatorsBySeriesUri_requestIsWellGenerated() {
        var id = "s2132";
        prepareRdfUtils();
        String expectedGeneratedQuery = """
                SELECT ?creators
                FROM <http://rdf.insee.fr/graphes/operations>
                	WHERE	{
                		{
                			?series dc:creator ?creators .
                			VALUES ?series { <http://bauhaus/operations/serie/s2132> }
                		}
                		UNION
                		{
                			?series dc:creator ?creators .
                			?series dcterms:hasPart <http://bauhaus/operations/serie/s2132>
                		}
                	}""";
        assertThatCode(() -> actualRequest = operationSeriesQueries.getCreatorsBySeriesUri(
                        RdfUtils.objectIRI(ObjectType.SERIES, id).toString()))
                .doesNotThrowAnyException();
        assertThat(normalize(actualRequest)).isEqualTo(normalize(expectedGeneratedQuery));
    }

    private void prepareRdfUtils() {
        RdfUtils.setBauhausUriBuilder(new BauhausUriBuilder("", "http://bauhaus/", p -> Optional.of(SERIES_BASE_URI)));
    }

    @Test
    void seriesWithStampQuery_rendersStampValue_notRecordToString() throws Exception {
        Set<Stamp> stamps = new LinkedHashSet<>();
        stamps.add(new Stamp("HIE2000069"));

        String query = operationSeriesQueries.seriesWithStampQuery(stamps, false);

        assertThat(query).contains("\"HIE2000069\"");
        assertThat(query).doesNotContain("Stamp[");
    }
}
