package fr.insee.rmes.bauhaus_services.code_list;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eclipse.rdf4j.common.iteration.CloseableIteratorIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.base.InternedIRI;
import org.eclipse.rdf4j.model.impl.GenericStatement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CodeListPublicationTest {

    @InjectMocks
    CodeListPublication codeListPublication;

    @Mock
    RepositoryGestion repositoryGestion;

    @Mock
    RepositoryPublication repositoryPublication;

    @Mock
    PublicationUtils publicationUtils;

    @Mock
    GenericStatement statement;

    @Test
    void shouldExcludeTriplet() {
        InternedIRI myIRI = new InternedIRI("myIRI", "creator");
        when(statement.getPredicate()).thenReturn(myIRI);
        String pred = RdfUtils.toString(statement.getPredicate());
        assertTrue(pred.endsWith("validationState")
                || pred.endsWith(Constants.CREATOR)
                || pred.endsWith(Constants.CONTRIBUTOR)
                || pred.endsWith("lastCodeUriSegment"));
    }

    @Test
    void shouldThrowExceptionIfNoStatements() throws RmesException {
        List<Statement> fakeStatements = Collections.emptyList();

        IRI resource = SimpleValueFactory.getInstance().createIRI("http://codes-list/1");
        RepositoryResult<Statement> fakeRepositoryResult =
                new RepositoryResult<>(new CloseableIteratorIteration<>(fakeStatements.iterator()));

        when(repositoryGestion.getConnection()).thenReturn(null);
        when(repositoryGestion.getStatements(any(), eq(resource)))
                .thenReturn(new RepositoryResult<>(fakeRepositoryResult));

        Assertions.assertThrows(RuntimeException.class, () -> codeListPublication.publishCodeListAndCodes(resource));
    }

    @Test
    void shouldNotPublishExcludedTriplets() throws RmesException {
        SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
        IRI resource = valueFactory.createIRI("http://codes-list/1");

        IRI subject = valueFactory.createIRI("http://codes-list/1");

        Statement stmt1 = valueFactory.createStatement(
                subject,
                valueFactory.createIRI("http://example.org/predicate1"),
                valueFactory.createLiteral("Object 1"),
                valueFactory.createIRI("http://example.org/context"));

        Statement creator = valueFactory.createStatement(
                subject,
                valueFactory.createIRI("http://purl.org/dc/elements/1.1/creator"),
                valueFactory.createLiteral("Object 2"),
                valueFactory.createIRI("http://example.org/context"));

        Statement contributor = valueFactory.createStatement(
                subject,
                valueFactory.createIRI("http://purl.org/dc/elements/1.1/contributor"),
                valueFactory.createLiteral("Object 2"),
                valueFactory.createIRI("http://example.org/context"));

        Statement validationState = valueFactory.createStatement(
                subject,
                valueFactory.createIRI("http://rdf.insee.fr/def/base#validationState"),
                valueFactory.createLiteral("Object 3"),
                valueFactory.createIRI("http://example.org/context"));

        Statement lastCodeUriSegment = valueFactory.createStatement(
                subject,
                valueFactory.createIRI("http://rdf.insee.fr/def/base#lastCodeUriSegment"),
                valueFactory.createLiteral("Object 3"),
                valueFactory.createIRI("http://example.org/context"));

        List<Statement> fakeStatements =
                Arrays.asList(stmt1, creator, contributor, validationState, lastCodeUriSegment);
        List<Statement> codeStatement = Collections.emptyList();

        RepositoryResult<Statement> fakeRepositoryResult =
                new RepositoryResult<>(new CloseableIteratorIteration<>(fakeStatements.iterator()));

        when(publicationUtils.tranformBaseURIToPublish(subject)).thenReturn(subject);
        when(repositoryGestion.getConnection()).thenReturn(null);
        when(repositoryGestion.getStatementsPredicateObject(any(), eq(SKOS.IN_SCHEME), any()))
                .thenReturn(new RepositoryResult<>(new CloseableIteratorIteration<>(codeStatement.iterator())));
        when(repositoryGestion.getStatements(any(), eq(resource)))
                .thenReturn(new RepositoryResult<>(fakeRepositoryResult));

        codeListPublication.publishCodeListAndCodes(resource);

        ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);

        verify(repositoryPublication).publishResource(any(), model.capture(), eq(Constants.CODELIST));
        Assertions.assertEquals(
                "[(http://codes-list/1, http://example.org/predicate1, \"Object 1\") [http://example.org/context]]",
                model.getValue().toString());
        verify(repositoryGestion).closeStatements(any());
    }

    /**
     * Publier une liste publie aussi ses codes : le type et l'appartenance au scheme sont des
     * ressources dont l'URI est réécrite, les libellés des littéraux recopiés tels quels, et les
     * attributs de gestion ne suivent pas.
     */
    @Test
    void shouldPublishTheCodesOfTheCodeListRewritingTheirLinks() throws RmesException {
        SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
        IRI codeListIri = valueFactory.createIRI("http://codes-list/1");
        IRI codeIri = valueFactory.createIRI("http://codes-list/1/A");
        IRI graph = valueFactory.createIRI("http://example.org/context");

        Statement inScheme = valueFactory.createStatement(codeIri, SKOS.IN_SCHEME, codeListIri, graph);
        Statement type = valueFactory.createStatement(codeIri, RDF.TYPE, SKOS.CONCEPT, graph);
        Statement label =
                valueFactory.createStatement(codeIri, SKOS.PREF_LABEL, valueFactory.createLiteral("A"), graph);
        Statement creator = valueFactory.createStatement(
                codeIri,
                valueFactory.createIRI("http://purl.org/dc/elements/1.1/creator"),
                valueFactory.createLiteral("DG75-F302"),
                graph);

        when(publicationUtils.tranformBaseURIToPublish(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(repositoryGestion.getConnection()).thenReturn(null);
        when(repositoryGestion.getStatements(any(), eq(codeListIri)))
                .thenReturn(new RepositoryResult<>(new CloseableIteratorIteration<>(
                        List.of(codeListStatement(codeListIri, graph)).iterator())));
        when(repositoryGestion.getStatementsPredicateObject(any(), eq(SKOS.IN_SCHEME), eq(codeListIri)))
                .thenReturn(new RepositoryResult<>(
                        new CloseableIteratorIteration<>(List.of(inScheme).iterator())));
        when(repositoryGestion.getStatements(any(), eq(codeIri)))
                .thenReturn(new RepositoryResult<>(new CloseableIteratorIteration<>(
                        List.of(inScheme, type, label, creator).iterator())));

        codeListPublication.publishCodeListAndCodes(codeListIri);

        ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);
        verify(repositoryPublication, times(2)).publishResource(any(), model.capture(), eq(Constants.CODELIST));
        Model codeModel = model.getAllValues().getFirst();
        assertTrue(codeModel.contains(codeIri, RDF.TYPE, SKOS.CONCEPT));
        assertTrue(codeModel.contains(codeIri, SKOS.IN_SCHEME, codeListIri));
        assertTrue(codeModel.contains(codeIri, SKOS.PREF_LABEL, valueFactory.createLiteral("A")));
        Assertions.assertEquals(3, codeModel.size(), "le créateur du code reste en gestion");
    }

    private static Statement codeListStatement(IRI codeListIri, IRI graph) {
        SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
        return valueFactory.createStatement(codeListIri, SKOS.NOTATION, valueFactory.createLiteral("cl1"), graph);
    }
}
