package fr.insee.rmes.bauhaus_services.structures.persistence;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausIriFactory;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.QB;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static fr.insee.rmes.bauhaus_services.structures.persistence.StructureComponentRepository.MODIFIED;
import static fr.insee.rmes.bauhaus_services.structures.persistence.StructureComponentRepository.VALIDATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StructureComponentRepositoryTest {

    private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();

    @InjectMocks
    StructureComponentRepository structureComponentRepository;

    @Spy
    BauhausLanguagesProperties languages = new BauhausLanguagesProperties("fr", "en");

    @Mock
    RepositoryGestion repoGestion;


    @Mock
    ComponentPublication componentPublication;

    /**
     * Fabrique réelle, et non bouchon : les IRI des composants et le graphe de destination font
     * partie de ce que ce test vérifie. Le {@code @Spy} n'est là que pour qu'elle soit injectée
     * dans {@code @InjectMocks} comme les autres collaborateurs.
     */
    @Spy
    BauhausIriFactory iriFactory = new BauhausIriFactory(
            GraphsPropertiesStub.stub(),
            new BauhausUriBuilder("http://publication/", "http://bauhaus/", name -> Optional.of("composants/")));

    @Test
    void shouldThrowRmesExceptionWhenUpdateComponent() {
        RmesException exception = assertThrows(RmesBadRequestException.class, () ->  structureComponentRepository.updateComponent("componentId","{\"id\":\"idExample\",\"creator\":\"creatorExample\"}"));
        assertThat(exception.getDetails()).contains("The id of the component should be the same as the one defined in the request");
    }

    @Test
    void shouldThrowRmesExceptionWhenValidateComponentWithoutIdentifiant() {
        RmesException exception = assertThrows(RmesBadRequestException.class, () ->  structureComponentRepository.updateComponent("idExample","{\"id\":\"idExample\",\"creator\":\"creatorExample\"}"));
        assertThat(exception.getDetails()).contains("The property identifiant is required");
    }

    @Test
    void shouldThrowRmesExceptionWhenValidateComponentWithoutLabelLg1() {
        RmesException exception = assertThrows(RmesBadRequestException.class, () ->  structureComponentRepository.updateComponent("idExample","{\"id\":\"idExample\",\"identifiant\":\"identifiantExample\",\"creator\":\"creatorExample\"}"));
        assertThat(exception.getDetails()).contains("{\"message\":\"The property labelLg1 is required\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenValidateComponentWithoutLabelLg2() {
        RmesException exception = assertThrows(RmesBadRequestException.class, () ->  structureComponentRepository.updateComponent("idExample","{\"id\":\"idExample\",\"identifiant\":\"identifiantExample\",\"labelLg1\":\"labelLg1Example\"}"));
        assertThat(exception.getDetails()).contains("{\"message\":\"The property labelLg2 is required\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenValidateComponentWithoutType() {
        RmesException exception = assertThrows(RmesBadRequestException.class, () ->  structureComponentRepository.updateComponent("idExample","{\"id\":\"idExample\",\"identifiant\":\"identifiantExample\",\"labelLg1\":\"labelLg1Example\",\"labelLg2\":\"labelLg2Example\"}"));
        assertThat(exception.getDetails()).contains("{\"message\":\"The property type is required\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenValidateComponentWithInvalidateType() {
        RmesException exception = assertThrows(RmesBadRequestException.class, () ->  structureComponentRepository.updateComponent("idExample","{\"id\":\"idExample\",\"identifiant\":\"identifiantExample\",\"labelLg1\":\"labelLg1Example\",\"labelLg2\":\"labelLg2Example\",\"type\":\"typeExample\"}"));
        assertThat(exception.getDetails()).contains("{\"message\":\"The property type is not valid\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenCreateComponent() {
        RmesException exception = assertThrows(RmesBadRequestException.class, () -> structureComponentRepository.createComponent("{\"id\":\"idExample\",\"identifiant\":\"identifiantExample\",\"labelLg1\":\"labelLg1Example\",\"labelLg2\":\"labelLg2Example\",\"type\":\"typeExample\"}"));
        assertThat(exception.getDetails()).contains("{\"message\":\"During the creation of a new component, the id property should be null\"}");
    }

    @ParameterizedTest
    @ValueSource(strings = { VALIDATED, MODIFIED })
    void shouldThrowRmesExceptionWhenDeleteComponent(String value) {
        JSONObject component = new JSONObject().put("validationState",value);
        RmesException exception = assertThrows(RmesException.class, () -> structureComponentRepository.deleteComponent(component,"id","type"));
        assertThat(exception.getDetails()).contains("{\"details\":\"[]\",\"message\":\"You cannot delete a validated component\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenDeleteComponentWithInvalidStructures() {
        JSONObject exampleOne = new JSONObject().put("validationState",VALIDATED);
        JSONObject exampleTwo = new JSONObject().put("validationState","example");
        JSONArray structures = new JSONArray().put(exampleOne).put(exampleTwo);
        JSONObject component = new JSONObject().put("validationState","value").put("structures",structures);

        RmesException exception = assertThrows(RmesException.class, () -> structureComponentRepository.deleteComponent(component,"id","type"));
        assertThat(exception.getDetails()).contains("{\"details\":\"[]\",\"message\":\"You cannot delete a validated component\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishingAnAlreadyPublishedComponent() {
        JSONObject component = new JSONObject()
                .put(Constants.ID, "c1000")
                .put(Constants.CREATOR, "creatorExample")
                .put("disseminationStatus", "http://status")
                .put("validationState", "Validated");

        RmesException exception = assertThrows(RmesBadRequestException.class, () -> structureComponentRepository.publishComponent(component));

        assertThat(exception.getDetails()).contains("\"code\":1301");
        assertThat(exception.getDetails()).contains("This component is already published");
        assertThat(exception.getDetails()).contains("Component: c1000");
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishComponentWithInvalidCreator() {
        JSONObject component = new JSONObject().put(Constants.CREATOR,"");
        RmesException exception = assertThrows(RmesException.class, () -> structureComponentRepository.publishComponent(component));
        assertThat(exception.getDetails()).contains("{\"code\":1004,\"details\":\"[]\",\"message\":\"The creator should not be empty\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishComponentWithInvalidDisseminationStatus() {
        JSONObject component = new JSONObject().put(Constants.CREATOR,"creatorExample").put("disseminationStatus","");
        RmesException exception = assertThrows(RmesException.class, () -> structureComponentRepository.publishComponent(component));
        assertThat(exception.getDetails()).contains("{\"code\":1005,\"details\":\"[]\",\"message\":\"The dissemination status should not be empty\"}");
    }

    @Test
    void shouldStoreCreatorAndContributorAsUriForComponent() throws RmesException {
        MutualizedComponent component = dimension();
        component.setCreator("http://creator-uri");
        component.setContributor(List.of("http://contributor-uri"));

        structureComponentRepository.createComponent(component, "d1000", new JSONObject());

        assertThat(storedModel()).anyMatch(stmt ->
                stmt.getPredicate().equals(DC.CREATOR)
                        && stmt.getObject().equals(VF.createIRI("http://creator-uri")));
        assertThat(storedModel()).anyMatch(stmt ->
                stmt.getPredicate().equals(DC.CONTRIBUTOR)
                        && stmt.getObject().equals(VF.createIRI("http://contributor-uri")));
    }

    @Test
    void shouldNotStoreNullCreatorInModel() throws RmesException {
        MutualizedComponent component = dimension();
        component.setContributor(List.of());

        structureComponentRepository.createComponent(component, "d1000", new JSONObject());

        assertThat(storedModel()).noneMatch(stmt -> stmt.getPredicate().equals(DC.CREATOR));
    }

    /**
     * L'IRI du composant et le graphe qui l'accueille se déduisent de la configuration : ce test
     * vérifie qu'ils sont bien portés par les triplets écrits, et non seulement par l'appel au
     * dépôt.
     */
    @Test
    void shouldStoreTheComponentInTheComponentsGraphUnderItsTypedIri() throws RmesException {
        MutualizedComponent component = dimension();
        component.setContributor(List.of());

        structureComponentRepository.createComponent(component, "d1000", new JSONObject());

        IRI expectedIri = VF.createIRI("http://bauhaus/composants/dimension/d1000");
        Resource expectedGraph = VF.createIRI("http://rdf.insee.fr/graphes/composants");

        verify(repoGestion).loadSimpleObject(eq(expectedIri), any(Model.class), isNull());
        assertThat(storedModel()).allMatch(stmt ->
                stmt.getSubject().equals(expectedIri) && expectedGraph.equals(stmt.getContext()));
    }

    private static MutualizedComponent dimension() throws RmesException {
        MutualizedComponent component = new MutualizedComponent();
        component.setIdentifiant("identifiant");
        component.setLabelLg1("label fr");
        component.setLabelLg2("label en");
        component.setType(QB.DIMENSION_PROPERTY.toString());
        return component;
    }

    private Model storedModel() throws RmesException {
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(repoGestion).loadSimpleObject(any(IRI.class), modelCaptor.capture(), isNull());
        return modelCaptor.getValue();
    }

}
