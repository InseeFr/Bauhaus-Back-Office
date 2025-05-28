package fr.insee.rmes.webservice.classifications;

import fr.insee.rmes.bauhaus_services.ClassificationsService;
import fr.insee.rmes.bauhaus_services.classifications.item.ClassificationItemService;
import fr.insee.rmes.config.swagger.model.Id;
import fr.insee.rmes.exceptions.RmesException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(properties = { "fr.insee.rmes.bauhaus.lg1=fr", "fr.insee.rmes.bauhaus.lg2=en"})
class ClassificationsResourcesTest {

    @MockitoBean
    ClassificationsService classificationsService;

    @MockitoBean
    ClassificationItemService classificationItemService;

    final Id id = new Id("mocked Id");

    @Test
    void shouldReturnResponseWhenGetFamilies() throws RmesException {
        ClassificationsResources classificationsResources = new ClassificationsResources(classificationsService,classificationItemService);
        when(classificationsService.getFamily(id.identifier())).thenReturn("mocked result");
        String actual = classificationsResources.getFamily(id.identifier()).toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetFamilyMembers() throws RmesException {
        ClassificationsResources classificationsResources = new ClassificationsResources(classificationsService,classificationItemService);
        when(classificationsService.getFamilyMembers(id.identifier())).thenReturn("mocked result");
        String actual = classificationsResources.getFamilyMembers(id.identifier()).toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetOneSeries() throws RmesException {
        ClassificationsResources classificationsResources = new ClassificationsResources(classificationsService,classificationItemService);
        when(classificationsService.getOneSeries(id.identifier())).thenReturn("mocked result");
        String actual = classificationsResources.getOneSeries(id.identifier()).toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetSeriesMembers() throws RmesException {
        ClassificationsResources classificationsResources = new ClassificationsResources(classificationsService,classificationItemService);
        when(classificationsService.getSeriesMembers(id.identifier())).thenReturn("mocked result");
        String actual = classificationsResources.getSeriesMembers(id.identifier()).toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetClassification() throws RmesException {
        ClassificationsResources classificationsResources = new ClassificationsResources(classificationsService,classificationItemService);
        when(classificationsService.getClassification(id.identifier())).thenReturn("mocked result");
        String actual = classificationsResources.getClassification(id.identifier()).toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenUpdateClassification() throws RmesException {
        doNothing().when(classificationsService).updateClassification(id.identifier(), " mocked body");
        ClassificationsResources classificationsResources = new ClassificationsResources(classificationsService,classificationItemService);
        String actual = classificationsResources.updateClassification(id, " mocked body").toString();
        Assertions.assertEquals("<200 OK OK,Id[identifier=mocked Id],[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenPublishClassification() throws RmesException {
        doNothing().when(classificationsService).setClassificationValidation(id.identifier());
        ClassificationsResources classificationsResources = new ClassificationsResources(classificationsService,classificationItemService);
        String actual = classificationsResources.publishClassification(id).toString();
        Assertions.assertEquals("<200 OK OK,Id[identifier=mocked Id],[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetClassificationItems() throws RmesException {
        when(classificationItemService.getClassificationItems(id.identifier())).thenReturn("mocked result");
        ClassificationsResources classificationsResources = new ClassificationsResources(classificationsService,classificationItemService);
        String actual = classificationsResources.getClassificationItems(id.identifier()).toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetClassificationLevels() throws RmesException {
        when(classificationsService.getClassificationLevels(id.identifier())).thenReturn("mocked result");
        ClassificationsResources classificationsResources = new ClassificationsResources(classificationsService,classificationItemService);
        String actual = classificationsResources.getClassificationLevels(id.identifier()).toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetClassificationLevel() throws RmesException {
        when(classificationsService.getClassificationLevel(id.identifier(),"mocked Level")).thenReturn("mocked result");
        ClassificationsResources classificationsResources = new ClassificationsResources(classificationsService,classificationItemService);
        String actual = classificationsResources.getClassificationLevel(id.identifier(),"mocked Level").toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetClassificationLevelMembers() throws RmesException {
        when(classificationsService.getClassificationLevelMembers(id.identifier(),"mocked Level")).thenReturn("mocked result");
        ClassificationsResources classificationsResources = new ClassificationsResources(classificationsService,classificationItemService);
        String actual = classificationsResources.getClassificationLevelMembers(id.identifier(),"mocked Level").toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetClassificationItem() throws RmesException {
        when(classificationItemService.getClassificationItem(id.identifier(),"mocked item")).thenReturn("mocked result");
        ClassificationsResources classificationsResources = new ClassificationsResources(classificationsService,classificationItemService);
        String actual = classificationsResources.getClassificationItem(id.identifier(),"mocked item").toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenUpdateClassificationItem() throws RmesException {
        doNothing().when(classificationItemService).updateClassificationItem(id.identifier(),"mocked item","body");
        ClassificationsResources classificationsResources = new ClassificationsResources(classificationsService,classificationItemService);
        String actual = classificationsResources.updateClassificationItem(id.identifier(),"mocked item","body").toString();
        Assertions.assertEquals("<200 OK OK,mocked item,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetClassificationItemNarrowers() throws RmesException {
        when(classificationItemService.getClassificationItemNarrowers(id.identifier(),"mocked item")).thenReturn("mocked result");
        ClassificationsResources classificationsResources = new ClassificationsResources(classificationsService,classificationItemService);
        String actual = classificationsResources.getClassificationItemNarrowers(id.identifier(),"mocked item").toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetCorrespondences() throws RmesException {
        when(classificationsService.getCorrespondences()).thenReturn("mocked result");
        ClassificationsResources classificationsResources = new ClassificationsResources(classificationsService,classificationItemService);
        String actual = classificationsResources.getCorrespondences().toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetCorrespondence() throws RmesException {
        when(classificationsService.getCorrespondence(id.identifier())).thenReturn("mocked result");
        ClassificationsResources classificationsResources = new ClassificationsResources(classificationsService,classificationItemService);
        String actual = classificationsResources.getCorrespondence(id.identifier()).toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetCorrespondenceAssociations() throws RmesException {
        when(classificationsService.getCorrespondenceAssociations(id.identifier())).thenReturn("mocked result");
        ClassificationsResources classificationsResources = new ClassificationsResources(classificationsService,classificationItemService);
        String actual = classificationsResources.getCorrespondenceAssociations(id.identifier()).toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

    @Test
    void shouldReturnResponseWhenGetCorrespondenceItem() throws RmesException {
        when(classificationsService.getCorrespondenceAssociation(id.identifier(),"mocked associationId")).thenReturn("mocked result");
        ClassificationsResources classificationsResources = new ClassificationsResources(classificationsService,classificationItemService);
        String actual = classificationsResources.getCorrespondenceItem(id.identifier(),"mocked associationId").toString();
        Assertions.assertEquals("<200 OK OK,mocked result,[]>",actual);
    }

}

