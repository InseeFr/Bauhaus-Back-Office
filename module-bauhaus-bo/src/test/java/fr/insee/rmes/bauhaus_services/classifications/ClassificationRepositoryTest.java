package fr.insee.rmes.bauhaus_services.classifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.BauhausUriPropertiesStub;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.graphdb.ontologies.XKOS;
import fr.insee.rmes.modules.classifications.nomenclatures.model.Classification;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.persistance.sparql_queries.classifications.ClassificationsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        RdfUtils.setUris(BauhausUriPropertiesStub.stub());
        classificationRepository = new ClassificationRepository(
                repoGestion,
                null,
                null,
                new BauhausLanguagesProperties("fr", "en"),
                null,
                classificationNoteService,
                classificationsQueries,
                GraphsPropertiesStub.stub());
    }

    @Test
    void updateClassification_writesValidationStateInTheNomenclaturesGraph() throws RmesException {
        Classification classification = givenClassificationWithoutItems("label1", "label2");
        classification.setValidationState(ValidationStatus.VALIDATED.getValue());

        classificationRepository.updateClassification(classification, "http://bauhaus/codes/cpfr21/");

        Statement validationState = capturedValidationStateStatement();
        assertThat(validationState.getObject().stringValue()).isEqualTo(ValidationStatus.MODIFIED.getValue());
        assertThat(validationState.getContext().stringValue()).isEqualTo(NOMENCLATURES_GRAPH);
    }

    private Classification givenClassificationWithoutItems(String prefLabelLg1, String prefLabelLg2)
            throws RmesException {
        when(repoGestion.getResponseAsArray(any())).thenReturn(new JSONArray());
        Classification classification = new Classification();
        classification.setId("cpfr21");
        classification.setPrefLabelLg1(prefLabelLg1);
        classification.setPrefLabelLg2(prefLabelLg2);
        return classification;
    }

    private Statement capturedValidationStateStatement() throws RmesException {
        return capturedModel().stream()
                .filter(st -> st.getPredicate().equals(INSEE.VALIDATION_STATE))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No validationState statement in the persisted model"));
    }

    /**
     * Toutes les propriétés facultatives d'une nomenclature sont écrites dans le graphe qui lui est
     * propre ; seules celles qui sont renseignées produisent un triplet.
     */
    @Test
    void updateClassification_writesEveryOptionalPropertyItCarries() throws RmesException {
        Classification classification = givenClassificationWithoutItems("label fr", "label en");
        classification.setAltLabelLg1("alt fr");
        classification.setAltLabelLg2("alt en");
        classification.setDescriptionLg1("description fr");
        classification.setDescriptionLg2("description en");
        classification.setIdSeries("nafr2");
        classification.setCreator("http://creator");
        classification.setContributor("http://contributor");
        classification.setAdditionalMaterial("http://complement");
        classification.setLegalMaterial("http://texte-legal");
        classification.setHomepage("http://page");
        classification.setDisseminationStatus("http://statut");

        classificationRepository.updateClassification(classification, "http://bauhaus/codes/cpfr21/");

        Model model = capturedModel();
        assertThat(objectsOf(model, SKOS.ALT_LABEL)).containsExactlyInAnyOrder("alt fr", "alt en");
        assertThat(objectsOf(model, DC.DESCRIPTION)).containsExactlyInAnyOrder("description fr", "description en");
        assertThat(objectsOf(model, XKOS.BELONGS_TO)).allMatch(object -> object.endsWith("nafr2"));
        assertThat(objectsOf(model, DC.CREATOR)).containsExactly("http://creator");
        assertThat(objectsOf(model, DC.CONTRIBUTOR)).containsExactly("http://contributor");
        assertThat(objectsOf(model, INSEE.ADDITIONALMATERIAL)).containsExactly("http://complement");
        assertThat(objectsOf(model, INSEE.LEGALMATERIAL)).containsExactly("http://texte-legal");
        assertThat(objectsOf(model, FOAF.HOMEPAGE)).containsExactly("http://page");
        assertThat(objectsOf(model, INSEE.DISSEMINATIONSTATUS)).containsExactly("http://statut");
    }

    @Test
    void updateClassification_writesOnlyTheMandatoryLabelsWhenNothingElseIsFilled() throws RmesException {
        Classification classification = givenClassificationWithoutItems("label fr", "label en");

        classificationRepository.updateClassification(classification, "http://bauhaus/codes/cpfr21/");

        Model model = capturedModel();
        assertThat(objectsOf(model, SKOS.PREF_LABEL)).containsExactlyInAnyOrder("label fr", "label en");
        assertThat(objectsOf(model, SKOS.ALT_LABEL)).isEmpty();
        assertThat(objectsOf(model, DC.DESCRIPTION)).isEmpty();
        assertThat(objectsOf(model, XKOS.BELONGS_TO)).isEmpty();
        assertThat(objectsOf(model, INSEE.VALIDATION_STATE)).containsExactly(ValidationStatus.UNPUBLISHED.getValue());
    }

    private Model capturedModel() throws RmesException {
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(repoGestion).loadSimpleObjectWithoutDeletion(any(), modelCaptor.capture(), any());
        return modelCaptor.getValue();
    }

    private static java.util.List<String> objectsOf(Model model, org.eclipse.rdf4j.model.IRI predicate) {
        return model.stream()
                .filter(statement -> statement.getPredicate().equals(predicate))
                .map(statement -> statement.getObject().stringValue())
                .toList();
    }
}
