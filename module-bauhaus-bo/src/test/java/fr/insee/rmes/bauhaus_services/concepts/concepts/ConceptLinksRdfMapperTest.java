package fr.insee.rmes.bauhaus_services.concepts.concepts;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.model.links.Link;
import fr.insee.rmes.utils.Deserializer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ConceptLinksRdfMapperTest {

    private static final ValueFactory FACTORY = SimpleValueFactory.getInstance();
    private static final IRI CONCEPTS_GRAPH = FACTORY.createIRI("http://rdf.insee.fr/graphes/concepts/");
    private static final IRI SOURCE = FACTORY.createIRI("http://bauhaus//concepts/definition/c-source");

    private final ConceptLinksRdfMapper conceptLinksRdfMapper = new ConceptLinksRdfMapper();
    private final Model model = new LinkedHashModel();

    @BeforeAll
    static void initConfig() {
        RdfUtils.setGraphs(GraphsPropertiesStub.stub());
        RdfUtils.setBauhausUriBuilder(new BauhausUriBuilder(
                "http://bauhaus/publication/", "http://bauhaus/", p -> Optional.of("/concepts/definition")));
    }

    @Test
    void shouldMapNarrowerToSkosNarrowerAndItsInverse() throws RmesException {
        conceptLinksRdfMapper.createRdfLinks(SOURCE, links("narrower", "\"ids\":[\"c-target\"]"), model);

        assertThat(model)
                .containsExactlyInAnyOrder(
                        statement(SOURCE, SKOS.NARROWER, concept("c-target")),
                        statement(concept("c-target"), SKOS.BROADER, SOURCE));
    }

    @Test
    void shouldMapBroaderToSkosBroaderAndItsInverse() throws RmesException {
        conceptLinksRdfMapper.createRdfLinks(SOURCE, links("broader", "\"ids\":[\"c-target\"]"), model);

        assertThat(model)
                .containsExactlyInAnyOrder(
                        statement(SOURCE, SKOS.BROADER, concept("c-target")),
                        statement(concept("c-target"), SKOS.NARROWER, SOURCE));
    }

    @Test
    void shouldMapRelatedToSkosRelatedInBothDirections() throws RmesException {
        conceptLinksRdfMapper.createRdfLinks(SOURCE, links("related", "\"ids\":[\"c-target\"]"), model);

        assertThat(model)
                .containsExactlyInAnyOrder(
                        statement(SOURCE, SKOS.RELATED, concept("c-target")),
                        statement(concept("c-target"), SKOS.RELATED, SOURCE));
    }

    @Test
    void shouldMapReferencesWithoutAnyInverseStatement() throws RmesException {
        conceptLinksRdfMapper.createRdfLinks(SOURCE, links("references", "\"ids\":[\"c-target\"]"), model);

        assertThat(model).containsExactly(statement(SOURCE, DCTERMS.REFERENCES, concept("c-target")));
    }

    @Test
    void shouldMapSucceedToDctermsReplacesAndItsInverse() throws RmesException {
        conceptLinksRdfMapper.createRdfLinks(SOURCE, links("succeed", "\"ids\":[\"c-target\"]"), model);

        assertThat(model)
                .containsExactlyInAnyOrder(
                        statement(SOURCE, DCTERMS.REPLACES, concept("c-target")),
                        statement(concept("c-target"), DCTERMS.IS_REPLACED_BY, SOURCE));
    }

    @Test
    void shouldMapSucceededByToDctermsIsReplacedByAndItsInverse() throws RmesException {
        conceptLinksRdfMapper.createRdfLinks(SOURCE, links("succeededBy", "\"ids\":[\"c-target\"]"), model);

        assertThat(model)
                .containsExactlyInAnyOrder(
                        statement(SOURCE, DCTERMS.IS_REPLACED_BY, concept("c-target")),
                        statement(concept("c-target"), DCTERMS.REPLACES, SOURCE));
    }

    @Test
    void shouldBuildCloseMatchFromTheUrnAndNotFromTheIds() throws RmesException {
        conceptLinksRdfMapper.createRdfLinks(
                SOURCE, links("closeMatch", "\"ids\":[\"c-target\"],\"urn\":[\"http://external/concept/42\"]"), model);

        assertThat(model)
                .containsExactly(statement(SOURCE, SKOS.CLOSE_MATCH, FACTORY.createIRI("http://external/concept/42")));
    }

    @Test
    void shouldCreateFourStatementsWhenALinkCarriesTwoIds() throws RmesException {
        conceptLinksRdfMapper.createRdfLinks(SOURCE, links("narrower", "\"ids\":[\"c-first\",\"c-second\"]"), model);

        assertThat(model)
                .containsExactlyInAnyOrder(
                        statement(SOURCE, SKOS.NARROWER, concept("c-first")),
                        statement(concept("c-first"), SKOS.BROADER, SOURCE),
                        statement(SOURCE, SKOS.NARROWER, concept("c-second")),
                        statement(concept("c-second"), SKOS.BROADER, SOURCE));
    }

    @Test
    void shouldIgnoreAnUnknownTypeOfLink() throws RmesException {
        conceptLinksRdfMapper.createRdfLinks(SOURCE, links("unknownType", "\"ids\":[\"c-target\"]"), model);

        assertThat(model).isEmpty();
    }

    @Test
    void shouldLeaveTheModelUntouchedWhenThereIsNoLink() {
        conceptLinksRdfMapper.createRdfLinks(SOURCE, null, model);

        assertThat(model).isEmpty();
    }

    private static List<Link> links(String typeOfLink, String otherFields) throws RmesException {
        String json = "[{\"typeOfLink\":\"" + typeOfLink + "\"," + otherFields + "}]";
        return Arrays.asList(Deserializer.deserializeJsonString(json, Link[].class));
    }

    private static IRI concept(String id) {
        return FACTORY.createIRI("http://bauhaus//concepts/definition/" + id);
    }

    private static Statement statement(IRI subject, IRI predicate, IRI object) {
        return FACTORY.createStatement(subject, predicate, object, CONCEPTS_GRAPH);
    }
}
