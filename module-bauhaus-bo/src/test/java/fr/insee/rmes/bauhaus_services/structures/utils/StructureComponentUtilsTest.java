package fr.insee.rmes.bauhaus_services.structures.utils;

import fr.insee.rmes.Config;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.QB;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.modules.structures.components.domain.model.MutualizedComponent;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static fr.insee.rmes.bauhaus_services.structures.utils.StructureComponentUtils.MODIFIED;
import static fr.insee.rmes.bauhaus_services.structures.utils.StructureComponentUtils.VALIDATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StructureComponentUtilsTest {

    @InjectMocks
    StructureComponentUtils structureComponentUtils;

    @Mock
    RepositoryGestion repoGestion;

    @Mock
    Config config;

    @Mock
    ComponentPublication componentPublication;

    @Test
    void shouldThrowRmesExceptionWhenUpdateComponent() {
        RmesException exception = assertThrows(RmesBadRequestException.class, () ->  structureComponentUtils.updateComponent("componentId","{\"id\":\"idExample\",\"creator\":\"creatorExample\"}"));
        assertThat(exception.getDetails()).contains("The id of the component should be the same as the one defined in the request");
    }

    @Test
    void shouldThrowRmesExceptionWhenValidateComponentWithoutIdentifiant() {
        RmesException exception = assertThrows(RmesBadRequestException.class, () ->  structureComponentUtils.updateComponent("idExample","{\"id\":\"idExample\",\"creator\":\"creatorExample\"}"));
        assertThat(exception.getDetails()).contains("The property identifiant is required");
    }

    @Test
    void shouldThrowRmesExceptionWhenValidateComponentWithoutLabelLg1() {
        RmesException exception = assertThrows(RmesBadRequestException.class, () ->  structureComponentUtils.updateComponent("idExample","{\"id\":\"idExample\",\"identifiant\":\"identifiantExample\",\"creator\":\"creatorExample\"}"));
        assertThat(exception.getDetails()).contains("{\"message\":\"The property labelLg1 is required\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenValidateComponentWithoutLabelLg2() {
        RmesException exception = assertThrows(RmesBadRequestException.class, () ->  structureComponentUtils.updateComponent("idExample","{\"id\":\"idExample\",\"identifiant\":\"identifiantExample\",\"labelLg1\":\"labelLg1Example\"}"));
        assertThat(exception.getDetails()).contains("{\"message\":\"The property labelLg2 is required\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenValidateComponentWithoutType() {
        RmesException exception = assertThrows(RmesBadRequestException.class, () ->  structureComponentUtils.updateComponent("idExample","{\"id\":\"idExample\",\"identifiant\":\"identifiantExample\",\"labelLg1\":\"labelLg1Example\",\"labelLg2\":\"labelLg2Example\"}"));
        assertThat(exception.getDetails()).contains("{\"message\":\"The property type is required\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenValidateComponentWithInvalidateType() {
        RmesException exception = assertThrows(RmesBadRequestException.class, () ->  structureComponentUtils.updateComponent("idExample","{\"id\":\"idExample\",\"identifiant\":\"identifiantExample\",\"labelLg1\":\"labelLg1Example\",\"labelLg2\":\"labelLg2Example\",\"type\":\"typeExample\"}"));
        assertThat(exception.getDetails()).contains("{\"message\":\"The property type is not valid\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenCreateComponent() {
        RmesException exception = assertThrows(RmesBadRequestException.class, () -> structureComponentUtils.createComponent("{\"id\":\"idExample\",\"identifiant\":\"identifiantExample\",\"labelLg1\":\"labelLg1Example\",\"labelLg2\":\"labelLg2Example\",\"type\":\"typeExample\"}"));
        assertThat(exception.getDetails()).contains("{\"message\":\"During the creation of a new component, the id property should be null\"}");
    }

    @ParameterizedTest
    @ValueSource(strings = { VALIDATED, MODIFIED })
    void shouldThrowRmesExceptionWhenDeleteComponent(String value) {
        JSONObject component = new JSONObject().put("validationState",value);
        RmesException exception = assertThrows(RmesException.class, () -> structureComponentUtils.deleteComponent(component,"id","type"));
        assertThat(exception.getDetails()).contains("{\"details\":\"[]\",\"message\":\"You cannot delete a validated component\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenDeleteComponentWithInvalidStructures() {
        JSONObject exampleOne = new JSONObject().put("validationState",VALIDATED);
        JSONObject exampleTwo = new JSONObject().put("validationState","example");
        JSONArray structures = new JSONArray().put(exampleOne).put(exampleTwo);
        JSONObject component = new JSONObject().put("validationState","value").put("structures",structures);

        RmesException exception = assertThrows(RmesException.class, () -> structureComponentUtils.deleteComponent(component,"id","type"));
        assertThat(exception.getDetails()).contains("{\"details\":\"[]\",\"message\":\"You cannot delete a validated component\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishComponentWithInvalidCreator() {
        JSONObject component = new JSONObject().put(Constants.CREATOR,"");
        RmesException exception = assertThrows(RmesException.class, () -> structureComponentUtils.publishComponent(component));
        assertThat(exception.getDetails()).contains("{\"code\":1004,\"details\":\"[]\",\"message\":\"The creator should not be empty\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishComponentWithInvalidDisseminationStatus() {
        JSONObject component = new JSONObject().put(Constants.CREATOR,"creatorExample").put("disseminationStatus","");
        RmesException exception = assertThrows(RmesException.class, () -> structureComponentUtils.publishComponent(component));
        assertThat(exception.getDetails()).contains("{\"code\":1005,\"details\":\"[]\",\"message\":\"The dissemination status should not be empty\"}");
    }

    @Test
    void shouldStoreCreatorAndContributorAsUriForComponent() throws RmesException {
        IRI componentIri = SimpleValueFactory.getInstance().createIRI("http://bauhaus/structureComponent/dimension/d1000");
        IRI graphIri = SimpleValueFactory.getInstance().createIRI("http://rdf.insee.fr/graphes/structures/components");
        org.eclipse.rdf4j.model.Literal fakeDateLiteral = SimpleValueFactory.getInstance().createLiteral("2024-01-15T10:00:00");

        try (MockedStatic<RdfUtils> rdfUtilsMock = mockStatic(RdfUtils.class)) {
            rdfUtilsMock.when(() -> RdfUtils.structureComponentDimensionIRI("d1000")).thenReturn(componentIri);
            rdfUtilsMock.when(RdfUtils::structureComponentGraph).thenReturn(graphIri);
            rdfUtilsMock.when(() -> RdfUtils.toString(any(IRI.class))).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.toURI(anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(anyString(), anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(any(ValidationStatus.class))).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralDateTime(anyString())).thenReturn(fakeDateLiteral);
            rdfUtilsMock.when(() -> RdfUtils.addTripleString(any(IRI.class), any(IRI.class), anyString(), any(Model.class), any(Resource.class))).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleString(any(IRI.class), any(IRI.class), anyString(), anyString(), any(Model.class), any(Resource.class))).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleUri(any(IRI.class), any(IRI.class), any(IRI.class), any(Model.class), any(Resource.class))).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleUri(any(Resource.class), any(IRI.class), anyString(), any(Model.class), any(Resource.class))).thenCallRealMethod();

            when(config.getLg1()).thenReturn("fr");
            when(config.getLg2()).thenReturn("en");

            MutualizedComponent component = new MutualizedComponent();
            component.setIdentifiant("identifiant");
            component.setLabelLg1("label fr");
            component.setLabelLg2("label en");
            component.setType(QB.DIMENSION_PROPERTY.toString());
            component.setCreator("http://creator-uri");
            component.setContributor(List.of("http://contributor-uri"));

            structureComponentUtils.createComponent(component, "d1000", new JSONObject());

            ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
            verify(repoGestion).loadSimpleObject(eq(componentIri), modelCaptor.capture(), isNull());

            Model model = modelCaptor.getValue();
            IRI expectedCreatorIri = SimpleValueFactory.getInstance().createIRI("http://creator-uri");
            IRI expectedContributorIri = SimpleValueFactory.getInstance().createIRI("http://contributor-uri");

            assertThat(model).anyMatch(stmt ->
                stmt.getPredicate().equals(DC.CREATOR) && stmt.getObject().equals(expectedCreatorIri)
            );
            assertThat(model).anyMatch(stmt ->
                stmt.getPredicate().equals(DC.CONTRIBUTOR) && stmt.getObject().equals(expectedContributorIri)
            );
        }
    }

    @Test
    void shouldNotStoreNullCreatorInModel() throws RmesException {
        IRI componentIri = SimpleValueFactory.getInstance().createIRI("http://bauhaus/structureComponent/dimension/d1000");
        IRI graphIri = SimpleValueFactory.getInstance().createIRI("http://rdf.insee.fr/graphes/structures/components");
        org.eclipse.rdf4j.model.Literal fakeDateLiteral = SimpleValueFactory.getInstance().createLiteral("2024-01-15T10:00:00");

        try (MockedStatic<RdfUtils> rdfUtilsMock = mockStatic(RdfUtils.class)) {
            rdfUtilsMock.when(() -> RdfUtils.structureComponentDimensionIRI("d1000")).thenReturn(componentIri);
            rdfUtilsMock.when(RdfUtils::structureComponentGraph).thenReturn(graphIri);
            rdfUtilsMock.when(() -> RdfUtils.toString(any(IRI.class))).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.toURI(anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(anyString(), anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(any(ValidationStatus.class))).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralDateTime(anyString())).thenReturn(fakeDateLiteral);
            rdfUtilsMock.when(() -> RdfUtils.addTripleString(any(IRI.class), any(IRI.class), anyString(), any(Model.class), any(Resource.class))).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleString(any(IRI.class), any(IRI.class), anyString(), anyString(), any(Model.class), any(Resource.class))).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleUri(any(IRI.class), any(IRI.class), any(IRI.class), any(Model.class), any(Resource.class))).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleUri(any(Resource.class), any(IRI.class), anyString(), any(Model.class), any(Resource.class))).thenCallRealMethod();

            when(config.getLg1()).thenReturn("fr");
            when(config.getLg2()).thenReturn("en");

            MutualizedComponent component = new MutualizedComponent();
            component.setIdentifiant("identifiant");
            component.setLabelLg1("label fr");
            component.setLabelLg2("label en");
            component.setType(QB.DIMENSION_PROPERTY.toString());
            component.setContributor(List.of());

            structureComponentUtils.createComponent(component, "d1000", new JSONObject());

            ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
            verify(repoGestion).loadSimpleObject(eq(componentIri), modelCaptor.capture(), isNull());

            Model model = modelCaptor.getValue();
            assertThat(model).noneMatch(stmt -> stmt.getPredicate().equals(DC.CREATOR));
        }
    }

}




