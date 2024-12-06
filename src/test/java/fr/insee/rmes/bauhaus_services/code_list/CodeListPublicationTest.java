package fr.insee.rmes.bauhaus_services.code_list;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.exceptions.RmesException;
import org.eclipse.rdf4j.common.iteration.CloseableIteratorIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void shouldThrowExceptionIfNoStatements() throws RmesException {
        List<Statement> fakeStatements = Collections.emptyList();

        IRI resource = SimpleValueFactory.getInstance().createIRI("http://codes-list/1");
        RepositoryResult<Statement> fakeRepositoryResult =
                new RepositoryResult<>(new CloseableIteratorIteration<>(fakeStatements.iterator()));


        when(repositoryGestion.getConnection()).thenReturn(null);
        when(repositoryGestion.getStatements(any(), eq(resource))).thenReturn(new RepositoryResult<>(fakeRepositoryResult));

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
                valueFactory.createIRI("http://example.org/context")
        );

        Statement creator = valueFactory.createStatement(
                subject,
                valueFactory.createIRI("http://purl.org/dc/elements/1.1/creator"),
                valueFactory.createLiteral("Object 2"),
                valueFactory.createIRI("http://example.org/context")
        );

        Statement contributor = valueFactory.createStatement(
                subject,
                valueFactory.createIRI("http://purl.org/dc/elements/1.1/contributor"),
                valueFactory.createLiteral("Object 2"),
                valueFactory.createIRI("http://example.org/context")
        );

        Statement validationState = valueFactory.createStatement(
                subject,
                valueFactory.createIRI("http://rdf.insee.fr/def/base#validationState"),
                valueFactory.createLiteral("Object 3"),
                valueFactory.createIRI("http://example.org/context")
        );

        Statement lastCodeUriSegment = valueFactory.createStatement(
                subject,
                valueFactory.createIRI("http://rdf.insee.fr/def/base#lastCodeUriSegment"),
                valueFactory.createLiteral("Object 3"),
                valueFactory.createIRI("http://example.org/context")
        );

        List<Statement> fakeStatements = Arrays.asList(stmt1, creator, contributor, validationState, lastCodeUriSegment);
        List<Statement> codeStatement = Collections.emptyList();


        RepositoryResult<Statement> fakeRepositoryResult =
                new RepositoryResult<>(new CloseableIteratorIteration<>(fakeStatements.iterator()));


        when(publicationUtils.tranformBaseURIToPublish(subject)).thenReturn(subject);
        when(repositoryGestion.getConnection()).thenReturn(null);
        when(repositoryGestion.getStatementsPredicateObject(any(), eq(SKOS.IN_SCHEME), any())).thenReturn(new RepositoryResult<>(new CloseableIteratorIteration<>(codeStatement.iterator())));
        when(repositoryGestion.getStatements(any(), eq(resource))).thenReturn(new RepositoryResult<>(fakeRepositoryResult));

        codeListPublication.publishCodeListAndCodes(resource);

        ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);

        verify(repositoryPublication).publishResource(any(), model.capture(), eq(Constants.CODELIST));
        Assertions.assertEquals("[(http://codes-list/1, http://example.org/predicate1, \"Object 1\", http://example.org/context) [http://example.org/context]]", model.getValue().toString());
        verify(repositoryGestion).closeStatements(any());
    }

}