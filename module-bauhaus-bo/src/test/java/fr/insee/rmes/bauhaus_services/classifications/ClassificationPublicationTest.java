package fr.insee.rmes.bauhaus_services.classifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.Collections;
import java.util.List;
import org.eclipse.rdf4j.common.iteration.CloseableIteratorIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.base.InternedIRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ExtendWith(MockitoExtension.class)
@AppSpringBootTest
class ClassificationPublicationTest {

    @InjectMocks
    ClassificationPublication classificationPublication;

    @MockitoBean
    RepositoryGestion repoGestion;

    @MockitoBean
    RepositoryConnection repositoryConnection;

    @MockitoBean
    RepositoryPublication repositoryPublication;

    @Spy
    PublicationUtils publicationUtils = new PublicationUtils("http://bauhaus/", "http://publication/", null, null);

    @Test
    void shouldThrowRmesExceptionWhenPublishClassification() throws RmesException {

        when(repoGestion.getConnection()).thenReturn(repositoryConnection);
        RepositoryConnection con = repoGestion.getConnection();

        Resource graphIri = new InternedIRI("namespace", "localName");

        List<Statement> fakeStatements = Collections.emptyList();
        RepositoryResult<Statement> fakeRepositoryResult =
                new RepositoryResult<>(new CloseableIteratorIteration<>(fakeStatements.iterator()));
        when(repoGestion.getCompleteGraph(con, graphIri)).thenReturn(fakeRepositoryResult);

        RmesException exception =
                assertThrows(RmesException.class, () -> classificationPublication.publishClassification(graphIri));
        Assertions.assertEquals(
                "{\"code\":1141,\"details\":\"namespacelocalName\",\"message\":\"Classification not found\"}",
                exception.getDetails());
    }

    /**
     * La publication recopie le graphe de gestion tel quel, sauf les attributs qui n'ont de sens
     * qu'en gestion : l'état de validation et la version de concept.
     */
    @Test
    void shouldPublishEveryTripleExceptTheManagementOnlyOnes() throws RmesException {
        SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
        Resource graphIri = valueFactory.createIRI("http://rdf.insee.fr/graphes/codes/nafr2");
        IRI classification = valueFactory.createIRI("http://bauhaus/codes/nafr2");

        when(repoGestion.getConnection()).thenReturn(repositoryConnection);
        when(repoGestion.getCompleteGraph(repositoryConnection, graphIri))
                .thenReturn(new RepositoryResult<>(new CloseableIteratorIteration<>(List.of(
                                valueFactory.createStatement(
                                        classification, SKOS.PREF_LABEL, valueFactory.createLiteral("NAF rév. 2")),
                                valueFactory.createStatement(
                                        classification,
                                        valueFactory.createIRI("http://rdf.insee.fr/def/base#validationState"),
                                        valueFactory.createLiteral("Validated")),
                                valueFactory.createStatement(
                                        classification,
                                        valueFactory.createIRI("http://rdf.insee.fr/def/base#conceptVersion"),
                                        valueFactory.createLiteral("2")))
                        .iterator())));

        classificationPublication.publishClassification(graphIri);

        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(repositoryPublication).publishContext(eq(graphIri), modelCaptor.capture(), eq("classification"));
        Model model = modelCaptor.getValue();
        assertThat(model).hasSize(1);
        assertThat(model.contains(null, SKOS.PREF_LABEL, valueFactory.createLiteral("NAF rév. 2")))
                .isTrue();
    }

    @Test
    void shouldReportAFailingRepositoryAsAServerError() throws RmesException {
        Resource graphIri = new InternedIRI("http://graphe/", "nafr2");
        when(repoGestion.getConnection()).thenReturn(repositoryConnection);
        when(repoGestion.getCompleteGraph(repositoryConnection, graphIri))
                .thenThrow(new RepositoryException("connexion perdue"));

        RmesException exception =
                assertThrows(RmesException.class, () -> classificationPublication.publishClassification(graphIri));

        assertThat(exception.getStatus()).isEqualTo(500);
    }
}
