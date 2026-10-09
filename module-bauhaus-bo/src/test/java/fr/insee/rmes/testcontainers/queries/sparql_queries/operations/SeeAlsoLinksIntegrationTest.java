package fr.insee.rmes.testcontainers.queries.sparql_queries.operations;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.config.BauhausUriPropertiesStub;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationIndicatorsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Un lien « Séries ou Indicateurs liés » (rdfs:seeAlso) se lit depuis ses deux extrémités, et
 * réenregistrer un objet retire les liens qui pointaient vers lui (issue Bauhaus#1657).
 */
@Tag("integration")
class SeeAlsoLinksIntegrationTest extends WithGraphDBContainer {

    private static final ValueFactory VF = SimpleValueFactory.getInstance();
    private static final IRI OPERATIONS_GRAPH = VF.createIRI("http://rdf.insee.fr/graphes/operations");

    RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));
    OperationSeriesQueries seriesQueries =
            new OperationSeriesQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());
    OperationIndicatorsQueries indicatorsQueries = new OperationIndicatorsQueries(
            BauhausUriPropertiesStub.stub(), new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());

    @BeforeAll
    static void initData() {
        container.withTrigFiles("see-also-links.trig");
    }

    @Test
    void a_series_lists_the_series_and_indicators_that_point_to_it() throws Exception {
        JSONArray links = repositoryGestion.getResponseAsArray(
                seriesQueries.seriesLinks("s9001", RDFS.SEEALSO, Constants.OPERATIONS));

        assertThat(ids(links)).containsExactlyInAnyOrder("s9002", "p9001");
    }

    @Test
    void an_indicator_lists_the_series_that_point_to_it() throws Exception {
        JSONArray links = repositoryGestion.getResponseAsArray(indicatorsQueries.indicatorLinks("p9002", RDFS.SEEALSO));

        assertThat(ids(links)).containsExactly("s9002");
    }

    @Test
    void saving_an_object_drops_the_see_also_links_pointing_to_it_that_it_no_longer_lists() throws Exception {
        IRI series = VF.createIRI("http://bauhaus/operations/serie/s9003");
        Model model = new LinkedHashModel();
        model.add(series, SKOS.PREF_LABEL, VF.createLiteral("Série réenregistrée", "fr"), OPERATIONS_GRAPH);

        repositoryGestion.loadObjectWithReplaceLinks(series, model);

        JSONArray links = repositoryGestion.getResponseAsArray(
                seriesQueries.seriesLinks("s9003", RDFS.SEEALSO, Constants.OPERATIONS));
        assertThat(ids(links)).isEmpty();
    }

    private static List<String> ids(JSONArray links) {
        return JSONUtils.stream(links).map(link -> link.getString("id")).toList();
    }
}
