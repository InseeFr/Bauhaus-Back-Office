package fr.insee.rmes.testcontainers.concepts;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reproduction du ticket #1495 : « La publication des liens entre concept est incorrecte ».
 *
 * Scénario : le concept A référence (dcterms:references) le concept C. A est publié AVANT C.
 * Quand C est publié à son tour, {@code clearConceptLinks(C)} effaçait le lien entrant
 * {@code A references C} sans qu'aucune étape ne le reconstruise (references est le seul
 * lien unidirectionnel, porté uniquement par le concept référençant).
 *
 * Le test ÉCHOUE tant que {@code DCTERMS.REFERENCES} n'est pas retiré de la liste des
 * prédicats effacés par {@code clearConceptLinks}.
 */
@Tag("integration")
class ConceptReferencesPublicationTest extends WithGraphDBContainer {

    private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();

    private static final IRI GRAPH = VF.createIRI("http://publication/concepts/graph-1495");
    private static final IRI CONCEPT_A = VF.createIRI("http://publication/concepts/cA1495");
    private static final IRI CONCEPT_C = VF.createIRI("http://publication/concepts/cC1495");

    private RepositoryPublication repositoryPublication;

    @BeforeEach
    void setUp() {
        RepositoryUtils repositoryUtils = new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED);
        repositoryPublication = new RepositoryPublication(
                getRdfGestionConnectionDetails().getUrlServer(),
                getRdfGestionConnectionDetails().repositoryId(),
                repositoryUtils
        );
    }

    @Test
    void referencesLinkSurvivesPublicationOfTheReferencedConcept() throws RmesException {
        // A référence C : ce triplet sortant appartient au modèle propre de A.
        Model modelA = new LinkedHashModel();
        modelA.add(CONCEPT_A, RDFS.LABEL, VF.createLiteral("Concept A"), GRAPH);
        modelA.add(CONCEPT_A, DCTERMS.REFERENCES, CONCEPT_C, GRAPH);

        // C n'a aucun triplet sortant references : son modèle propre l'ignore.
        Model modelC = new LinkedHashModel();
        modelC.add(CONCEPT_C, RDFS.LABEL, VF.createLiteral("Concept C"), GRAPH);

        // A est publié avant C.
        repositoryPublication.publishConcept(CONCEPT_A, modelA, List.of(), List.of());
        // Puis C est publié : la publication de C ne doit pas toucher au lien entrant A references C.
        repositoryPublication.publishConcept(CONCEPT_C, modelC, List.of(), List.of());

        String query = """
                SELECT ?s WHERE {
                    GRAPH <%s> {
                        ?s <%s> <%s> .
                    }
                }
                """.formatted(GRAPH.stringValue(), DCTERMS.REFERENCES.stringValue(), CONCEPT_C.stringValue());

        var result = repositoryPublication.getResponseAsArray(query);

        assertThat(result)
                .as("A dcterms:references C doit subsister après la publication de C (#1495)")
                .hasSize(1);
        assertThat(result.getJSONObject(0).getString("s")).isEqualTo(CONCEPT_A.stringValue());
    }

    @Test
    void removingTheReferenceFromTheReferencingConceptIsStillPropagated() throws RmesException {
        Model modelWithReference = new LinkedHashModel();
        modelWithReference.add(CONCEPT_A, RDFS.LABEL, VF.createLiteral("Concept A"), GRAPH);
        modelWithReference.add(CONCEPT_A, DCTERMS.REFERENCES, CONCEPT_C, GRAPH);
        repositoryPublication.publishConcept(CONCEPT_A, modelWithReference, List.of(), List.of());

        // A est republié sans la référence : son modèle propre ne contient plus le triplet sortant.
        Model modelWithoutReference = new LinkedHashModel();
        modelWithoutReference.add(CONCEPT_A, RDFS.LABEL, VF.createLiteral("Concept A"), GRAPH);
        repositoryPublication.publishConcept(CONCEPT_A, modelWithoutReference, List.of(), List.of());

        String query = """
                SELECT ?s WHERE {
                    GRAPH <%s> {
                        ?s <%s> <%s> .
                    }
                }
                """.formatted(GRAPH.stringValue(), DCTERMS.REFERENCES.stringValue(), CONCEPT_C.stringValue());

        var result = repositoryPublication.getResponseAsArray(query);

        assertThat(result)
                .as("retirer la référence à la republication du concept référençant doit bien la supprimer")
                .isEmpty();
    }
}
