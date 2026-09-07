package fr.insee.rmes.bauhaus_services.operations.documentations.documents;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.DocumentsStorageProperties;
import fr.insee.rmes.bauhaus_services.operations.OperationsParentRepository;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.model.operations.documentations.Document;
import fr.insee.rmes.modules.commons.configuration.StorageProperties;
import fr.insee.rmes.modules.commons.domain.port.serverside.FilesOperations;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationDocumentsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.IdGenerator;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DocumentsUtilsAddRubricTest {

    private static final SimpleValueFactory FACTORY = SimpleValueFactory.getInstance();

    @Mock RepositoryGestion repoGestion;
    @Mock IdGenerator idGenerator;
    @Mock RepositoryPublication repositoryPublication;
    @Mock PublicationUtils publicationUtils;
    @Mock OperationsParentRepository operationsParentRepository;
    @Mock FilesOperations filesOperations;
    @Mock StorageProperties storageProperties;
    @Mock OperationDocumentsQueries operationDocumentsQueries;
    @Mock DocumentsStorageProperties documentsStorage;

    private DocumentsUtils documentsUtils;
    private Model model;
    private Resource graph;
    private IRI textUri;

    @BeforeEach
    void setUp() {
        documentsUtils = new DocumentsUtils(repoGestion, idGenerator, repositoryPublication,
                new BauhausLanguagesProperties("fr", "en"), publicationUtils, operationsParentRepository,
                filesOperations, storageProperties, operationDocumentsQueries, documentsStorage);
        model = new LinkedHashModel();
        graph = FACTORY.createIRI("http://rdf.insee.fr/graphes/qualite/rapport/9999");
        textUri = FACTORY.createIRI("http://bauhaus/qualite/attribut/9999/S.3.1/texte");
    }

    @Test
    @DisplayName("Trois documents produisent une rdf:List ordonnée 709->710->711 attachée au textUri")
    void shouldProduceOrderedRdfListForThreeDocuments() throws RmesException {
        IRI doc709 = doc("709");
        IRI doc710 = doc("710");
        IRI doc711 = doc("711");

        documentsUtils.addDocumentsAndLinksToRubric(
                model, graph,
                List.of(asDocument(doc709), asDocument(doc710), asDocument(doc711)),
                textUri);

        Resource head = uniqueAdditionalMaterialHead();
        List<Value> orderedDocs = walkList(head);

        assertThat(orderedDocs)
                .as("L'ordre de la liste RDF doit refléter l'ordre du paramètre")
                .containsExactly(doc709, doc710, doc711);
    }

    @Test
    @DisplayName("Un seul document produit une liste singleton (first=doc, rest=nil)")
    void shouldProduceSingletonRdfListForOneDocument() throws RmesException {
        IRI doc709 = doc("709");

        documentsUtils.addDocumentsAndLinksToRubric(
                model, graph, List.of(asDocument(doc709)), textUri);

        Resource head = uniqueAdditionalMaterialHead();
        List<Value> orderedDocs = walkList(head);

        assertThat(orderedDocs).containsExactly(doc709);
        assertThat(model.filter(head, RDF.REST, null).objects())
                .containsExactly(RDF.NIL);
    }

    @Test
    @DisplayName("Une liste vide ou null n'attache aucun additionalMaterial")
    void shouldNotAttachAnythingForEmptyOrNullList() throws RmesException {
        documentsUtils.addDocumentsAndLinksToRubric(model, graph, List.of(), textUri);
        documentsUtils.addDocumentsAndLinksToRubric(model, graph, null, textUri);

        assertThat(model.filter(textUri, INSEE.ADDITIONALMATERIAL, null))
                .as("Aucun triplet additionalMaterial ne doit être créé")
                .isEmpty();
    }

    @Test
    @DisplayName("Aucune forme plate : additionalMaterial pointe uniquement la tête de liste, pas chaque doc")
    void shouldNotEmitFlatAdditionalMaterialTriples() throws RmesException {
        IRI doc709 = doc("709");
        IRI doc710 = doc("710");

        documentsUtils.addDocumentsAndLinksToRubric(
                model, graph, List.of(asDocument(doc709), asDocument(doc710)), textUri);

        Set<Value> heads = model.filter(textUri, INSEE.ADDITIONALMATERIAL, null).objects();
        assertThat(heads)
                .as("Une seule arête additionalMaterial doit relier le texte à la tête de liste")
                .hasSize(1);
        assertThat(heads.iterator().next())
                .as("La cible n'est pas un document mais une cellule de liste (BNode ou ressource intermédiaire)")
                .isNotIn(doc709, doc710);
    }

    private static Document asDocument(IRI uri) {
        Document doc = new Document();
        doc.setUri(uri.stringValue());
        doc.setUrl(uri.stringValue());
        return doc;
    }

    private static IRI doc(String id) {
        return FACTORY.createIRI("http://bauhaus/documents/document/" + id);
    }

    private Resource uniqueAdditionalMaterialHead() {
        Set<Value> objects = model.filter(textUri, INSEE.ADDITIONALMATERIAL, null).objects();
        assertThat(objects).as("Exactement une tête de liste attendue").hasSize(1);
        return (Resource) objects.iterator().next();
    }

    private List<Value> walkList(Resource head) {
        List<Value> result = new ArrayList<>();
        Resource current = head;
        while (current != null && !RDF.NIL.equals(current)) {
            Value first = onlyObject(current, RDF.FIRST);
            result.add(first);
            Value rest = onlyObject(current, RDF.REST);
            current = (rest instanceof Resource r) ? r : null;
        }
        return result;
    }

    private Value onlyObject(Resource subject, IRI predicate) {
        return Optional.of(model.filter(subject, predicate, null).objects())
                .map(s -> {
                    assertThat(s).as("Une seule valeur attendue pour " + predicate).hasSize(1);
                    return s.iterator().next();
                }).orElseThrow();
    }
}
