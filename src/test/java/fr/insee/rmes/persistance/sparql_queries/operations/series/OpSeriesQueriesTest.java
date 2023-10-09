package fr.insee.rmes.persistance.sparql_queries.operations.series;

import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.UriUtils;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;
import org.eclipse.rdf4j.model.IRI;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static fr.insee.rmes.utils.StringUtils.urisAsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class OpSeriesQueriesTest {

    public static final String SERIES_BASE_URI="operations/serie";

    private String actualRequest;


    @Test
    void getCreatorsBySeriesUri_requestIsWellGenerated() {
        var id="s2132";
        prepareRdfUtils();
        prepareGenericQueries();
        List<IRI> uris=List.of(RdfUtils.objectIRI(ObjectType.SERIES,id));
        String expectedGeneratedQuery= """
                SELECT ?creators\s
                FROM <http://rdf.insee.fr/graphes/operations>
                	WHERE	{
                		{
                			?series dc:creator ?creators .
                			VALUES ?series { <http://bauhaus/operations/serie/s2132>}
                		}
                		UNION
                		{
                			?series dc:creator ?creators .
                			?series dcterms:hasPart <http://bauhaus/operations/serie/s2132>
                		}
                	}""";
        assertThatCode(()->actualRequest=OpSeriesQueries.getCreatorsBySeriesUri(urisAsString(uris)))
                .doesNotThrowAnyException();
        assertThat(actualRequest).isEqualToIgnoringNewLines(expectedGeneratedQuery);
    }

    private void prepareGenericQueries() {
        GenericQueries.setConfig(new ConfigStub());
    }

    private void prepareRdfUtils() {
        RdfUtils.setUriUtils(new UriUtils("","http://bauhaus/",p-> Optional.of(SERIES_BASE_URI)));
    }

}