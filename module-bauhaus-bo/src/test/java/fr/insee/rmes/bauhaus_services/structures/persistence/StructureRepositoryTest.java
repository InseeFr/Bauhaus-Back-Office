package fr.insee.rmes.bauhaus_services.structures.persistence;

import static fr.insee.rmes.bauhaus_services.utils.StoredRdfModels.objectOf;
import static fr.insee.rmes.bauhaus_services.utils.StoredRdfModels.storedModel;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.structures.StructureComponent;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.graphdb.ontologies.QB;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.modules.structures.components.domain.model.MutualizedComponent;
import fr.insee.rmes.modules.structures.infrastructure.graphdb.StructureQueries;
import fr.insee.rmes.modules.structures.structures.domain.model.ComponentDefinition;
import fr.insee.rmes.modules.structures.structures.domain.model.Structure;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ExtendWith(MockitoExtension.class)
@AppSpringBootTest
class StructureRepositoryTest {
    @InjectMocks
    StructureRepository structureRepository;

    @Spy
    BauhausLanguagesProperties languages = new BauhausLanguagesProperties("fr", "en");

    @MockitoBean
    RepositoryGestion repositoryGestion;

    @Mock
    StructureQueries structureQueries;

    @Mock
    StructureComponent structureComponent;

    @Mock
    StructureComponentRepository structureComponentRepository;

    public static final String VALIDATION_STATUS = "{\"state\":\"Published\"}";
    public String fakeJsonObjectBody = "This a fake body of JsonObject";

    @Test
    void shouldReturnBadRequestExceptionIfPublishedStructure() throws RmesException {
        JSONObject mockJSON = new JSONObject(VALIDATION_STATUS);
        when(structureQueries.getValidationStatus(anyString())).thenReturn("validation-status-query");
        Structure structure = new Structure();
        structure.setId("id");
        when(repositoryGestion.getResponseAsObject(Mockito.anyString())).thenReturn(mockJSON);
        RmesException exception =
                assertThrows(RmesBadRequestException.class, () -> structureRepository.deleteStructure("id"));
        Assertions.assertEquals(
                "{\"code\":1103,\"message\":\"Only unpublished codelist can be deleted\"}", exception.getDetails());
    }

    @Test
    void shouldReturnNotFoundExceptionIfStructureDoesNotExist() throws RmesException {
        when(structureQueries.getValidationStatus(anyString())).thenReturn("validation-status-query");
        when(repositoryGestion.getResponseAsObject(Mockito.anyString())).thenReturn(new JSONObject());

        RmesException exception =
                assertThrows(RmesNotFoundException.class, () -> structureRepository.deleteStructure("unknown"));
        Assertions.assertEquals(404, exception.getStatus());
    }

    @Test
    void shouldThrowRmesExceptionWhenSetStructure() {
        RmesException exception =
                assertThrows(RmesException.class, () -> structureRepository.setStructure(fakeJsonObjectBody));
        Assertions.assertTrue(
                exception.getDetails().contains("{\"details\":\"IOException\",\"message\":\"Unrecognized token"));
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishingAnAlreadyPublishedStructure() {
        JSONObject jsonObject = new JSONObject()
                .put(Constants.ID, "dsd1000")
                .put(Constants.CREATOR, "creatorExample")
                .put("disseminationStatus", "http://status")
                .put("validationState", "Validated");

        RmesException exception =
                assertThrows(RmesBadRequestException.class, () -> structureRepository.publishStructure(jsonObject));

        assertThat(exception.getDetails()).contains("\"code\":1301");
        assertThat(exception.getDetails()).contains("This structure is already published");
        assertThat(exception.getDetails()).contains("Structure: dsd1000");
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishStructureWhenCreatorEmpty() {
        JSONObject jsonObject = new JSONObject().put(Constants.CREATOR, "");
        RmesException exception =
                assertThrows(RmesBadRequestException.class, () -> structureRepository.publishStructure(jsonObject));
        Assertions.assertEquals(
                ("{\"code\":1004,\"details\":\"[]\",\"message\":\"The creator should not be empty\"}"),
                exception.getDetails());
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishStructureWhenCreatorNull() {
        RmesException exception = assertThrows(
                RmesBadRequestException.class, () -> structureRepository.publishStructure(new JSONObject()));
        Assertions.assertEquals(
                ("{\"code\":1004,\"details\":\"[]\",\"message\":\"The creator should not be empty\"}"),
                exception.getDetails());
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishStructureWhenDisseminationStatusNull() {
        JSONObject jsonObject = new JSONObject().put(Constants.CREATOR, "creatorExample");
        RmesException exception =
                assertThrows(RmesBadRequestException.class, () -> structureRepository.publishStructure(jsonObject));
        Assertions.assertEquals(
                "{\"code\":1005,\"details\":\"[]\",\"message\":\"The dissemination status should not be empty\"}",
                exception.getDetails());
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishStructureWhenDisseminationStatusIsEmpty() {
        JSONObject jsonObject =
                new JSONObject().put(Constants.CREATOR, "creatorExample").put("disseminationStatus", "");
        RmesException exception =
                assertThrows(RmesBadRequestException.class, () -> structureRepository.publishStructure(jsonObject));
        Assertions.assertEquals(
                "{\"code\":1005,\"details\":\"[]\",\"message\":\"The dissemination status should not be empty\"}",
                exception.getDetails());
    }

    @Test
    void shouldThrowRmesExceptionWhenSetStructureWithIdAndBody() {
        RmesException exception = assertThrows(
                RmesException.class, () -> structureRepository.setStructure("idExample", fakeJsonObjectBody));
        Assertions.assertTrue(exception.getDetails().contains("{\"details\":\"IOException\""));
    }

    @Test
    void shouldStoreCreatorAndContributorAsUriForStructure() throws RmesException {
        IRI structureIri = SimpleValueFactory.getInstance().createIRI("http://bauhaus/structuresDSD/dsd1000");
        Resource graph = SimpleValueFactory.getInstance().createIRI("http://rdf.insee.fr/graphes/structures");

        try (MockedStatic<RdfUtils> rdfUtilsMock = mockStatic(RdfUtils.class)) {
            rdfUtilsMock.when(() -> RdfUtils.toURI(anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(anyString())).thenCallRealMethod();
            rdfUtilsMock
                    .when(() -> RdfUtils.setLiteralString(anyString(), anyString()))
                    .thenCallRealMethod();
            rdfUtilsMock
                    .when(() -> RdfUtils.setLiteralString(any(ValidationStatus.class)))
                    .thenCallRealMethod();
            rdfUtilsMock
                    .when(() -> RdfUtils.addTripleString(
                            any(IRI.class), any(IRI.class), anyString(), any(Model.class), any(Resource.class)))
                    .thenCallRealMethod();
            rdfUtilsMock
                    .when(() -> RdfUtils.addTripleString(
                            any(IRI.class),
                            any(IRI.class),
                            anyString(),
                            anyString(),
                            any(Model.class),
                            any(Resource.class)))
                    .thenCallRealMethod();
            rdfUtilsMock
                    .when(() -> RdfUtils.addTripleDateTime(
                            any(IRI.class), any(IRI.class), anyString(), any(Model.class), any(Resource.class)))
                    .thenCallRealMethod();
            rdfUtilsMock
                    .when(() -> RdfUtils.addTripleUri(
                            any(IRI.class), any(IRI.class), any(IRI.class), any(Model.class), any(Resource.class)))
                    .thenCallRealMethod();
            rdfUtilsMock
                    .when(() -> RdfUtils.addTripleUri(
                            any(Resource.class), any(IRI.class), anyString(), any(Model.class), any(Resource.class)))
                    .thenCallRealMethod();

            Structure structure = new Structure("dsd1000");
            structure.setIdentifiant("identifiant");
            structure.setLabelLg1("label fr");
            structure.setLabelLg2("label en");
            structure.setCreator("http://creator-uri");
            structure.setContributor(List.of("http://contributor-uri"));
            structure.setComponentDefinitions(List.of());

            structureRepository.createRdfStructure(
                    structure, "dsd1000", structureIri, graph, ValidationStatus.UNPUBLISHED);

            ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
            verify(repositoryGestion).loadSimpleObject(eq(structureIri), modelCaptor.capture(), isNull());

            Model model = modelCaptor.getValue();
            IRI expectedCreatorIri = SimpleValueFactory.getInstance().createIRI("http://creator-uri");
            IRI expectedContributorIri = SimpleValueFactory.getInstance().createIRI("http://contributor-uri");

            assertThat(model)
                    .anyMatch(stmt -> stmt.getPredicate().equals(DC.CREATOR)
                            && stmt.getObject().equals(expectedCreatorIri));
            assertThat(model)
                    .anyMatch(stmt -> stmt.getPredicate().equals(DC.CONTRIBUTOR)
                            && stmt.getObject().equals(expectedContributorIri));
        }
    }

    @Test
    void shouldAttachTheComponentsOfEachStructureReturnedBySearch() throws RmesException {
        when(structureQueries.getComponentsForStructure("dsd1000")).thenReturn("components-query");
        when(repositoryGestion.getResponseAsArray("components-query"))
                .thenReturn(new JSONArray().put(new JSONObject().put(Constants.ID, "cs1000")));

        JSONArray formatted = structureRepository.formatStructuresForSearch(
                new JSONArray().put(new JSONObject().put(Constants.ID, "dsd1000")));

        assertThat(formatted.getJSONObject(0).getJSONArray("components").length())
                .isEqualTo(1);
    }

    /**
     * La requête renvoie une ligne plate par définition de composant : le service en extrait les
     * propriétés de la définition et laisse au composant lui-même ce qui reste.
     */
    @Test
    void shouldNestTheFlatComponentDefinitionsUnderTheirOwnProperties() throws RmesException {
        JSONObject flat = new JSONObject()
                .put(StructureRepository.COMPONENT_DEFINITION_ID, "cs1000")
                .put(StructureRepository.REQUIRED, "true")
                .put(StructureRepository.ORDER, "1")
                .put(StructureRepository.COMPONENT_DEFINITION_CREATED, "2026-01-01T10:00:00")
                .put(StructureRepository.COMPONENT_DEFINITION_MODIFIED, "2026-02-01T10:00:00")
                .put(StructureRepository.NOTATION, "notation")
                .put(StructureRepository.LABEL_LG1, "label fr")
                .put(StructureRepository.LABEL_LG2, "label en")
                .put("component", "http://component");
        when(structureQueries.getComponentsForStructure("dsd1000")).thenReturn("components-query");
        when(repositoryGestion.getResponseAsArray("components-query")).thenReturn(new JSONArray().put(flat));
        when(structureQueries.getStructuresAttachments("dsd1000", "cs1000")).thenReturn("attachments-query");
        when(repositoryGestion.getResponseAsArray("attachments-query"))
                .thenReturn(
                        new JSONArray().put(new JSONObject().put(StructureRepository.ATTACHMENT, "http://attachment")));

        JSONObject formatted = structureRepository.formatStructure(new JSONObject(), "dsd1000");

        assertThat(formatted.getString(Constants.ID)).isEqualTo("dsd1000");
        JSONObject definition = formatted.getJSONArray("componentDefinitions").getJSONObject(0);
        assertThat(definition.getString(Constants.ID)).isEqualTo("cs1000");
        assertThat(definition.getBoolean(StructureRepository.REQUIRED)).isTrue();
        assertThat(definition.getString(StructureRepository.ORDER)).isEqualTo("1");
        assertThat(definition.getString("created")).isEqualTo("2026-01-01T10:00:00");
        assertThat(definition.getString("modified")).isEqualTo("2026-02-01T10:00:00");
        assertThat(definition.getString(StructureRepository.NOTATION)).isEqualTo("notation");
        assertThat(definition.getString("labelLg1")).isEqualTo("label fr");
        assertThat(definition.getString("labelLg2")).isEqualTo("label en");
        assertThat(definition.getJSONArray(StructureRepository.ATTACHMENT).getString(0))
                .isEqualTo("http://attachment");
        assertThat(definition.getJSONObject("component").has("component")).isFalse();
    }

    @Test
    void shouldKeepAnUpdatedStructureUnpublishedWhenItWasNotPublishedYet() throws RmesException {
        givenStructureDsd1000WithStatus(ValidationStatus.UNPUBLISHED);

        String id = structureRepository.setStructure("dsd1000", structureBody());

        assertThat(id).isEqualTo("dsd1000");
        assertThat(validationStateOfStoredStructure()).isEqualTo(ValidationStatus.UNPUBLISHED.getValue());
    }

    @Test
    void shouldFlagAnUpdatedStructureAsModifiedWhenItIsAlreadyPublished() throws RmesException {
        givenStructureDsd1000WithStatus(ValidationStatus.VALIDATED);

        structureRepository.setStructure("dsd1000", structureBody());

        assertThat(validationStateOfStoredStructure()).isEqualTo(ValidationStatus.MODIFIED.getValue());
    }

    /**
     * Deux structures ne peuvent pas porter exactement les mêmes composants. Le contrôle n'a de
     * sens que si tous les composants sont déjà identifiés : un composant sans id est en cours de
     * création, donc la structure ne peut pas encore être un doublon.
     */
    @Test
    void shouldRejectAStructureBuiltOnTheSameComponentsAsAnotherOne() throws RmesException {
        when(structureQueries.checkUnicityStructure(eq("dsd1000"), any(String[].class)))
                .thenReturn("unicity-query");
        when(repositoryGestion.getResponseAsBoolean("unicity-query")).thenReturn(true);

        RmesException exception = assertThrows(
                RmesBadRequestException.class,
                () -> structureRepository.setStructure("dsd1000", structureBodyWithComponent("d1000")));

        assertThat(exception.getDetails()).contains("A structure with the same components already exists");
    }

    @Test
    void shouldSkipTheUnicityCheckWhenAComponentOfTheStructureIsStillToBeCreated() throws RmesException {
        givenStructureDsd1000WithStatus(ValidationStatus.UNPUBLISHED);
        when(structureComponentRepository.createComponent(any(MutualizedComponent.class), any(JSONObject.class)))
                .thenReturn("d2000");

        structureRepository.setStructure("dsd1000", structureBodyWithComponent(null));

        verify(structureQueries, never()).checkUnicityStructure(anyString(), any(String[].class));
    }

    @Test
    void shouldStoreADimensionSpecificationPointingToItsDimension() throws RmesException {
        Model model = storedSpecificationFor(componentDefinition(QB.DIMENSION_PROPERTY.stringValue(), "d1000"));

        assertThat(model).anyMatch(statement -> statement.getPredicate().equals(QB.DIMENSION));
        assertThat(objectOf(model, DCTERMS.IDENTIFIER)).isEqualTo("cs1000");
        assertThat(objectOf(model, SKOS.NOTATION)).isEqualTo("notation");
        assertThat(objectOf(model, QB.ORDER)).isEqualTo("1");
    }

    @Test
    void shouldStoreAnAttributeSpecificationWithItsAttachmentsAndItsRequiredFlag() throws RmesException {
        ComponentDefinition componentDefinition = componentDefinition(QB.ATTRIBUTE_PROPERTY.stringValue(), "a1000");
        componentDefinition.setAttachment(new String[] {"http://attachment", "m1000"});

        Model model = storedSpecificationFor(componentDefinition);

        assertThat(model).anyMatch(statement -> statement.getPredicate().equals(QB.ATTRIBUTE));
        assertThat(model.filter(null, QB.COMPONENT_ATTACHMENT, null)).hasSize(2);
        assertThat(objectOf(model, QB.COMPONENT_REQUIRED)).isEqualTo("true");
    }

    @Test
    void shouldStoreAMeasureSpecificationPointingToItsMeasure() throws RmesException {
        Model model = storedSpecificationFor(componentDefinition(QB.MEASURE_PROPERTY.stringValue(), "m1000"));

        assertThat(model).anyMatch(statement -> statement.getPredicate().equals(QB.MEASURE));
    }

    @Test
    void shouldRejectThePublicationOfAStructureWhoseComponentCannotBePublished() throws RmesException {
        JSONObject structure = new JSONObject()
                .put(Constants.ID, "dsd1000")
                .put(Constants.CREATOR, "http://creator")
                .put("disseminationStatus", "http://status");
        when(structureQueries.getUnValidatedComponent("dsd1000")).thenReturn("unvalidated-query");
        when(repositoryGestion.getResponseAsArray("unvalidated-query"))
                .thenReturn(new JSONArray().put(new JSONObject().put(Constants.ID, "d1000")));
        doThrow(new RmesBadRequestException("nope")).when(structureComponent).publishComponent("d1000");

        RmesException exception =
                assertThrows(RmesUnauthorizedException.class, () -> structureRepository.publishStructure(structure));

        assertThat(exception.getDetails()).contains("The component d1000 component can not be published");
    }

    private static String structureBody() {
        return new JSONObject()
                .put("identifiant", "identifiant")
                .put("labelLg1", "label fr")
                .put("labelLg2", "label en")
                .put("contributor", new JSONArray())
                .put("componentDefinitions", new JSONArray())
                .toString();
    }

    private static String structureBodyWithComponent(String componentId) {
        JSONObject component = new JSONObject()
                .put("identifiant", "identifiant")
                .put("labelLg1", "label fr")
                .put("labelLg2", "label en")
                .put("type", QB.DIMENSION_PROPERTY.stringValue());
        if (componentId != null) {
            component.put(Constants.ID, componentId);
        }
        return new JSONObject(structureBody())
                .put("componentDefinitions", new JSONArray().put(new JSONObject().put("component", component)))
                .toString();
    }

    private static ComponentDefinition componentDefinition(String type, String componentId) throws RmesException {
        MutualizedComponent component = new MutualizedComponent();
        component.setId(componentId);
        component.setType(type);

        ComponentDefinition componentDefinition = new ComponentDefinition();
        componentDefinition.setId("cs1000");
        componentDefinition.setCreated("2026-01-01T10:00:00");
        componentDefinition.setModified("2026-02-01T10:00:00");
        componentDefinition.setOrder("1");
        componentDefinition.setNotation("notation");
        componentDefinition.setLabelLg1("label fr");
        componentDefinition.setLabelLg2("label en");
        componentDefinition.setRequired(true);
        componentDefinition.setComponent(component);
        return componentDefinition;
    }

    private Model storedSpecificationFor(ComponentDefinition componentDefinition) throws RmesException {
        IRI structureIri = SimpleValueFactory.getInstance().createIRI("http://bauhaus/structuresDSD/dsd1000");
        Resource graph = SimpleValueFactory.getInstance().createIRI("http://rdf.insee.fr/graphes/structures");

        structureRepository.createRdfComponentSpecification(structureIri, componentDefinition, graph);

        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(repositoryGestion).loadSimpleObject(any(IRI.class), modelCaptor.capture());
        return modelCaptor.getValue();
    }

    private void givenStructureDsd1000WithStatus(ValidationStatus status) throws RmesException {
        when(structureQueries.getValidationStatus("dsd1000")).thenReturn("validation-status-query");
        when(repositoryGestion.getResponseAsObject("validation-status-query"))
                .thenReturn(new JSONObject().put("state", status.getValue()));
    }

    private String validationStateOfStoredStructure() throws RmesException {
        return objectOf(storedModel(repositoryGestion), INSEE.VALIDATION_STATE);
    }
}
