package fr.insee.rmes.bauhaus_services.classifications;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.GenericQueries;
import fr.insee.rmes.persistance.sparql_queries.classifications.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@AppSpringBootTest
class ClassificationsImplTest {

    @Mock
    RepositoryGestion repoGestion;

    @Mock
    ClassificationNoteService classificationNoteService;

    @BeforeAll
    static void initGenericQueries(){
        GenericQueries.setConfig(new ConfigStub());
    }

    String mockedComplexResult = "{\"id\":\"mocked result\"}";
    String mockedSingleResult="[\"mocked result\"]";
    JSONObject mockedJsonObject = new JSONObject().put("id","mocked result");
    JSONArray mockedJsonArray = new JSONArray().put("mocked result");

    @Test
    void shouldGetFamily() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, new ClassificationRepository(classificationNoteService), new ClassificationPublication());
        when(repoGestion.getResponseAsObject(ClassificationFamiliesQueries.familyQuery("mocked ID"))).thenReturn(mockedJsonObject);
        String actual = classificationImpl.getFamily("mocked ID");
        Assertions.assertEquals(mockedComplexResult ,actual);
    }

    @Test
    void shouldGetFamilyMembers() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, new ClassificationRepository(classificationNoteService), new ClassificationPublication());
        when(repoGestion.getResponseAsArray(ClassificationFamiliesQueries.familyMembersQuery("mocked ID"))).thenReturn(mockedJsonArray);
        String actual = classificationImpl.getFamilyMembers("mocked ID");
        Assertions.assertEquals(mockedSingleResult,actual);
    }

    @Test
    void shouldGetOneSeries() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, new ClassificationRepository(classificationNoteService), new ClassificationPublication());
        when(repoGestion.getResponseAsObject(ClassificationSeriesQueries.oneSeriesQuery("mocked ID"))).thenReturn(mockedJsonObject);
        String actual = classificationImpl.getOneSeries("mocked ID");
        Assertions.assertEquals(mockedComplexResult,actual);
    }

    @Test
    void shouldGetSeriesMembers() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, new ClassificationRepository(classificationNoteService), new ClassificationPublication());
        when(repoGestion.getResponseAsArray(ClassificationSeriesQueries.seriesMembersQuery("mocked ID"))).thenReturn(mockedJsonObject.names());
        String actual = classificationImpl.getSeriesMembers("mocked ID");
        Assertions.assertEquals("[\"id\"]",actual);
    }

    @Test
    void shouldGetClassification() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, new ClassificationRepository(classificationNoteService), new ClassificationPublication());
        when(repoGestion.getResponseAsObject(ClassificationsQueries.classificationQuery("mocked ID"))).thenReturn(mockedJsonObject);
        String actual = classificationImpl.getClassification("mocked ID");
        Assertions.assertEquals(mockedComplexResult,actual);
    }

    @Test
    void shouldGetClassificationLevels() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, new ClassificationRepository(classificationNoteService), new ClassificationPublication());
        when(repoGestion.getResponseAsArray(ClassificationLevelsQueries.levelsQuery("mocked ID"))).thenReturn(mockedJsonObject.names());
        String actual = classificationImpl.getClassificationLevels("mocked ID");
        Assertions.assertEquals("[\"id\"]",actual);
    }

    @Test
    void shouldGetClassificationLevel() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, new ClassificationRepository(classificationNoteService), new ClassificationPublication());
        when(repoGestion.getResponseAsObject(ClassificationLevelsQueries.levelQuery("mocked classificationId", "mocked levelId"))).thenReturn(mockedJsonObject);
        String actual = classificationImpl.getClassificationLevel("mocked classificationId", "mocked levelId");
        Assertions.assertEquals(mockedComplexResult,actual);
    }

    @Test
    void shouldGetClassificationLevelMembers() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, new ClassificationRepository(classificationNoteService), new ClassificationPublication());
        when(repoGestion.getResponseAsArray(ClassificationLevelsQueries.levelMembersQuery("mocked classificationId", "mocked levelId"))).thenReturn(mockedJsonArray);
        String actual = classificationImpl.getClassificationLevelMembers("mocked classificationId", "mocked levelId");
        Assertions.assertEquals(mockedSingleResult,actual);
    }

    @Test
    void shouldGetCorrespondences() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, new ClassificationRepository(classificationNoteService), new ClassificationPublication());
        when(repoGestion.getResponseAsArray(ClassificationCorrespondencesQueries.correspondencesQuery())).thenReturn(mockedJsonArray);
        String actual = classificationImpl.getCorrespondences();
        Assertions.assertEquals(mockedSingleResult,actual);
    }

    @Test
    void shouldGetCorrespondenceAssociations() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, new ClassificationRepository(classificationNoteService), new ClassificationPublication());
        when(repoGestion.getResponseAsArray(ClassificationCorrespondencesQueries.correspondenceAssociationsQuery("mocked id"))).thenReturn(mockedJsonArray);
        String actual = classificationImpl.getCorrespondenceAssociations("mocked id");
        Assertions.assertEquals(mockedSingleResult,actual);
    }

    @Test
    void shouldGetClassificationFamiliesList() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, null, null);

        JSONArray array = new JSONArray();
        array.put(new JSONObject().put("id", "1").put("value", "value 1"));
        array.put(new JSONObject().put("id", "2").put("value", "elabel 1"));
        array.put(new JSONObject().put("id", "3").put("value", "alabel 1"));
        array.put(new JSONObject().put("id", "4").put("value", "élabel 1"));
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
        assertEquals("value 1", families.get(3).label());
    }

    @Test
    void shouldGetClassificationSeriesList() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, null, null);

        JSONArray array = new JSONArray();
        array.put(new JSONObject().put("id", "1").put("value", "value 1").put("altLabels", "value"));
        array.put(new JSONObject().put("id", "1").put("value", "value 1").put("altLabels", "value 2"));
        array.put(new JSONObject().put("id", "2").put("value", "elabel 1").put("altLabels", "value"));
        array.put(new JSONObject().put("id", "3").put("value", "alabel 1").put("altLabels", "value"));
        array.put(new JSONObject().put("id", "4").put("value", "élabel 1").put("altLabels", "value"));
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(array);
        var series = classificationImpl.getSeries().stream().toList();

        assertEquals(4, series.size());

        assertEquals("3", series.getFirst().id());
        assertEquals("alabel 1", series.get(0).label());
        assertEquals("value", series.get(0).altLabels());

        assertEquals("2", series.get(1).id());
        assertEquals("elabel 1", series.get(1).label());
        assertEquals("value", series.get(1).altLabels());

        assertEquals("4", series.get(2).id());
        assertEquals("élabel 1", series.get(2).label());
        assertEquals("value", series.get(2).altLabels());

        assertEquals("1", series.get(3).id());
        assertEquals("value 1", series.get(3).label());
        assertEquals("value || value 2", series.get(3).altLabels());
    }

    @Test
    void shouldGetClassificationList() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, null, null);

        JSONArray array = new JSONArray();
        array.put(new JSONObject().put("id", "1").put("value", "value 1").put("altLabels", "value"));
        array.put(new JSONObject().put("id", "1").put("value", "value 1").put("altLabels", "value 2"));
        array.put(new JSONObject().put("id", "2").put("value", "elabel 1").put("altLabels", "value"));
        array.put(new JSONObject().put("id", "3").put("value", "alabel 1").put("altLabels", "value"));
        array.put(new JSONObject().put("id", "4").put("value", "élabel 1").put("altLabels", "value"));
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(array);
        var series = classificationImpl.getClassifications().stream().toList();

        assertEquals(4, series.size());

        assertEquals("3", series.getFirst().id());
        assertEquals("alabel 1", series.get(0).label());
        assertEquals("value", series.get(0).altLabels());

        assertEquals("2", series.get(1).id());
        assertEquals("elabel 1", series.get(1).label());
        assertEquals("value", series.get(1).altLabels());

        assertEquals("4", series.get(2).id());
        assertEquals("élabel 1", series.get(2).label());
        assertEquals("value", series.get(2).altLabels());

        assertEquals("1", series.get(3).id());
        assertEquals("value 1", series.get(3).label());
        assertEquals("value || value 2", series.get(3).altLabels());
    }

    @Test
    void shouldThrowRmesExceptionWhenUpdateClassification() {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl( repoGestion, null, null);
        RmesException exception = assertThrows(RmesNotFoundException.class, () -> classificationImpl.updateClassification("idExample","bodyExample"));
        Assertions.assertTrue(exception.getDetails().contains("{\"details\":\"Can't read request body\",\"message\":\"1142 "));
    }


    @Test
    void shouldThrowRmesExceptionWhenSetClassificationValidation() throws RmesException {
        String classificationId = "2025";
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, null, null);
        when(repoGestion.getResponseAsObject(ClassificationsQueries.getGraphUriById(classificationId))).thenReturn(new JSONObject());
        RmesException exception = assertThrows(RmesException.class, () -> classificationImpl.setClassificationValidation(classificationId));
        Assertions.assertEquals("{\"details\":\"2025\",\"message\":\"1141 : Classification not found\"}", exception.getDetails());
    }

}