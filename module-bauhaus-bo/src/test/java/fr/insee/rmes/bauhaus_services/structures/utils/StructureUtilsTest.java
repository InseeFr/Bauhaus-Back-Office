package fr.insee.rmes.bauhaus_services.structures.utils;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.Config;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.modules.structures.structures.domain.model.Structure;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.structures.infrastructure.graphdb.StructureQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@AppSpringBootTest
class StructureUtilsTest {
    @InjectMocks
    StructureUtils structureUtils = new StructureUtils();

    @MockitoBean
    RepositoryGestion repositoryGestion;

    @Mock
    StructureQueries structureQueries;

    @Autowired
    Config config;

    public static final String VALIDATION_STATUS = "{\"state\":\"Published\"}";
    public String fakeJsonObjectBody = "This a fake body of JsonObject";

    @Test
    void shouldReturnBadRequestExceptionIfPublishedStructure() throws RmesException {
        JSONObject mockJSON = new JSONObject(VALIDATION_STATUS);
        when(structureQueries.getValidationStatus(anyString())).thenReturn("validation-status-query");
        Structure structure = new Structure();
        structure.setId("id");
        when(repositoryGestion.getResponseAsObject(Mockito.anyString())).thenReturn(mockJSON);
        RmesException exception = assertThrows(RmesBadRequestException.class, () -> structureUtils.deleteStructure("id"));
        Assertions.assertEquals("{\"code\":1103,\"message\":\"Only unpublished codelist can be deleted\"}", exception.getDetails());
    }

    @Test
    void shouldThrowRmesExceptionWhenSetStructure()  {
        RmesException exception = assertThrows(RmesException.class, () -> structureUtils.setStructure(fakeJsonObjectBody));
        Assertions.assertTrue( exception.getDetails().contains("{\"details\":\"IOException\",\"message\":\"Unrecognized token"));
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishStructureWhenCreatorEmpty()  {
        JSONObject jsonObject = new JSONObject().put(Constants.CREATOR,"");
        RmesException exception = assertThrows(RmesBadRequestException.class, () -> structureUtils.publishStructure(jsonObject));
        Assertions.assertEquals(("{\"code\":1004,\"details\":\"[]\",\"message\":\"The creator should not be empty\"}"), exception.getDetails());
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishStructureWhenCreatorNull()  {
        RmesException exception = assertThrows(RmesBadRequestException.class, () -> structureUtils.publishStructure(new JSONObject()));
        Assertions.assertEquals(("{\"code\":1004,\"details\":\"[]\",\"message\":\"The creator should not be empty\"}"), exception.getDetails());
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishStructureWhenDisseminationStatusNull()  {
        JSONObject jsonObject = new JSONObject().put(Constants.CREATOR,"creatorExample");
        RmesException exception = assertThrows(RmesBadRequestException.class, () -> structureUtils.publishStructure(jsonObject));
        Assertions.assertEquals("{\"code\":1005,\"details\":\"[]\",\"message\":\"The dissemination status should not be empty\"}",exception.getDetails());
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishStructureWhenDisseminationStatusIsEmpty()  {
        JSONObject jsonObject = new JSONObject().put(Constants.CREATOR,"creatorExample").put("disseminationStatus","");
        RmesException exception = assertThrows(RmesBadRequestException.class, () -> structureUtils.publishStructure(jsonObject));
        Assertions.assertEquals("{\"code\":1005,\"details\":\"[]\",\"message\":\"The dissemination status should not be empty\"}",exception.getDetails());
    }

    @Test
    void shouldThrowRmesExceptionWhenSetStructureWithIdAndBody()  {
       RmesException exception = assertThrows(RmesException.class, () -> structureUtils.setStructure("idExample",fakeJsonObjectBody));
       Assertions.assertTrue(exception.getDetails().contains("{\"details\":\"IOException\""));
    }

    @Test
    void shouldStoreCreatorAndContributorAsUriForStructure() throws RmesException {
        IRI structureIri = SimpleValueFactory.getInstance().createIRI("http://bauhaus/structuresDSD/dsd1000");
        Resource graph = SimpleValueFactory.getInstance().createIRI("http://rdf.insee.fr/graphes/structures");

        try (MockedStatic<RdfUtils> rdfUtilsMock = mockStatic(RdfUtils.class)) {
            rdfUtilsMock.when(() -> RdfUtils.toURI(anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(anyString(), anyString())).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.setLiteralString(any(ValidationStatus.class))).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleString(any(IRI.class), any(IRI.class), anyString(), any(Model.class), any(Resource.class))).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleString(any(IRI.class), any(IRI.class), anyString(), anyString(), any(Model.class), any(Resource.class))).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleDateTime(any(IRI.class), any(IRI.class), anyString(), any(Model.class), any(Resource.class))).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleUri(any(IRI.class), any(IRI.class), any(IRI.class), any(Model.class), any(Resource.class))).thenCallRealMethod();
            rdfUtilsMock.when(() -> RdfUtils.addTripleUri(any(Resource.class), any(IRI.class), anyString(), any(Model.class), any(Resource.class))).thenCallRealMethod();

            ReflectionTestUtils.setField(structureUtils, "config", config);

            Structure structure = new Structure("dsd1000");
            structure.setIdentifiant("identifiant");
            structure.setLabelLg1("label fr");
            structure.setLabelLg2("label en");
            structure.setCreator("http://creator-uri");
            structure.setContributor(List.of("http://contributor-uri"));
            structure.setComponentDefinitions(List.of());

            structureUtils.createRdfStructure(structure, "dsd1000", structureIri, graph, ValidationStatus.UNPUBLISHED);

            ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
            verify(repositoryGestion).loadSimpleObject(eq(structureIri), modelCaptor.capture(), isNull());

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

}
