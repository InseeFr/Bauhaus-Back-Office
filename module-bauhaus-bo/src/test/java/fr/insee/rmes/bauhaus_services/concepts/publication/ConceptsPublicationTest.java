package fr.insee.rmes.bauhaus_services.concepts.publication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
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

    @Test
    void publishesTheSymmetricRelatedLinkBetweenBothConcepts() throws RmesException {
        IRI a = VF.createIRI(BASE_URI + "concepts/c1");
        IRI b = VF.createIRI(BASE_URI + "concepts/c2");
        Statement statement = VF.createStatement(a, SKOS.RELATED, b);
        Model model = new LinkedHashModel();

        conceptsPublication.prepareOneTripleToPublicationAndCheckIfHasBroader(
                model, new ArrayList<>(), new ArrayList<>(), null, statement, false);

        assertTrue(model.contains(a, SKOS.RELATED, b));
        assertTrue(model.contains(b, SKOS.RELATED, a), "skos:related is symmetric");
    }

    /** Un concept qui a un parent n'est pas un concept racine : le drapeau remonte à l'appelant. */
    @Test
    void publishesTheNarrowerCounterpartOfABroaderLinkAndFlagsTheConceptAsNotTop() throws RmesException {
        IRI a = VF.createIRI(BASE_URI + "concepts/c1");
        IRI b = VF.createIRI(BASE_URI + "concepts/c2");
        Statement statement = VF.createStatement(a, SKOS.BROADER, b);
        Model model = new LinkedHashModel();

        Boolean hasBroader = conceptsPublication.prepareOneTripleToPublicationAndCheckIfHasBroader(
                model, new ArrayList<>(), new ArrayList<>(), null, statement, false);

        assertTrue(hasBroader);
        assertTrue(model.contains(a, SKOS.BROADER, b));
        assertTrue(model.contains(b, SKOS.NARROWER, a));
    }

    /** Le concept fils cesse d'être un concept racine du scheme : son triplet topConceptOf est à retirer. */
    @Test
    void publishesTheBroaderCounterpartOfANarrowerLinkAndScheduleTheChildForTopConceptRemoval() throws RmesException {
        IRI a = VF.createIRI(BASE_URI + "concepts/c1");
        IRI b = VF.createIRI(BASE_URI + "concepts/c2");
        Statement statement = VF.createStatement(a, SKOS.NARROWER, b);
        Model model = new LinkedHashModel();
        List<Resource> topConceptOfToDelete = new ArrayList<>();

        conceptsPublication.prepareOneTripleToPublicationAndCheckIfHasBroader(
                model, new ArrayList<>(), topConceptOfToDelete, null, statement, false);

        assertTrue(model.contains(a, SKOS.NARROWER, b));
        assertTrue(model.contains(b, SKOS.BROADER, a));
        assertEquals(List.of(b), topConceptOfToDelete);
    }

    @Test
    void publishesTheInSchemeLinkWithoutAnyCounterpart() throws RmesException {
        IRI a = VF.createIRI(BASE_URI + "concepts/c1");
        IRI scheme = VF.createIRI(BASE_URI + "concepts/definitions");
        Statement statement = VF.createStatement(a, SKOS.IN_SCHEME, scheme);
        Model model = new LinkedHashModel();

        conceptsPublication.prepareOneTripleToPublicationAndCheckIfHasBroader(
                model, new ArrayList<>(), new ArrayList<>(), null, statement, false);

        assertEquals(1, model.size());
        assertTrue(model.contains(a, SKOS.IN_SCHEME, scheme));
    }

    /** L'état de validation appartient à la base de gestion : il ne suit pas le concept en publication. */
    @Test
    void doesNotPublishTheAttributesThatBelongToTheManagementBase() throws RmesException {
        IRI a = VF.createIRI(BASE_URI + "concepts/c1");
        Statement statement = VF.createStatement(
                a, VF.createIRI("http://rdf.insee.fr/def/base#validationState"), VF.createLiteral("Validated"));
        Model model = new LinkedHashModel();

        conceptsPublication.prepareOneTripleToPublicationAndCheckIfHasBroader(
                model, new ArrayList<>(), new ArrayList<>(), null, statement, false);

        assertTrue(model.isEmpty());
    }

    @Test
    void publishesAnyOtherLiteralAsIs() throws RmesException {
        IRI a = VF.createIRI(BASE_URI + "concepts/c1");
        Statement statement = VF.createStatement(a, SKOS.PREF_LABEL, VF.createLiteral("label fr", "fr"));
        Model model = new LinkedHashModel();

        conceptsPublication.prepareOneTripleToPublicationAndCheckIfHasBroader(
                model, new ArrayList<>(), new ArrayList<>(), null, statement, false);

        assertTrue(model.contains(a, SKOS.PREF_LABEL, VF.createLiteral("label fr", "fr")));
    }
}
