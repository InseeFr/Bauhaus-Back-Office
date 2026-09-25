package fr.insee.rmes.bauhaus_services.classifications;

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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.modules.classifications.nomenclatures.model.PartialClassification;
import fr.insee.rmes.modules.classifications.series.model.PartialClassificationSeries;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.persistance.sparql_queries.classifications.*;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.List;
import java.util.function.Function;
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
        classificationsQueries =
                new ClassificationsQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());
        classificationLevelsQueries = new ClassificationLevelsQueries(new BauhausLanguagesProperties("fr", "en"));
        classificationSeriesQueries = new ClassificationSeriesQueries(
                new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());
        classificationFamiliesQueries = new ClassificationFamiliesQueries(
                new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());
        classificationCorrespondencesQueries =
                new ClassificationCorrespondencesQueries(new BauhausLanguagesProperties("fr", "en"));
    }

    String mockedComplexResult = "{\"id\":\"mocked result\"}";
    String mockedSingleResult = "[\"mocked result\"]";
    JSONObject mockedJsonObject = new JSONObject().put("id", "mocked result");
    JSONArray mockedJsonArray = new JSONArray().put("mocked result");

    @Test
    void shouldGetFamily() throws RmesException {
        ClassificationsServiceImpl classificationImpl = serviceWithRepositoryAndPublication();
        when(repoGestion.getResponseAsObject(classificationFamiliesQueries.familyQuery("mocked ID")))
                .thenReturn(mockedJsonObject);
        String actual = classificationImpl.getFamily("mocked ID");
        Assertions.assertEquals(mockedComplexResult, actual);
    }

    @Test
    void shouldGetFamilyMembers() throws RmesException {
        ClassificationsServiceImpl classificationImpl = serviceWithRepositoryAndPublication();
        when(repoGestion.getResponseAsArray(classificationFamiliesQueries.familyMembersQuery("mocked ID")))
                .thenReturn(mockedJsonArray);
        String actual = classificationImpl.getFamilyMembers("mocked ID");
        Assertions.assertEquals(mockedSingleResult, actual);
    }

    @Test
    void shouldGetOneSeries() throws RmesException {
        ClassificationsServiceImpl classificationImpl = serviceWithRepositoryAndPublication();
        when(repoGestion.getResponseAsObject(classificationSeriesQueries.oneSeriesQuery("mocked ID")))
                .thenReturn(mockedJsonObject);
        String actual = classificationImpl.getOneSeries("mocked ID");
        Assertions.assertEquals(mockedComplexResult, actual);
    }

    @Test
    void shouldGetSeriesMembers() throws RmesException {
        ClassificationsServiceImpl classificationImpl = serviceWithRepositoryAndPublication();
        when(repoGestion.getResponseAsArray(classificationSeriesQueries.seriesMembersQuery("mocked ID")))
                .thenReturn(mockedJsonObject.names());
        String actual = classificationImpl.getSeriesMembers("mocked ID");
        Assertions.assertEquals("[\"id\"]", actual);
    }

    @Test
    void shouldGetClassification() throws RmesException {
        ClassificationsServiceImpl classificationImpl = serviceWithRepositoryAndPublication();
        when(repoGestion.getResponseAsObject(classificationsQueries.classificationQuery("mocked ID")))
                .thenReturn(mockedJsonObject);
        String actual = classificationImpl.getClassification("mocked ID");
        Assertions.assertEquals(mockedComplexResult, actual);
    }

    @Test
    void shouldGetClassificationLevels() throws RmesException {
        ClassificationsServiceImpl classificationImpl = serviceWithRepositoryAndPublication();
        when(repoGestion.getResponseAsArray(classificationLevelsQueries.levelsQuery("mocked ID")))
                .thenReturn(mockedJsonObject.names());
        String actual = classificationImpl.getClassificationLevels("mocked ID");
        Assertions.assertEquals("[\"id\"]", actual);
    }

    @Test
    void shouldGetClassificationLevel() throws RmesException {
        ClassificationsServiceImpl classificationImpl = serviceWithRepositoryAndPublication();
        when(repoGestion.getResponseAsObject(
                        classificationLevelsQueries.levelQuery("mocked classificationId", "mocked levelId")))
                .thenReturn(mockedJsonObject);
        String actual = classificationImpl.getClassificationLevel("mocked classificationId", "mocked levelId");
        Assertions.assertEquals(mockedComplexResult, actual);
    }

    @Test
    void shouldGetClassificationLevelMembers() throws RmesException {
        ClassificationsServiceImpl classificationImpl = serviceWithRepositoryAndPublication();
        when(repoGestion.getResponseAsArray(
                        classificationLevelsQueries.levelMembersQuery("mocked classificationId", "mocked levelId")))
                .thenReturn(mockedJsonArray);
        String actual = classificationImpl.getClassificationLevelMembers("mocked classificationId", "mocked levelId");
        Assertions.assertEquals(mockedSingleResult, actual);
    }

    @Test
    void shouldGetCorrespondences() throws RmesException {
        ClassificationsServiceImpl classificationImpl = serviceWithRepositoryAndPublication();
        when(repoGestion.getResponseAsArray(classificationCorrespondencesQueries.correspondencesQuery()))
                .thenReturn(mockedJsonArray);
        String actual = classificationImpl.getCorrespondences();
        Assertions.assertEquals(mockedSingleResult, actual);
    }

    @Test
    void shouldGetCorrespondenceAssociations() throws RmesException {
        ClassificationsServiceImpl classificationImpl = serviceWithRepositoryAndPublication();
        when(repoGestion.getResponseAsArray(
                        classificationCorrespondencesQueries.correspondenceAssociationsQuery("mocked id")))
                .thenReturn(mockedJsonArray);
        String actual = classificationImpl.getCorrespondenceAssociations("mocked id");
        Assertions.assertEquals(mockedSingleResult, actual);
    }

    @Test
    void shouldGetClassificationFamiliesList() throws RmesException {
        ClassificationsServiceImpl classificationImpl = service(null);

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
        ClassificationsServiceImpl classificationImpl = service(null);

        when(repoGestion.getResponseAsArray(anyString())).thenReturn(rowsWithAltLabels());
        var series = classificationImpl.getSeries().stream().toList();

        assertSortedByLabelWithMergedAltLabels(
                series,
                PartialClassificationSeries::id,
                PartialClassificationSeries::label,
                PartialClassificationSeries::altLabels);
    }

    @Test
    void shouldGetClassificationList() throws RmesException {
        ClassificationsServiceImpl classificationImpl = service(null);

        when(repoGestion.getResponseAsArray(anyString())).thenReturn(rowsWithAltLabels());
        var classifications = classificationImpl.getClassifications().stream().toList();

        assertSortedByLabelWithMergedAltLabels(
                classifications,
                PartialClassification::id,
                PartialClassification::label,
                PartialClassification::altLabels);
    }

    @Test
    void shouldThrowRmesExceptionWhenUpdateClassification() {
        ClassificationsServiceImpl classificationImpl = service(null);
        RmesException exception = assertThrows(
                RmesNotFoundException.class, () -> classificationImpl.updateClassification("idExample", "bodyExample"));
        Assertions.assertTrue(
                exception.getDetails().contains("{\"code\":1142,\"details\":\"Can't read request body\""));
    }

    @Test
    void setClassificationValidation_shouldReturn400_whenTheClassificationIsAlreadyPublished() throws RmesException {
        String classificationId = "naf2025";
        ClassificationPublication classificationPublication = mock(ClassificationPublication.class);
        ClassificationsServiceImpl classificationImpl = service(classificationPublication);
        givenStoredClassification(
                classificationId,
                "http://rdf.insee.fr/graphes/codes/" + classificationId,
                "http://rdf.insee.fr/codes/naf2025/",
                "Validated");

        RmesBadRequestException exception = assertThrows(
                RmesBadRequestException.class, () -> classificationImpl.setClassificationValidation(classificationId));

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
        ClassificationsServiceImpl classificationImpl = service(classificationPublication);
        givenStoredClassification(
                classificationId,
                "http://rdf.insee.fr/graphes/codes/" + classificationId,
                classificationUri,
                "Unpublished");

        classificationImpl.setClassificationValidation(classificationId);

        ArgumentCaptor<Model> model = ArgumentCaptor.forClass(Model.class);
        verify(repoGestion).objectValidation(eq(RdfUtils.toURI(classificationUri)), model.capture());
        assertThat(model.getValue().contexts())
                .as("le validationState doit être écrit dans le graphe nomenclatures, celui que lit getClassification")
                .containsExactly(RdfUtils.createIRI(GraphsPropertiesStub.stub().classifFamiliesGraph()));
        assertThat(model.getValue().objects()).containsExactly(RdfUtils.setLiteralString(ValidationStatus.VALIDATED));
    }

    @Test
    void setClassificationValidation_logsTheInconsistencyWhenTheManagementRepositoryCannotBeMarkedAsValidated()
            throws RmesException {
        String classificationId = "naf2025";
        String classificationGraph = "http://rdf.insee.fr/graphes/codes/" + classificationId;
        ClassificationsServiceImpl classificationImpl = service(mock(ClassificationPublication.class));
        givenStoredClassification(
                classificationId,
                classificationGraph,
                "http://rdf.insee.fr/codes/" + classificationId + "/",
                "Unpublished");
        RmesException markingFailure = new RmesException(500, "Failure validation", "connection reset");
        doThrow(markingFailure).when(repoGestion).objectValidation(any(), any());

        Logger logger = (Logger) LoggerFactory.getLogger(ClassificationsServiceImpl.class);
        ListAppender<ILoggingEvent> logs = new ListAppender<>();
        logs.start();
        logger.addAppender(logs);
        try {
            RmesException thrown = assertThrows(
                    RmesException.class, () -> classificationImpl.setClassificationValidation(classificationId));

            assertThat(thrown).as("l'échec doit rester visible de l'appelant").isSameAs(markingFailure);
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
        ClassificationsServiceImpl classificationImpl = service(null);
        when(repoGestion.getResponseAsObject(classificationsQueries.getGraphUriById(classificationId)))
                .thenReturn(new JSONObject());
        RmesException exception = assertThrows(
                RmesException.class, () -> classificationImpl.setClassificationValidation(classificationId));
        Assertions.assertEquals(
                "{\"code\":1141,\"details\":\"2025\",\"message\":\"Classification not found\"}",
                exception.getDetails());
    }

    /** Service dont le dépôt et la publication sont réels mais sans dépendances. */
    private ClassificationsServiceImpl serviceWithRepositoryAndPublication() {
        return new ClassificationsServiceImpl(
                repoGestion,
                new ClassificationRepository(
                        null,
                        null,
                        null,
                        null,
                        null,
                        classificationNoteService,
                        classificationsQueries,
                        GraphsPropertiesStub.stub()),
                new ClassificationPublication(null, null, null, null),
                classificationsQueries,
                classificationLevelsQueries,
                classificationSeriesQueries,
                classificationFamiliesQueries,
                classificationCorrespondencesQueries,
                GraphsPropertiesStub.stub());
    }

    private ClassificationsServiceImpl service(ClassificationPublication classificationPublication) {
        return new ClassificationsServiceImpl(
                repoGestion,
                null,
                classificationPublication,
                classificationsQueries,
                classificationLevelsQueries,
                classificationSeriesQueries,
                classificationFamiliesQueries,
                classificationCorrespondencesQueries,
                GraphsPropertiesStub.stub());
    }

    private void givenStoredClassification(String classificationId, String graph, String uri, String validationState)
            throws RmesException {
        when(repoGestion.getResponseAsObject(classificationsQueries.getGraphUriById(classificationId)))
                .thenReturn(new JSONObject().put("graph", graph).put("uri", uri));
        when(repoGestion.getResponseAsObject(classificationsQueries.classificationQuery(classificationId)))
                .thenReturn(new JSONObject().put("validationState", validationState));
    }

    private static JSONArray rowsWithAltLabels() {
        JSONArray array = new JSONArray();
        array.put(new JSONObject().put("id", "1").put("label", "value 1").put("altLabels", "label"));
        array.put(new JSONObject().put("id", "1").put("label", "value 1").put("altLabels", "value 2"));
        array.put(new JSONObject().put("id", "2").put("label", "elabel 1").put("altLabels", "label"));
        array.put(new JSONObject().put("id", "3").put("label", "alabel 1").put("altLabels", "label"));
        array.put(new JSONObject().put("id", "4").put("label", "élabel 1").put("altLabels", "label"));
        return array;
    }

    /** Attendu pour {@link #rowsWithAltLabels()} : tri par libellé, altLabels du même id fusionnés. */
    private static <T> void assertSortedByLabelWithMergedAltLabels(
            List<T> actual, Function<T, String> id, Function<T, String> label, Function<T, String> altLabels) {
        assertEquals(4, actual.size());

        assertEquals("3", id.apply(actual.getFirst()));
        assertEquals("alabel 1", label.apply(actual.get(0)));
        assertEquals("label", altLabels.apply(actual.get(0)));

        assertEquals("2", id.apply(actual.get(1)));
        assertEquals("elabel 1", label.apply(actual.get(1)));
        assertEquals("label", altLabels.apply(actual.get(1)));

        assertEquals("4", id.apply(actual.get(2)));
        assertEquals("élabel 1", label.apply(actual.get(2)));
        assertEquals("label", altLabels.apply(actual.get(2)));

        assertEquals("1", id.apply(actual.get(3)));
        assertEquals("value 1", label.apply(actual.get(3)));
        assertEquals("label || value 2", altLabels.apply(actual.get(3)));
    }
}
