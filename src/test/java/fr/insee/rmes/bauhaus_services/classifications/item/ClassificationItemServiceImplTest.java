package fr.insee.rmes.bauhaus_services.classifications.item;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.classifications.ItemsQueries;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(properties = { "fr.insee.rmes.bauhaus.lg1=fr", "fr.insee.rmes.bauhaus.lg2=en"})
class ClassificationItemServiceImplTest {

    @InjectMocks
    ClassificationItemServiceImpl classificationItemServiceImpl = new ClassificationItemServiceImpl();

    @MockitoBean
    RepositoryGestion repoGestion;

    String classificationId = "classificationID";
    String itemId = "itemId";
    String body = "fake body for Json Object";
    JSONObject item = new JSONObject().put("A", "letterA").put("B", "letterB");


    @Test
    void shouldGetClassificationItemsWhenAltLabelNotNull() throws RmesException {
        JSONArray altLabels = new JSONArray().put(new JSONObject().put("C", "letterC").put("D", "letterD"));
        when(repoGestion.getResponseAsObject(ItemsQueries.itemQuery(classificationId, itemId))).thenReturn(item);
        when(repoGestion.getResponseAsArray(ItemsQueries.itemAltQuery(classificationId, itemId))).thenReturn(altLabels);
        String actual = classificationItemServiceImpl.getClassificationItem(classificationId, itemId);
        String expected = "{\"A\":\"letterA\",\"B\":\"letterB\",\"altLabels\":[{\"C\":\"letterC\",\"D\":\"letterD\"}]}";
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldGetClassificationItemsWhenAltLabelIsNull() throws RmesException {
        when(repoGestion.getResponseAsObject(ItemsQueries.itemQuery(classificationId, itemId))).thenReturn(item);
        when(repoGestion.getResponseAsArray(ItemsQueries.itemAltQuery(classificationId, itemId))).thenReturn(new JSONArray());
        String actual = classificationItemServiceImpl.getClassificationItem(classificationId, itemId);
        Assertions.assertEquals(item.toString(), actual);
    }

    @Test
    void shouldThrowRmesExceptionWhenUpdateClassificationItem(){
        RmesException exception = assertThrows(RmesException.class, () -> classificationItemServiceImpl.updateClassificationItem(classificationId,itemId,body));
        Assertions.assertTrue(exception.getDetails().contains("{\"details\":\"Can't read request body\""));
    }
}
