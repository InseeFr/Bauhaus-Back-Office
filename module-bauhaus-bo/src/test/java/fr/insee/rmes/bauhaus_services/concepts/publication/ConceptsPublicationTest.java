package fr.insee.rmes.bauhaus_services.concepts.publication;

import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import java.util.ArrayList;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConceptsPublicationTest {

    private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();
    private static final String BASE_URI = "http://baseUri/";

    private ConceptsPublication conceptsPublication;

    @BeforeEach
    void setUp() {
        // Same gestion and publication base URI keeps `tranformBaseURIToPublish` as identity
        // so we can focus on which triples the publication step adds to the model.
        PublicationUtils publicationUtils = new PublicationUtils(BASE_URI, BASE_URI, null, null);
        conceptsPublication = new ConceptsPublication(null, null, null, publicationUtils, null, null);
    }

    @Test
    void publishesBothReplacesAndIsReplacedByWhenStatementUsesReplaces() throws RmesException {
        IRI a = VF.createIRI(BASE_URI + "concepts/c1");
        IRI b = VF.createIRI(BASE_URI + "concepts/c2");
        Statement statement = VF.createStatement(a, DCTERMS.REPLACES, b);
        Model model = new LinkedHashModel();

        conceptsPublication.prepareOneTripleToPublicationAndCheckIfHasBroader(
                model, new ArrayList<>(), new ArrayList<>(), null, statement, false);

        assertTrue(
                model.contains(a, DCTERMS.REPLACES, b),
                "expected published model to contain <c1> dcterms:replaces <c2>");
        assertTrue(
                model.contains(b, DCTERMS.IS_REPLACED_BY, a),
                "expected published model to contain symmetric <c2> dcterms:isReplacedBy <c1>");
    }

    @Test
    void publishesBothIsReplacedByAndReplacesWhenStatementUsesIsReplacedBy() throws RmesException {
        IRI a = VF.createIRI(BASE_URI + "concepts/c1");
        IRI b = VF.createIRI(BASE_URI + "concepts/c2");
        Statement statement = VF.createStatement(a, DCTERMS.IS_REPLACED_BY, b);
        Model model = new LinkedHashModel();

        conceptsPublication.prepareOneTripleToPublicationAndCheckIfHasBroader(
                model, new ArrayList<>(), new ArrayList<>(), null, statement, false);

        assertTrue(
                model.contains(a, DCTERMS.IS_REPLACED_BY, b),
                "expected published model to contain <c1> dcterms:isReplacedBy <c2>");
        assertTrue(
                model.contains(b, DCTERMS.REPLACES, a),
                "expected published model to contain symmetric <c2> dcterms:replaces <c1> (#1450)");
    }
}
