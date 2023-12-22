package fr.insee.rmes.bauhaus_services.code_list;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.model.ValidationStatus;
import fr.insee.rmes.persistance.ontologies.INSEE;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MockRepositoryResult implements CloseableIteration<Statement, RepositoryException> {

    private final Iterator<Statement> iterator;

    public MockRepositoryResult() {
        this.iterator = Collections.emptyIterator();
    }
    public MockRepositoryResult(List<Statement> list) {
        this.iterator = list.iterator();
    }

    @Override
    public void close() {

    }

    @Override
    public boolean hasNext() {
        return this.iterator.hasNext();
    }

    @Override
    public Statement next() {
        return this.iterator.next();
    }

    @Override
    public void remove() {

    }

    @Override
    public Stream<Statement> stream() {
        return CloseableIteration.super.stream();
    }
}
@ExtendWith(MockitoExtension.class)
class CodeListPublicationTest {

    @Mock
    RepositoryGestion repositoryGestion;

    @Mock
    RepositoryPublication repositoryPublication;

    @Mock
    Config config;

    @InjectMocks
    CodeListPublication codeListPublication;


    @Test
    void shouldThrowAnExceptionIfTheCodeListRepositoryResultIsEmpty() throws RmesException {
        IRI codeListOrCode = RdfUtils.createIRI("http://code-list");
        RepositoryResult<Statement> statements = new RepositoryResult<>(new MockRepositoryResult());
        when(repositoryGestion.getStatements(any(), any())).thenReturn(statements);
        assertThrows(RmesNotFoundException.class, () -> codeListPublication.publishCodeListAndCodes(codeListOrCode,  false));
    }
    @Test
    void shouldCallPublishResourceForCodeList() throws RmesException {
        when(config.getBaseUriGestion()).thenReturn("http://base-uri-gestion");
        PublicationUtils.setConfig(config);

        IRI codeListOrCode = RdfUtils.createIRI("http://code-list");

        List<Statement> list = new ArrayList<>();

        list.add(createStatement("http://subject", "http://predicate", "value"));

        RepositoryResult<Statement> statements = new RepositoryResult<>(new MockRepositoryResult(list));
        when(repositoryGestion.getStatements(any(), any())).thenReturn(statements);
        when(repositoryGestion.getStatementsPredicateObject(any(), any(), any())).thenReturn(new RepositoryResult<>(new MockRepositoryResult()));
        codeListPublication.publishCodeListAndCodes(codeListOrCode,  false);

        ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);
        verify(repositoryPublication, times(1)).publishResource(eq(codeListOrCode), model.capture(), eq(Constants.CODELIST));

        model.getValue().subjects().forEach(subject -> Assertions.assertEquals("http://subject", subject.stringValue()));
        model.getValue().predicates().forEach(predicate -> Assertions.assertEquals("http://predicate", predicate.stringValue()));
        model.getValue().objects().forEach(object -> Assertions.assertEquals("value", object.stringValue()));
    }

    @Test
    void shouldThrowExceptionIfSeeAlsoEmptyList() throws RmesException {
        when(config.getBaseUriGestion()).thenReturn("http://base-uri-gestion");
        PublicationUtils.setConfig(config);

        IRI codeListOrCode = RdfUtils.createIRI("http://code-list");

        List<Statement> list = new ArrayList<>();
        list.add(createStatement("http://subject", "http://predicate", "value"));
        list.add(createStatement("http://subject", RDFS.SEEALSO.toString(), "http://see-also"));

        RepositoryResult<Statement> statements = new RepositoryResult<>(new MockRepositoryResult(list));
        RepositoryResult<Statement> statementsSeeAlso = new RepositoryResult<>(new MockRepositoryResult());

        when(repositoryGestion.getStatements(any(), any())).thenReturn(statements, statementsSeeAlso);
        assertThrows(RmesNotFoundException.class, () -> codeListPublication.publishCodeListAndCodes(codeListOrCode,  false));

    }

    @Test
    void shouldCallPublishResourceForSeeAlso() throws RmesException {
        when(config.getBaseUriGestion()).thenReturn("http://base-uri-gestion");
        PublicationUtils.setConfig(config);

        IRI codeListOrCode = RdfUtils.createIRI("http://code-list");
        IRI seeAlso = RdfUtils.createIRI("http://see-also");

        List<Statement> list = new ArrayList<>();
        list.add(createStatement("http://subject", "http://predicate", "value"));
        list.add(createStatement("http://subject", RDFS.SEEALSO.toString(), "http://see-also"));
        list.add(createStatement("http://subject", INSEE.VALIDATION_STATE.toString(), ValidationStatus.VALIDATED.toString()));

        RepositoryResult<Statement> statements = new RepositoryResult<>(new MockRepositoryResult(list));

        List<Statement> seeAlsoList = new ArrayList<>();
        seeAlsoList.add(createStatement("http://subject2", "http://predicate2", "value"));
        seeAlsoList.add(createStatement("http://subject2", INSEE.VALIDATION_STATE.toString(), ValidationStatus.VALIDATED.toString()));

        RepositoryResult<Statement> statementsSeeAlso = new RepositoryResult<>(new MockRepositoryResult(seeAlsoList));

        when(repositoryGestion.getStatements(any(), any())).thenReturn(statements, statementsSeeAlso);
        when(repositoryGestion.getStatementsPredicateObject(any(), any(), any())).thenReturn(new RepositoryResult<>(new MockRepositoryResult()));
        codeListPublication.publishCodeListAndCodes(codeListOrCode,  false);

        ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);
        verify(repositoryPublication, times(1)).publishResource(eq(codeListOrCode), model.capture(), eq(Constants.CODELIST));
        ArgumentCaptor<Model> model2 = ArgumentCaptor.forClass(Model.class);
        verify(repositoryPublication, times(1)).publishResource(eq(seeAlso), model2.capture(), eq(Constants.CODELIST));

        Model value = model.getValue();
        Assertions.assertEquals(2, value.predicates().toArray().length);
        value.forEach(statement -> {
           if(statement.getPredicate().toString().equalsIgnoreCase("http://predicate")){
               Assertions.assertEquals("http://subject", statement.getSubject().stringValue());
               Assertions.assertEquals("value", statement.getObject().stringValue());

           }
            if(statement.getPredicate().toString().equalsIgnoreCase(RDFS.SEEALSO.toString())){
                Assertions.assertEquals("http://subject", statement.getSubject().stringValue());
                Assertions.assertEquals("http://see-also", statement.getObject().stringValue());
            }
        });
        Model value2 = model2.getValue();
        Assertions.assertEquals(1, value2.predicates().toArray().length);
        value2.forEach(statement -> {
            if(statement.getPredicate().toString().equalsIgnoreCase("http://predicate2")){
                Assertions.assertEquals("http://subject2", statement.getSubject().stringValue());
                Assertions.assertEquals("value", statement.getObject().stringValue());

            }
        });
    }

    @Test
    void shouldCallPublishResourceForCodes() throws RmesException {
        when(config.getBaseUriGestion()).thenReturn("http://base-uri-gestion");
        PublicationUtils.setConfig(config);

        IRI codeListOrCode = RdfUtils.createIRI("http://code-list");

        List<Statement> list = new ArrayList<>();
        list.add(createStatement("http://subject", "http://predicate", "value"));

        RepositoryResult<Statement> statements = new RepositoryResult<>(new MockRepositoryResult(list));


        List<Statement> codesList = new ArrayList<>();
        codesList.add(createStatement("http://subject", SKOS.IN_SCHEME.toString(), "http://code"));
        RepositoryResult<Statement> statementsCodes = new RepositoryResult<>(new MockRepositoryResult(codesList));


        List<Statement> codes = new ArrayList<>();
        codes.add(createStatement("http://code", "http://predicate", "value"));
        codes.add(createStatement("http://code", INSEE.VALIDATION_STATE.toString(), ValidationStatus.VALIDATED.toString()));
        RepositoryResult<Statement> codesStatements = new RepositoryResult<>(new MockRepositoryResult(codes));

        when(repositoryGestion.getStatements(any(), any())).thenReturn(statements, codesStatements);
        when(repositoryGestion.getStatementsPredicateObject(any(), any(), any())).thenReturn(new RepositoryResult<>(statementsCodes));
        codeListPublication.publishCodeListAndCodes(codeListOrCode,  false);

        ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);
        verify(repositoryPublication, times(1)).publishResource(eq(RdfUtils.createIRI("http://subject")), model.capture(), eq(Constants.CODELIST));

        Model value = model.getValue();
        Assertions.assertEquals(1, value.predicates().toArray().length);
        value.forEach(statement -> {
            if(statement.getPredicate().toString().equalsIgnoreCase("http://predicate")){
                Assertions.assertEquals("http://code", statement.getSubject().stringValue());
                Assertions.assertEquals("value", statement.getObject().stringValue());

            }
        });

    }

    private static Statement createStatement(String subject, String predicate, String object) {
        return new Statement() {
            @Override
            public Resource getSubject() {
                return RdfUtils.toURI(subject);
            }

            @Override
            public IRI getPredicate() {
                return RdfUtils.toURI(predicate);
            }

            @Override
            public Value getObject() {
                return () -> object;
            }

            @Override
            public Resource getContext() {
                return RdfUtils.toURI("http://context");
            }
        };
    }

}