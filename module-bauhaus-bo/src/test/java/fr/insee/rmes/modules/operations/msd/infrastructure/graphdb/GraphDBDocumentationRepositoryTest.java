package fr.insee.rmes.modules.operations.msd.infrastructure.graphdb;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.modules.commons.domain.GenericInternalServerException;
import fr.insee.rmes.modules.operations.msd.domain.NotFoundAttributeException;
import fr.insee.rmes.modules.operations.msd.domain.OperationDocumentationRubricWithoutRangeException;
import fr.insee.rmes.modules.operations.msd.domain.model.DocumentationAttribute;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class GraphDBDocumentationRepositoryTest extends WithGraphDBContainer {

    RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));
    DocumentationQueries documentationQueries =
            new DocumentationQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());

    GraphDBDocumentationRepository repository =
            new GraphDBDocumentationRepository(repositoryGestion, documentationQueries);

    @BeforeAll
    static void initData() {
        container.withTrigFiles("sims-metadata.trig");
    }

    @Test
    void should_return_rubrics_sans_object() throws Exception {
        List<DocumentationAttribute> result = repository.getAttributesSpecification();
        Assertions.assertEquals(96, result.size());

        for (DocumentationAttribute obj : result) {
            if ("S.4".equalsIgnoreCase(obj.id())) {
                Assertions.assertTrue(obj.sansObject());
            }

            if ("S.6".equalsIgnoreCase(obj.id())) {
                Assertions.assertFalse(obj.sansObject());
            }
        }
    }

    @Test
    void should_return_rubric_with_true_sans_object_property()
            throws NotFoundAttributeException, GenericInternalServerException,
                    OperationDocumentationRubricWithoutRangeException {
        DocumentationAttribute result = repository.getAttributeSpecification("S.4");
        Assertions.assertTrue(result.sansObject());
    }

    @Test
    void should_return_rubric_with_false_sans_object_property()
            throws NotFoundAttributeException, GenericInternalServerException,
                    OperationDocumentationRubricWithoutRangeException {
        DocumentationAttribute result = repository.getAttributeSpecification("S.6");
        Assertions.assertFalse(result.sansObject());
    }

    @Test
    void should_expose_subPropertyOf_for_attribute_flagged_with_dcterms_modified() throws Exception {
        DocumentationAttribute result = repository.getAttributeSpecification("S.2.3");
        Assertions.assertEquals("http://purl.org/dc/terms/modified", result.subPropertyOf());
    }

    @Test
    void should_return_null_subPropertyOf_for_unflagged_attribute() throws Exception {
        DocumentationAttribute result = repository.getAttributeSpecification("S.6");
        Assertions.assertNull(result.subPropertyOf());
    }
}
