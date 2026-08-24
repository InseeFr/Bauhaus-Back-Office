package fr.insee.rmes.bauhaus_services.classifications;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.persistance.sparql_queries.classifications.*;
import org.eclipse.rdf4j.model.Model;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@AppSpringBootTest
class ClassificationsImplTest {

    @Mock
    RepositoryGestion repoGestion;

    @Mock
    ClassificationNoteService classificationNoteService;

    private ClassificationsQueries classificationsQueries;
    private ClassificationLevelsQueries classificationLevelsQueries;
    private ClassificationSeriesQueries classificationSeriesQueries;
    private ClassificationFamiliesQueries classificationFamiliesQueries;
    private ClassificationCorrespondencesQueries classificationCorrespondencesQueries;

    @BeforeEach
    void setUp() {
        classificationsQueries = new ClassificationsQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());
        classificationLevelsQueries = new ClassificationLevelsQueries(new BauhausLanguagesProperties("fr", "en"));
        classificationSeriesQueries = new ClassificationSeriesQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());
        classificationFamiliesQueries = new ClassificationFamiliesQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());
        classificationCorrespondencesQueries = new ClassificationCorrespondencesQueries(new BauhausLanguagesProperties("fr", "en"));
    }

    String mockedComplexResult = "{\"id\":\"mocked result\"}";
    String mockedSingleResult="[\"mocked result\"]";
    JSONObject mockedJsonObject = new JSONObject().put("id","mocked result");
    JSONArray mockedJsonArray = new JSONArray().put("mocked result");

    @Test
    void shouldGetFamily() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, new ClassificationRepository(null, null, null, null, null, classificationNoteService, classificationsQueries, GraphsPropertiesStub.stub()), new ClassificationPublication(null, null, null, null), classificationsQueries, classificationLevelsQueries, classificationSeriesQueries, classificationFamiliesQueries, classificationCorrespondencesQueries, GraphsPropertiesStub.stub());
        when(repoGestion.getResponseAsObject(classificationFamiliesQueries.familyQuery("mocked ID"))).thenReturn(mockedJsonObject);
        String actual = classificationImpl.getFamily("mocked ID");
        Assertions.assertEquals(mockedComplexResult ,actual);
    }

    @Test
    void shouldGetFamilyMembers() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, new ClassificationRepository(null, null, null, null, null, classificationNoteService, classificationsQueries, GraphsPropertiesStub.stub()), new ClassificationPublication(null, null, null, null), classificationsQueries, classificationLevelsQueries, classificationSeriesQueries, classificationFamiliesQueries, classificationCorrespondencesQueries, GraphsPropertiesStub.stub());
        when(repoGestion.getResponseAsArray(classificationFamiliesQueries.familyMembersQuery("mocked ID"))).thenReturn(mockedJsonArray);
        String actual = classificationImpl.getFamilyMembers("mocked ID");
        Assertions.assertEquals(mockedSingleResult,actual);
    }

    @Test
    void shouldGetOneSeries() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, new ClassificationRepository(null, null, null, null, null, classificationNoteService, classificationsQueries, GraphsPropertiesStub.stub()), new ClassificationPublication(null, null, null, null), classificationsQueries, classificationLevelsQueries, classificationSeriesQueries, classificationFamiliesQueries, classificationCorrespondencesQueries, GraphsPropertiesStub.stub());
        when(repoGestion.getResponseAsObject(classificationSeriesQueries.oneSeriesQuery("mocked ID"))).thenReturn(mockedJsonObject);
        String actual = classificationImpl.getOneSeries("mocked ID");
        Assertions.assertEquals(mockedComplexResult,actual);
    }

    @Test
    void shouldGetSeriesMembers() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, new ClassificationRepository(null, null, null, null, null, classificationNoteService, classificationsQueries, GraphsPropertiesStub.stub()), new ClassificationPublication(null, null, null, null), classificationsQueries, classificationLevelsQueries, classificationSeriesQueries, classificationFamiliesQueries, classificationCorrespondencesQueries, GraphsPropertiesStub.stub());
        when(repoGestion.getResponseAsArray(classificationSeriesQueries.seriesMembersQuery("mocked ID"))).thenReturn(mockedJsonObject.names());
        String actual = classificationImpl.getSeriesMembers("mocked ID");
        Assertions.assertEquals("[\"id\"]",actual);
    }

    @Test
    void shouldGetClassification() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, new ClassificationRepository(null, null, null, null, null, classificationNoteService, classificationsQueries, GraphsPropertiesStub.stub()), new ClassificationPublication(null, null, null, null), classificationsQueries, classificationLevelsQueries, classificationSeriesQueries, classificationFamiliesQueries, classificationCorrespondencesQueries, GraphsPropertiesStub.stub());
        when(repoGestion.getResponseAsObject(classificationsQueries.classificationQuery("mocked ID"))).thenReturn(mockedJsonObject);
        String actual = classificationImpl.getClassification("mocked ID");
        Assertions.assertEquals(mockedComplexResult,actual);
    }

    @Test
    void shouldGetClassificationLevels() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, new ClassificationRepository(null, null, null, null, null, classificationNoteService, classificationsQueries, GraphsPropertiesStub.stub()), new ClassificationPublication(null, null, null, null), classificationsQueries, classificationLevelsQueries, classificationSeriesQueries, classificationFamiliesQueries, classificationCorrespondencesQueries, GraphsPropertiesStub.stub());
        when(repoGestion.getResponseAsArray(classificationLevelsQueries.levelsQuery("mocked ID"))).thenReturn(mockedJsonObject.names());
        String actual = classificationImpl.getClassificationLevels("mocked ID");
        Assertions.assertEquals("[\"id\"]",actual);
    }

    @Test
    void shouldGetClassificationLevel() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, new ClassificationRepository(null, null, null, null, null, classificationNoteService, classificationsQueries, GraphsPropertiesStub.stub()), new ClassificationPublication(null, null, null, null), classificationsQueries, classificationLevelsQueries, classificationSeriesQueries, classificationFamiliesQueries, classificationCorrespondencesQueries, GraphsPropertiesStub.stub());
        when(repoGestion.getResponseAsObject(classificationLevelsQueries.levelQuery("mocked classificationId", "mocked levelId"))).thenReturn(mockedJsonObject);
        String actual = classificationImpl.getClassificationLevel("mocked classificationId", "mocked levelId");
        Assertions.assertEquals(mockedComplexResult,actual);
    }

    @Test
    void shouldGetClassificationLevelMembers() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, new ClassificationRepository(null, null, null, null, null, classificationNoteService, classificationsQueries, GraphsPropertiesStub.stub()), new ClassificationPublication(null, null, null, null), classificationsQueries, classificationLevelsQueries, classificationSeriesQueries, classificationFamiliesQueries, classificationCorrespondencesQueries, GraphsPropertiesStub.stub());
        when(repoGestion.getResponseAsArray(classificationLevelsQueries.levelMembersQuery("mocked classificationId", "mocked levelId"))).thenReturn(mockedJsonArray);
        String actual = classificationImpl.getClassificationLevelMembers("mocked classificationId", "mocked levelId");
        Assertions.assertEquals(mockedSingleResult,actual);
    }

    @Test
    void shouldGetCorrespondences() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, new ClassificationRepository(null, null, null, null, null, classificationNoteService, classificationsQueries, GraphsPropertiesStub.stub()), new ClassificationPublication(null, null, null, null), classificationsQueries, classificationLevelsQueries, classificationSeriesQueries, classificationFamiliesQueries, classificationCorrespondencesQueries, GraphsPropertiesStub.stub());
        when(repoGestion.getResponseAsArray(classificationCorrespondencesQueries.correspondencesQuery())).thenReturn(mockedJsonArray);
        String actual = classificationImpl.getCorrespondences();
        Assertions.assertEquals(mockedSingleResult,actual);
    }

    @Test
    void shouldGetCorrespondenceAssociations() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, new ClassificationRepository(null, null, null, null, null, classificationNoteService, classificationsQueries, GraphsPropertiesStub.stub()), new ClassificationPublication(null, null, null, null), classificationsQueries, classificationLevelsQueries, classificationSeriesQueries, classificationFamiliesQueries, classificationCorrespondencesQueries, GraphsPropertiesStub.stub());
        when(repoGestion.getResponseAsArray(classificationCorrespondencesQueries.correspondenceAssociationsQuery("mocked id"))).thenReturn(mockedJsonArray);
        String actual = classificationImpl.getCorrespondenceAssociations("mocked id");
        Assertions.assertEquals(mockedSingleResult,actual);
    }

    @Test
    void shouldGetClassificationFamiliesList() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, null, null, classificationsQueries, classificationLevelsQueries, classificationSeriesQueries, classificationFamiliesQueries, classificationCorrespondencesQueries, GraphsPropertiesStub.stub());

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
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, null, null, classificationsQueries, classificationLevelsQueries, classificationSeriesQueries, classificationFamiliesQueries, classificationCorrespondencesQueries, GraphsPropertiesStub.stub());

        JSONArray array = new JSONArray();
        array.put(new JSONObject().put("id", "1").put("label", "value 1").put("altLabels", "label"));
        array.put(new JSONObject().put("id", "1").put("label", "value 1").put("altLabels", "value 2"));
        array.put(new JSONObject().put("id", "2").put("label", "elabel 1").put("altLabels", "label"));
        array.put(new JSONObject().put("id", "3").put("label", "alabel 1").put("altLabels", "label"));
        array.put(new JSONObject().put("id", "4").put("label", "élabel 1").put("altLabels", "label"));
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(array);
        var series = classificationImpl.getSeries().stream().toList();

        assertEquals(4, series.size());

        assertEquals("3", series.getFirst().id());
        assertEquals("alabel 1", series.get(0).label());
        assertEquals("label", series.get(0).altLabels());

        assertEquals("2", series.get(1).id());
        assertEquals("elabel 1", series.get(1).label());
        assertEquals("label", series.get(1).altLabels());

        assertEquals("4", series.get(2).id());
        assertEquals("élabel 1", series.get(2).label());
        assertEquals("label", series.get(2).altLabels());

        assertEquals("1", series.get(3).id());
        assertEquals("value 1", series.get(3).label());
        assertEquals("label || value 2", series.get(3).altLabels());
    }

    @Test
    void shouldGetClassificationList() throws RmesException {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, null, null, classificationsQueries, classificationLevelsQueries, classificationSeriesQueries, classificationFamiliesQueries, classificationCorrespondencesQueries, GraphsPropertiesStub.stub());

        JSONArray array = new JSONArray();
        array.put(new JSONObject().put("id", "1").put("label", "value 1").put("altLabels", "label"));
        array.put(new JSONObject().put("id", "1").put("label", "value 1").put("altLabels", "value 2"));
        array.put(new JSONObject().put("id", "2").put("label", "elabel 1").put("altLabels", "label"));
        array.put(new JSONObject().put("id", "3").put("label", "alabel 1").put("altLabels", "label"));
        array.put(new JSONObject().put("id", "4").put("label", "élabel 1").put("altLabels", "label"));
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(array);
        var series = classificationImpl.getClassifications().stream().toList();

        assertEquals(4, series.size());

        assertEquals("3", series.getFirst().id());
        assertEquals("alabel 1", series.get(0).label());
        assertEquals("label", series.get(0).altLabels());

        assertEquals("2", series.get(1).id());
        assertEquals("elabel 1", series.get(1).label());
        assertEquals("label", series.get(1).altLabels());

        assertEquals("4", series.get(2).id());
        assertEquals("élabel 1", series.get(2).label());
        assertEquals("label", series.get(2).altLabels());

        assertEquals("1", series.get(3).id());
        assertEquals("value 1", series.get(3).label());
        assertEquals("label || value 2", series.get(3).altLabels());
    }

    @Test
    void shouldThrowRmesExceptionWhenUpdateClassification() {
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, null, null, classificationsQueries, classificationLevelsQueries, classificationSeriesQueries, classificationFamiliesQueries, classificationCorrespondencesQueries, GraphsPropertiesStub.stub());
        RmesException exception = assertThrows(RmesNotFoundException.class, () -> classificationImpl.updateClassification("idExample","bodyExample"));
        Assertions.assertTrue(exception.getDetails().contains("{\"code\":1142,\"details\":\"Can't read request body\""));
    }


    @Test
    void setClassificationValidation_shouldReturn400_whenTheClassificationIsAlreadyPublished() throws RmesException {
        String classificationId = "naf2025";
        ClassificationPublication classificationPublication = mock(ClassificationPublication.class);
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, null, classificationPublication, classificationsQueries, classificationLevelsQueries, classificationSeriesQueries, classificationFamiliesQueries, classificationCorrespondencesQueries, GraphsPropertiesStub.stub());
        when(repoGestion.getResponseAsObject(classificationsQueries.getGraphUriById(classificationId)))
                .thenReturn(new JSONObject()
                        .put("graph", "http://rdf.insee.fr/graphes/codes/" + classificationId)
                        .put("uri", "http://rdf.insee.fr/codes/naf2025/"));
        when(repoGestion.getResponseAsObject(classificationsQueries.classificationQuery(classificationId)))
                .thenReturn(new JSONObject().put("validationState", "Validated"));

        RmesBadRequestException exception = assertThrows(RmesBadRequestException.class,
                () -> classificationImpl.setClassificationValidation(classificationId));

        assertThat(exception.getDetails()).contains("\"code\":1301");
        assertThat(exception.getDetails()).contains("This classification is already published");
        assertThat(exception.getDetails()).contains("Classification: naf2025");
        verify(classificationPublication, never()).publishClassification(any());
        verify(repoGestion, never()).objectValidation(any(), any());
    }

    @Test
    void setClassificationValidation_writesTheValidatedStateInTheGraphReadByTheGetEndpoint() throws RmesException {
        String classificationId = "naf2025";
        String classificationUri = "http://rdf.insee.fr/codes/naf2025/";
        ClassificationPublication classificationPublication = mock(ClassificationPublication.class);
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, null, classificationPublication, classificationsQueries, classificationLevelsQueries, classificationSeriesQueries, classificationFamiliesQueries, classificationCorrespondencesQueries, GraphsPropertiesStub.stub());
        when(repoGestion.getResponseAsObject(classificationsQueries.getGraphUriById(classificationId)))
                .thenReturn(new JSONObject()
                        .put("graph", "http://rdf.insee.fr/graphes/codes/" + classificationId)
                        .put("uri", classificationUri));
        when(repoGestion.getResponseAsObject(classificationsQueries.classificationQuery(classificationId)))
                .thenReturn(new JSONObject().put("validationState", "Unpublished"));

        classificationImpl.setClassificationValidation(classificationId);

        ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);
        verify(repoGestion).objectValidation(eq(RdfUtils.toURI(classificationUri)), model.capture());
        assertThat(model.getValue().contexts())
                .as("le validationState doit être écrit dans le graphe nomenclatures, celui que lit getClassification")
                .containsExactly(RdfUtils.createIRI(GraphsPropertiesStub.stub().classifFamiliesGraph()));
        assertThat(model.getValue().objects())
                .containsExactly(RdfUtils.setLiteralString(ValidationStatus.VALIDATED));
    }

    @Test
    void setClassificationValidation_logsTheInconsistencyWhenTheManagementRepositoryCannotBeMarkedAsValidated() throws RmesException {
        String classificationId = "naf2025";
        String classificationGraph = "http://rdf.insee.fr/graphes/codes/" + classificationId;
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, null, mock(ClassificationPublication.class), classificationsQueries, classificationLevelsQueries, classificationSeriesQueries, classificationFamiliesQueries, classificationCorrespondencesQueries, GraphsPropertiesStub.stub());
        when(repoGestion.getResponseAsObject(classificationsQueries.getGraphUriById(classificationId)))
                .thenReturn(new JSONObject()
                        .put("graph", classificationGraph)
                        .put("uri", "http://rdf.insee.fr/codes/" + classificationId + "/"));
        when(repoGestion.getResponseAsObject(classificationsQueries.classificationQuery(classificationId)))
                .thenReturn(new JSONObject().put("validationState", "Unpublished"));
        RmesException markingFailure = new RmesException(500, "Failure validation", "connection reset");
        doThrow(markingFailure).when(repoGestion).objectValidation(any(), any());

        Logger logger = (Logger) LoggerFactory.getLogger(ClassificationsServiceImpl.class);
        ListAppender<ILoggingEvent> logs = new ListAppender<>();
        logs.start();
        logger.addAppender(logs);
        try {
            RmesException thrown = assertThrows(RmesException.class, () -> classificationImpl.setClassificationValidation(classificationId));

            assertThat(thrown)
                    .as("l'échec doit rester visible de l'appelant")
                    .isSameAs(markingFailure);
            assertThat(logs.list)
                    .filteredOn(event -> event.getLevel() == Level.ERROR)
                    .as("l'incohérence diffusion/gestion doit être journalisée avec de quoi la diagnostiquer")
                    .anySatisfy(event -> assertThat(event.getFormattedMessage())
                            .contains(classificationId)
                            .contains(classificationGraph));
        } finally {
            logger.detachAppender(logs);
        }
    }

    @Test
    void shouldThrowRmesExceptionWhenSetClassificationValidation() throws RmesException {
        String classificationId = "2025";
        ClassificationsServiceImpl classificationImpl = new ClassificationsServiceImpl(repoGestion, null, null, classificationsQueries, classificationLevelsQueries, classificationSeriesQueries, classificationFamiliesQueries, classificationCorrespondencesQueries, GraphsPropertiesStub.stub());
        when(repoGestion.getResponseAsObject(classificationsQueries.getGraphUriById(classificationId))).thenReturn(new JSONObject());
        RmesException exception = assertThrows(RmesException.class, () -> classificationImpl.setClassificationValidation(classificationId));
        Assertions.assertEquals("{\"code\":1141,\"details\":\"2025\",\"message\":\"Classification not found\"}", exception.getDetails());
    }

}