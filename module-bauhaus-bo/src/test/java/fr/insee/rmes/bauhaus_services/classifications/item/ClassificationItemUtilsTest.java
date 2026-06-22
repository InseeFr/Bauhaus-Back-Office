package fr.insee.rmes.bauhaus_services.classifications.item;
import fr.insee.rmes.bauhaus_services.classifications.ClassificationNoteService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.classifications.nomenclatures.model.ClassificationItem;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.persistance.sparql_queries.classifications.ClassificationItemsQueries;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassificationItemUtilsTest {


    @Mock
    RepositoryGestion repositoryGestion;

    @InjectMocks
    ClassificationItemRepository classificationItemUtils;

    @Spy
    BauhausLanguagesProperties languages = new BauhausLanguagesProperties("fr", "en");

    @Mock
    ClassificationNoteService classificationNoteService;

    @Mock
    ClassificationItemsQueries classificationItemsQueries;

    @Test
    void shouldThrowExceptionIfPrefLabelLg1Null() {
        ClassificationItem item = new ClassificationItem();
        item.setId("1");
        item.setPrefLabelLg2("label2");
        item.setDefinitionLg1("<p>Definition Lg1</p>");
        item.setDefinitionLg1Uri("http://definition-lg1");

        RmesException exception = assertThrows(RmesBadRequestException.class, () -> classificationItemUtils.updateClassificationItem(item, "http://uri", "1"));
        assertThat(exception.getDetails()).contains("The property prefLabelLg1 is required");

    }

    @Test
    void shouldThrowExceptionIfPrefLabelLg2Null() {

        ClassificationItem item = new ClassificationItem();
        item.setId("1");
        item.setPrefLabelLg1("label1");
        item.setDefinitionLg1("<p>Definition Lg1</p>");
        item.setDefinitionLg1Uri("http://definition-lg1");

        RmesException exception = assertThrows(RmesBadRequestException.class, () -> classificationItemUtils.updateClassificationItem(item, "http://uri", "1"));
        assertThat(exception.getDetails()).contains("The property prefLabelLg2 is required");

    }

    @Test
    void shouldAddNotes() throws RmesException {

        ClassificationItem item = new ClassificationItem();
        item.setId("1");
        item.setPrefLabelLg1("label1");
        item.setPrefLabelLg2("label2");
        item.setDefinitionLg1("Definition Lg1");
        item.setDefinitionLg1Uri("http://definition-lg1");

        classificationItemUtils.updateClassificationItem(item, "http://uri", "1");
        verify(classificationNoteService).addNotes(any(), eq("http://definition-lg1"), eq("Definition Lg1"), any());
    }

    @Test
    void shouldMarkItemAsModifiedWhenUpdatingAlreadyPublishedItem() throws RmesException {
        ClassificationItem item = new ClassificationItem();
        item.setId("1");
        item.setPrefLabelLg1("label1");
        item.setPrefLabelLg2("label2");
        when(repositoryGestion.getResponseAsObject(any()))
                .thenReturn(new JSONObject().put("validationState", ValidationStatus.VALIDATED.getValue()));

        classificationItemUtils.updateClassificationItem(item, "http://uri", "1");

        assertThat(validationStateOfCapturedModel()).isEqualTo(ValidationStatus.MODIFIED.getValue());
    }

    @Test
    void shouldMarkItemAsUnpublishedWhenUpdatingNeverPublishedItem() throws RmesException {
        ClassificationItem item = new ClassificationItem();
        item.setId("1");
        item.setPrefLabelLg1("label1");
        item.setPrefLabelLg2("label2");
        when(repositoryGestion.getResponseAsObject(any()))
                .thenReturn(new JSONObject().put("validationState", ValidationStatus.UNPUBLISHED.getValue()));

        classificationItemUtils.updateClassificationItem(item, "http://uri", "1");

        assertThat(validationStateOfCapturedModel()).isEqualTo(ValidationStatus.UNPUBLISHED.getValue());
    }

    private String validationStateOfCapturedModel() throws RmesException {
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(repositoryGestion).loadSimpleObjectWithoutDeletion(any(), modelCaptor.capture(), any());
        for (Statement st : modelCaptor.getValue()) {
            if (st.getPredicate().equals(INSEE.VALIDATION_STATE)) {
                return st.getObject().stringValue();
            }
        }
        return null;
    }
}