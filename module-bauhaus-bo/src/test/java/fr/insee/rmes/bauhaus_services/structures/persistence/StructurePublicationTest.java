package fr.insee.rmes.bauhaus_services.structures.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.QB;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.List;
import org.eclipse.rdf4j.common.iteration.CloseableIteratorIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * La publication d'une structure descend jusqu'aux spécifications de composants qu'elle porte :
 * le test vérifie ce qui est recopié, ce qui est réécrit vers la base de publication, et ce qui
 * reste en gestion.
 */
@ExtendWith(MockitoExtension.class)
class StructurePublicationTest {

    private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();
    private static final String GESTION = "http://gestion/";
    private static final String PUBLICATION = "http://publication/";

    @Mock
    RepositoryGestion repoGestion;

    @Mock
    RepositoryPublication repositoryPublication;

    private StructurePublication structurePublication;

    @BeforeEach
    void setUp() {
        structurePublication = new StructurePublication(
                repoGestion, null, repositoryPublication, new PublicationUtils(GESTION, PUBLICATION, null, null));
    }

    @Test
    void shouldPublishTheStructureAndTheSpecificationsOfItsComponents() throws RmesException {
        IRI structure = VF.createIRI(GESTION + "structures/dsd1000");
        IRI specification = VF.createIRI(GESTION + "structures/dsd1000/cs1000");
        IRI dimension = VF.createIRI(GESTION + "composants/dimension/d1000");

        when(repoGestion.getConnection()).thenReturn(null);
        when(repoGestion.getStatements(any(), eq(structure)))
                .thenReturn(statements(
                        VF.createStatement(structure, SKOS.NOTATION, VF.createLiteral("dsd1000")),
                        VF.createStatement(structure, QB.COMPONENT, specification)));
        when(repoGestion.getStatements(any(), eq(specification)))
                .thenReturn(statements(
                        VF.createStatement(specification, QB.DIMENSION, dimension),
                        VF.createStatement(specification, DCTERMS.IDENTIFIER, VF.createLiteral("cs1000"))));

        structurePublication.publish(structure);

        Model model = publishedModel();
        IRI publishedStructure = VF.createIRI(PUBLICATION + "structures/dsd1000");
        IRI publishedSpecification = VF.createIRI(PUBLICATION + "structures/dsd1000/cs1000");
        assertThat(model.contains(publishedStructure, SKOS.NOTATION, VF.createLiteral("dsd1000")))
                .isTrue();
        assertThat(model.contains(publishedStructure, QB.COMPONENT, publishedSpecification))
                .isTrue();
        assertThat(model.contains(
                        publishedSpecification, QB.DIMENSION, VF.createIRI(PUBLICATION + "composants/dimension/d1000")))
                .isTrue();
        assertThat(model.contains(publishedSpecification, DCTERMS.IDENTIFIER, VF.createLiteral("cs1000")))
                .as("l'identifiant d'une spécification reste en gestion")
                .isFalse();
        verify(repositoryPublication).clearStructureAndComponentForAllRepositories(publishedStructure);
    }

    @Test
    void shouldNotPublishTheManagementOnlyAttributesOfTheStructure() throws RmesException {
        IRI structure = VF.createIRI(GESTION + "structures/dsd1000");
        when(repoGestion.getConnection()).thenReturn(null);
        when(repoGestion.getStatements(any(), eq(structure)))
                .thenReturn(statements(
                        VF.createStatement(
                                structure,
                                VF.createIRI("http://rdf.insee.fr/def/base#validationState"),
                                VF.createLiteral("Validated")),
                        VF.createStatement(structure, DC.CREATOR, VF.createLiteral("DG75-F302")),
                        VF.createStatement(structure, DC.CONTRIBUTOR, VF.createLiteral("DG75-F302"))));

        structurePublication.publish(structure);

        assertThat(publishedModel()).isEmpty();
    }

    private static RepositoryResult<Statement> statements(Statement... statements) {
        return new RepositoryResult<>(
                new CloseableIteratorIteration<>(List.of(statements).iterator()));
    }

    private Model publishedModel() throws RmesException {
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(repositoryPublication).publishResource(any(Resource.class), modelCaptor.capture(), eq("Structure"));
        return modelCaptor.getValue();
    }
}
