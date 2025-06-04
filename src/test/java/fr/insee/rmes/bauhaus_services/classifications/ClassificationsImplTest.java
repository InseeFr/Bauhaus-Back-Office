package fr.insee.rmes.bauhaus_services.classifications;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;
import fr.insee.rmes.persistance.sparql_queries.classifications.ClassifFamiliesQueries;
import fr.insee.rmes.persistance.sparql_queries.classifications.ClassifSeriesQueries;
import fr.insee.rmes.persistance.sparql_queries.classifications.ClassificationsQueries;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(properties = { "fr.insee.rmes.bauhaus.lg1=fr", "fr.insee.rmes.bauhaus.lg2=en"})
class ClassificationsImplTest {

    @Mock
    RepositoryGestion repoGestion;

    @BeforeAll
    static void initGenericQueries(){
        GenericQueries.setConfig(new ConfigStub());
    }

    @Test
    void shouldGetFamily() throws RmesException {
        ClassificationsImpl classificationImpl = new ClassificationsImpl(repoGestion, new ClassificationUtils(), new ClassificationPublication());
        when(repoGestion.getResponseAsObject(ClassifFamiliesQueries.familyQuery("mocked ID"))).thenReturn(new JSONObject().put("id","mocked result"));
        String actual = classificationImpl.getFamily("mocked ID");
        Assertions.assertEquals("{\"id\":\"mocked result\"}",actual);
    }

    @Test
    void shouldGetFamilyMembers() throws RmesException {
        ClassificationsImpl classificationImpl = new ClassificationsImpl(repoGestion, new ClassificationUtils(), new ClassificationPublication());
        when(repoGestion.getResponseAsArray(ClassifFamiliesQueries.familyMembersQuery("mocked ID"))).thenReturn(new JSONArray().put("mocked result"));
        String actual = classificationImpl.getFamilyMembers("mocked ID");
        Assertions.assertEquals("[\"mocked result\"]",actual);
    }

    @Test
    void shouldGetOneSeries() throws RmesException {
        ClassificationsImpl classificationImpl = new ClassificationsImpl(repoGestion, new ClassificationUtils(), new ClassificationPublication());
        when(repoGestion.getResponseAsObject(ClassifSeriesQueries.oneSeriesQuery("mocked ID"))).thenReturn(new JSONObject().put("id","mocked result"));
        String actual = classificationImpl.getOneSeries("mocked ID");
        Assertions.assertEquals("{\"id\":\"mocked result\"}",actual);
    }

    @Test
    void shouldGetClassificationFamiliesList() throws RmesException {
        ClassificationsImpl classificationImpl = new ClassificationsImpl(repoGestion, null, null);

        JSONArray array = new JSONArray();
        array.put(new JSONObject().put("id", "1").put("label", "label 1"));
        array.put(new JSONObject().put("id", "2").put("label", "elabel 1"));
        array.put(new JSONObject().put("id", "3").put("label", "alabel 1"));
        array.put(new JSONObject().put("id", "4").put("label", "élabel 1"));
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(array);
        var families = classificationImpl.getFamilies().stream().toList();

        assertEquals(4, families.size());

        assertEquals("3", families.get(0).id());
        assertEquals("alabel 1", families.get(0).label());

        assertEquals("2", families.get(1).id());
        assertEquals("elabel 1", families.get(1).label());

        assertEquals("4", families.get(2).id());
        assertEquals("élabel 1", families.get(2).label());

        assertEquals("1", families.get(3).id());
        assertEquals("label 1", families.get(3).label());
    }

    @Test
    void shouldGetClassificationSeriesList() throws RmesException {
        ClassificationsImpl classificationImpl = new ClassificationsImpl(repoGestion, null, null);

        JSONArray array = new JSONArray();
        array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabels", "label"));
        array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabels", "label 2"));
        array.put(new JSONObject().put("id", "2").put("label", "elabel 1").put("altLabels", "label"));
        array.put(new JSONObject().put("id", "3").put("label", "alabel 1").put("altLabels", "label"));
        array.put(new JSONObject().put("id", "4").put("label", "élabel 1").put("altLabels", "label"));
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(array);
        var series = classificationImpl.getSeries().stream().toList();

        assertEquals(4, series.size());

        assertEquals("3", series.get(0).id());
        assertEquals("alabel 1", series.get(0).label());
        assertEquals("label", series.get(0).altLabels());

        assertEquals("2", series.get(1).id());
        assertEquals("elabel 1", series.get(1).label());
        assertEquals("label", series.get(1).altLabels());

        assertEquals("4", series.get(2).id());
        assertEquals("élabel 1", series.get(2).label());
        assertEquals("label", series.get(2).altLabels());

        assertEquals("1", series.get(3).id());
        assertEquals("label 1", series.get(3).label());
        assertEquals("label || label 2", series.get(3).altLabels());
    }

    @Test
    void shouldGetClassificationList() throws RmesException {
        ClassificationsImpl classificationImpl = new ClassificationsImpl(repoGestion, null, null);

        JSONArray array = new JSONArray();
        array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabels", "label"));
        array.put(new JSONObject().put("id", "1").put("label", "label 1").put("altLabels", "label 2"));
        array.put(new JSONObject().put("id", "2").put("label", "elabel 1").put("altLabels", "label"));
        array.put(new JSONObject().put("id", "3").put("label", "alabel 1").put("altLabels", "label"));
        array.put(new JSONObject().put("id", "4").put("label", "élabel 1").put("altLabels", "label"));
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(array);
        var series = classificationImpl.getClassifications().stream().toList();

        assertEquals(4, series.size());

        assertEquals("3", series.get(0).id());
        assertEquals("alabel 1", series.get(0).label());
        assertEquals("label", series.get(0).altLabels());

        assertEquals("2", series.get(1).id());
        assertEquals("elabel 1", series.get(1).label());
        assertEquals("label", series.get(1).altLabels());

        assertEquals("4", series.get(2).id());
        assertEquals("élabel 1", series.get(2).label());
        assertEquals("label", series.get(2).altLabels());

        assertEquals("1", series.get(3).id());
        assertEquals("label 1", series.get(3).label());
        assertEquals("label || label 2", series.get(3).altLabels());
    }

    @Test
    void shouldThrowRmesExceptionWhenUpdateClassification() {
        ClassificationsImpl classificationImpl = new ClassificationsImpl( repoGestion, null, null);
        RmesException exception = assertThrows(RmesNotFoundException.class, () -> classificationImpl.updateClassification("idExample","bodyExample"));
        Assertions.assertTrue(exception.getDetails().contains("{\"details\":\"Can't read request body\",\"message\":\"1142 "));
    }


    @Test
    void shouldThrowRmesExceptionWhenSetClassificationValidation() throws RmesException {
        String classificationId = "2025";
        ClassificationsImpl classificationImpl = new ClassificationsImpl(repoGestion, null, null);
        when(repoGestion.getResponseAsObject(ClassificationsQueries.getGraphUriById(classificationId))).thenReturn(new JSONObject());
        RmesException exception = assertThrows(RmesException.class, () -> classificationImpl.setClassificationValidation(classificationId));
        Assertions.assertEquals("{\"details\":\"2025\",\"message\":\"1141 : Classification not found\"}", exception.getDetails());
    }

}