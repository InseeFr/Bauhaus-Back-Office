package fr.insee.rmes.bauhaus_services.classifications;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import org.eclipse.rdf4j.common.iteration.CloseableIteratorIteration;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.base.InternedIRI;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@SpringBootTest(properties = { "fr.insee.rmes.bauhaus.lg1=fr", "fr.insee.rmes.bauhaus.lg2=en"})
class ClassificationPublicationTest {

    @InjectMocks
    ClassificationPublication classificationPublication = new ClassificationPublication();

    @MockitoBean
    RepositoryGestion repoGestion;

    @MockitoBean
    RepositoryConnection repositoryConnection;

    @Test
    void shouldThrowRmesExceptionWhenPublishClassification() throws RmesException {

        when(repoGestion.getConnection()).thenReturn(repositoryConnection);
        RepositoryConnection con = repoGestion.getConnection();

        Resource graphIri = new InternedIRI("namespace", "localName");

        List<Statement> fakeStatements = Collections.emptyList();
        RepositoryResult<Statement> fakeRepositoryResult = new RepositoryResult<>(new CloseableIteratorIteration<>(fakeStatements.iterator()));
        when(repoGestion.getCompleteGraph(con, graphIri)).thenReturn(fakeRepositoryResult);

        RmesException exception = assertThrows(RmesException.class, () -> classificationPublication.publishClassification(graphIri));
        Assertions.assertEquals("{\"details\":\"namespacelocalName\",\"message\":\"1141 : Classification not found\"}", exception.getDetails());

    }

}