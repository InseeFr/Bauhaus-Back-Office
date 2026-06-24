package fr.insee.rmes.bauhaus_services.classifications;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.modules.classifications.nomenclatures.model.Classification;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.persistance.sparql_queries.classifications.ClassificationsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassificationRepositoryTest {

    private static final String NOMENCLATURES_GRAPH = "http://rdf.insee.fr/graphes/codes/nomenclatures";

    @Mock
    RepositoryGestion repoGestion;

    @Mock
    ClassificationNoteService classificationNoteService;

    @Mock
    ClassificationsQueries classificationsQueries;

    ClassificationRepository classificationRepository;

    @BeforeEach
    void setUp() {
        RdfUtils.setGraphs(GraphsPropertiesStub.stub());
        classificationRepository = new ClassificationRepository(
                repoGestion, null, null, new BauhausLanguagesProperties("fr", "en"), null,
                classificationNoteService, classificationsQueries, GraphsPropertiesStub.stub());
    }

    @Test
    void updateClassification_writesValidationStateInTheNomenclaturesGraph() throws RmesException {
        when(repoGestion.getResponseAsArray(any())).thenReturn(new JSONArray());
        Classification classification = new Classification();
        classification.setId("cpfr21");
        classification.setPrefLabelLg1("label1");
        classification.setPrefLabelLg2("label2");
        classification.setValidationState(ValidationStatus.VALIDATED.getValue());

        classificationRepository.updateClassification(classification, "http://bauhaus/codes/cpfr21/");

        Statement validationState = capturedValidationStateStatement();
        assertThat(validationState.getObject().stringValue()).isEqualTo(ValidationStatus.MODIFIED.getValue());
        assertThat(validationState.getContext().stringValue()).isEqualTo(NOMENCLATURES_GRAPH);
    }

    private Statement capturedValidationStateStatement() throws RmesException {
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(repoGestion).loadSimpleObjectWithoutDeletion(any(), modelCaptor.capture(), any());
        return modelCaptor.getValue().stream()
                .filter(st -> st.getPredicate().equals(INSEE.VALIDATION_STATE))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No validationState statement in the persisted model"));
    }
}
