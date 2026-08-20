package fr.insee.rmes.persistance.sparql_queries;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.GraphsProperties;
import fr.insee.rmes.config.BauhausUriPropertiesStub;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.structures.infrastructure.graphdb.StructureQueries;
import fr.insee.rmes.persistance.sparql_queries.datasets.DatasetDistributionQueries;
import fr.insee.rmes.persistance.sparql_queries.datasets.DatasetQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationDocumentsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationFamilyQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationIndicatorsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationsOperationQueries;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Une valeur fournie par l'utilisateur — libellé saisi, identifiant d'URL, IRI d'un objet — ne doit
 * jamais pouvoir sortir du jeton SPARQL dans lequel la requête l'injecte.
 */
class QueryValueEscapingTest {

    /**
     * Ferme le littéral — les deux graphies de quote sont utilisées dans les templates —, referme le
     * motif, ajoute une clause destructrice et commente la fin de la requête d'origine.
     */
    private static final String LITERAL_PAYLOAD = "x\"' . } DELETE { ?s ?p ?o } # ";

    /**
     * Même charge, adaptée au jeton {@code <…>} d'une IRI.
     */
    private static final String IRI_PAYLOAD = "http://bauhaus/x> . } DELETE { ?s ?p ?o } # ";

    private static final String HARMLESS_IRI = "http://bauhaus/operations/serie/s1234";

    private static final BauhausLanguagesProperties LANGUAGES = new BauhausLanguagesProperties("fr", "en");
    private static final GraphsProperties GRAPHS = GraphsPropertiesStub.stub();

    private static final OperationsOperationQueries OPERATIONS = new OperationsOperationQueries(LANGUAGES, GRAPHS);
    private static final OperationFamilyQueries FAMILIES = new OperationFamilyQueries(GRAPHS);
    private static final OperationSeriesQueries SERIES = new OperationSeriesQueries(LANGUAGES, GRAPHS);
    private static final OperationIndicatorsQueries INDICATORS =
            new OperationIndicatorsQueries(BauhausUriPropertiesStub.stub(), LANGUAGES, GRAPHS);
    private static final OperationDocumentsQueries DOCUMENTS =
            new OperationDocumentsQueries(BauhausUriPropertiesStub.stub(), LANGUAGES, GRAPHS);
    private static final StructureQueries STRUCTURES = new StructureQueries(LANGUAGES, GRAPHS);
    private static final DatasetQueries DATASETS = new DatasetQueries(LANGUAGES);
    private static final DatasetDistributionQueries DISTRIBUTIONS = new DatasetDistributionQueries(LANGUAGES);

    @FunctionalInterface
    private interface UnicityQuery {
        String build(String id, String label, String lang) throws RmesException;
    }

    @FunctionalInterface
    private interface UriQuery {
        String build(String uri) throws RmesException;
    }

    static Stream<Arguments> unicityQueries() {
        return Stream.of(
                Arguments.of(Named.<UnicityQuery>of("operation", OPERATIONS::checkPrefLabelUnicity)),
                Arguments.of(Named.<UnicityQuery>of("family", FAMILIES::checkPrefLabelUnicity)),
                Arguments.of(Named.<UnicityQuery>of("series", SERIES::checkPrefLabelUnicity)),
                Arguments.of(Named.<UnicityQuery>of("indicator", INDICATORS::checkPrefLabelUnicity)),
                Arguments.of(Named.<UnicityQuery>of("document", DOCUMENTS::checkLabelUnicity))
        );
    }

    static Stream<Arguments> uriQueries() {
        return Stream.of(
                Arguments.of(Named.<UriQuery>of("structure contributors", STRUCTURES::getContributorsByStructureUri)),
                Arguments.of(Named.<UriQuery>of("component contributors", STRUCTURES::getContributorsByComponentUri)),
                Arguments.of(Named.<UriQuery>of("dataset contributors", DATASETS::getContributorsByDatasetUri)),
                Arguments.of(Named.<UriQuery>of("distribution contributors", DISTRIBUTIONS::getContributorsByDistributionUri)),
                Arguments.of(Named.<UriQuery>of("series creators", SERIES::getCreatorsBySeriesUri))
        );
    }

    @ParameterizedTest
    @MethodSource("unicityQueries")
    void shouldKeepTheQueryParsableWhenTheLabelCarriesAnInjectionPayload(UnicityQuery query) throws RmesException {
        String rendered = query.build("1234", LITERAL_PAYLOAD, "fr");

        assertParsable(rendered);
    }

    @ParameterizedTest
    @MethodSource("unicityQueries")
    void shouldKeepTheQueryParsableWhenTheIdentifierCarriesAnInjectionPayload(UnicityQuery query) throws RmesException {
        String rendered = query.build(LITERAL_PAYLOAD, "Libellé", "fr");

        assertParsable(rendered);
    }

    @ParameterizedTest
    @MethodSource("unicityQueries")
    void shouldRejectALanguageThatIsNotALangtag(UnicityQuery query) {
        assertThrows(IllegalArgumentException.class,
                () -> query.build("1234", "Libellé", "fr\" . } DELETE { ?s ?p ?o } #"));
    }

    @ParameterizedTest
    @MethodSource("uriQueries")
    void shouldWrapTheInjectedUriInAnIriRef(UriQuery query) throws RmesException {
        String rendered = query.build(HARMLESS_IRI);

        assertTrue(rendered.contains("<" + HARMLESS_IRI + ">"),
                () -> "The IRI must be injected as an IRIREF, got: " + rendered);
        assertParsable(rendered);
    }

    @ParameterizedTest
    @MethodSource("uriQueries")
    void shouldRejectAUriThatWouldCloseTheIriRef(UriQuery query) {
        assertThrows(IllegalArgumentException.class, () -> query.build(IRI_PAYLOAD));
    }

    private static void assertParsable(String query) {
        assertDoesNotThrow(() -> QueryParserUtil.parseQuery(QueryLanguage.SPARQL, query, null),
                () -> "Not a parsable SPARQL query: " + query);
    }
}
