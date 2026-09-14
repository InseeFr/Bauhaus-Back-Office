package fr.insee.rmes.bauhaus_services.structures.persistence;

import static fr.insee.rmes.bauhaus_services.structures.persistence.StructureComponentRepository.MODIFIED;
import static fr.insee.rmes.bauhaus_services.structures.persistence.StructureComponentRepository.VALIDATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausIriFactory;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfTriples;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.graphdb.ontologies.QB;
import fr.insee.rmes.modules.codeslists.codeslists.infrastructure.graphdb.CodeListsQueries;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.modules.structures.components.domain.model.MutualizedComponent;
import fr.insee.rmes.modules.structures.infrastructure.graphdb.StructureQueries;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptConceptsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.List;
import java.util.Optional;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
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

@ExtendWith(MockitoExtension.class)
class StructureComponentRepositoryTest {

    private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();

    private static final String CREATION_DATE = "2026-01-01T10:00:00";

    @InjectMocks
    StructureComponentRepository structureComponentRepository;

    @Spy
    BauhausLanguagesProperties languages = new BauhausLanguagesProperties("fr", "en");

    @Mock
    RepositoryGestion repoGestion;

    @Mock
    ComponentPublication componentPublication;

    @Mock
    StructureQueries structureQueries;

    @Mock
    CodeListsQueries codeListsQueries;

    @Mock
    ConceptConceptsQueries conceptConceptsQueries;

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
        RmesException exception = assertThrows(
                RmesBadRequestException.class,
                () -> structureComponentRepository.updateComponent(
                        "componentId", "{\"id\":\"idExample\",\"creator\":\"creatorExample\"}"));
        assertThat(exception.getDetails())
                .contains("The id of the component should be the same as the one defined in the request");
    }

    @Test
    void shouldThrowRmesExceptionWhenValidateComponentWithoutIdentifiant() {
        RmesException exception = assertThrows(
                RmesBadRequestException.class,
                () -> structureComponentRepository.updateComponent(
                        "idExample", "{\"id\":\"idExample\",\"creator\":\"creatorExample\"}"));
        assertThat(exception.getDetails()).contains("The property identifiant is required");
    }

    @Test
    void shouldThrowRmesExceptionWhenValidateComponentWithoutLabelLg1() {
        RmesException exception = assertThrows(
                RmesBadRequestException.class,
                () -> structureComponentRepository.updateComponent(
                        "idExample",
                        "{\"id\":\"idExample\",\"identifiant\":\"identifiantExample\",\"creator\":\"creatorExample\"}"));
        assertThat(exception.getDetails()).contains("{\"message\":\"The property labelLg1 is required\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenValidateComponentWithoutLabelLg2() {
        RmesException exception = assertThrows(
                RmesBadRequestException.class,
                () -> structureComponentRepository.updateComponent(
                        "idExample",
                        "{\"id\":\"idExample\",\"identifiant\":\"identifiantExample\",\"labelLg1\":\"labelLg1Example\"}"));
        assertThat(exception.getDetails()).contains("{\"message\":\"The property labelLg2 is required\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenValidateComponentWithoutType() {
        RmesException exception = assertThrows(
                RmesBadRequestException.class,
                () -> structureComponentRepository.updateComponent(
                        "idExample",
                        "{\"id\":\"idExample\",\"identifiant\":\"identifiantExample\",\"labelLg1\":\"labelLg1Example\",\"labelLg2\":\"labelLg2Example\"}"));
        assertThat(exception.getDetails()).contains("{\"message\":\"The property type is required\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenValidateComponentWithInvalidateType() {
        RmesException exception = assertThrows(
                RmesBadRequestException.class,
                () -> structureComponentRepository.updateComponent(
                        "idExample",
                        "{\"id\":\"idExample\",\"identifiant\":\"identifiantExample\",\"labelLg1\":\"labelLg1Example\",\"labelLg2\":\"labelLg2Example\",\"type\":\"typeExample\"}"));
        assertThat(exception.getDetails()).contains("{\"message\":\"The property type is not valid\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenCreateComponent() {
        RmesException exception = assertThrows(
                RmesBadRequestException.class,
                () -> structureComponentRepository.createComponent(
                        "{\"id\":\"idExample\",\"identifiant\":\"identifiantExample\",\"labelLg1\":\"labelLg1Example\",\"labelLg2\":\"labelLg2Example\",\"type\":\"typeExample\"}"));
        assertThat(exception.getDetails())
                .contains("{\"message\":\"During the creation of a new component, the id property should be null\"}");
    }

    @ParameterizedTest
    @ValueSource(strings = {VALIDATED, MODIFIED})
    void shouldThrowRmesExceptionWhenDeleteComponent(String value) {
        JSONObject component = new JSONObject().put("validationState", value);
        RmesException exception = assertThrows(
                RmesException.class, () -> structureComponentRepository.deleteComponent(component, "id", "type"));
        assertThat(exception.getDetails())
                .contains("{\"details\":\"[]\",\"message\":\"You cannot delete a validated component\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenDeleteComponentWithInvalidStructures() {
        JSONObject exampleOne = new JSONObject().put("validationState", VALIDATED);
        JSONObject exampleTwo = new JSONObject().put("validationState", "example");
        JSONArray structures = new JSONArray().put(exampleOne).put(exampleTwo);
        JSONObject component = new JSONObject().put("validationState", "value").put("structures", structures);

        RmesException exception = assertThrows(
                RmesException.class, () -> structureComponentRepository.deleteComponent(component, "id", "type"));
        assertThat(exception.getDetails())
                .contains("{\"details\":\"[]\",\"message\":\"You cannot delete a validated component\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishingAnAlreadyPublishedComponent() {
        JSONObject component = new JSONObject()
                .put(Constants.ID, "c1000")
                .put(Constants.CREATOR, "creatorExample")
                .put("disseminationStatus", "http://status")
                .put("validationState", "Validated");

        RmesException exception = assertThrows(
                RmesBadRequestException.class, () -> structureComponentRepository.publishComponent(component));

        assertThat(exception.getDetails()).contains("\"code\":1301");
        assertThat(exception.getDetails()).contains("This component is already published");
        assertThat(exception.getDetails()).contains("Component: c1000");
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishComponentWithInvalidCreator() {
        JSONObject component = new JSONObject().put(Constants.CREATOR, "");
        RmesException exception =
                assertThrows(RmesException.class, () -> structureComponentRepository.publishComponent(component));
        assertThat(exception.getDetails())
                .contains("{\"code\":1004,\"details\":\"[]\",\"message\":\"The creator should not be empty\"}");
    }

    @Test
    void shouldThrowRmesExceptionWhenPublishComponentWithInvalidDisseminationStatus() {
        JSONObject component =
                new JSONObject().put(Constants.CREATOR, "creatorExample").put("disseminationStatus", "");
        RmesException exception =
                assertThrows(RmesException.class, () -> structureComponentRepository.publishComponent(component));
        assertThat(exception.getDetails())
                .contains(
                        "{\"code\":1005,\"details\":\"[]\",\"message\":\"The dissemination status should not be empty\"}");
    }

    @Test
    void shouldStoreCreatorAndContributorAsUriForComponent() throws RmesException {
        MutualizedComponent component = dimension();
        component.setCreator("http://creator-uri");
        component.setContributor(List.of("http://contributor-uri"));

        structureComponentRepository.createComponent(component, "d1000", new JSONObject());

        assertThat(storedModel())
                .anyMatch(stmt -> stmt.getPredicate().equals(DC.CREATOR)
                        && stmt.getObject().equals(VF.createIRI("http://creator-uri")));
        assertThat(storedModel())
                .anyMatch(stmt -> stmt.getPredicate().equals(DC.CONTRIBUTOR)
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
        assertThat(storedModel())
                .allMatch(stmt -> stmt.getSubject().equals(expectedIri) && expectedGraph.equals(stmt.getContext()));
    }

    @Test
    void shouldRangeTheFormattedComponentOnCodeListAndAttachItsStructures() throws RmesException {
        when(repoGestion.getResponseAsArray(any()))
                .thenReturn(new JSONArray().put(new JSONObject().put(Constants.ID, "s1000")));

        JSONObject formatted = structureComponentRepository.formatComponent(
                "d1000", new JSONObject().put(Constants.CODELIST, "http://codelist"));

        assertThat(formatted.getString(Constants.ID)).isEqualTo("d1000");
        assertThat(formatted.getString("range")).isEqualTo(INSEE.CODELIST.stringValue());
        assertThat(formatted.getJSONArray("structures").length()).isEqualTo(1);
    }

    @Test
    void shouldNotRangeTheFormattedComponentOnCodeListWhenItHasNone() throws RmesException {
        when(repoGestion.getResponseAsArray(any())).thenReturn(new JSONArray());

        JSONObject formatted = structureComponentRepository.formatComponent("d1000", new JSONObject());

        assertThat(formatted.has("range")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Unpublished", Constants.UNDEFINED})
    void shouldKeepAnUpdatedComponentUnpublishedWhenItWasNotPublishedYet(String state) throws RmesException {
        when(repoGestion.getResponseAsObject(any())).thenReturn(new JSONObject().put("state", state));

        String id = structureComponentRepository.updateComponent("d1000", dimensionBody());

        assertThat(id).isEqualTo("d1000");
        assertThat(validationState()).isEqualTo(ValidationStatus.UNPUBLISHED.getValue());
    }

    @Test
    void shouldFlagAnUpdatedComponentAsModifiedWhenItIsAlreadyPublished() throws RmesException {
        when(repoGestion.getResponseAsObject(any()))
                .thenReturn(new JSONObject().put("state", ValidationStatus.VALIDATED.getValue()));

        structureComponentRepository.updateComponent("d1000", dimensionBody());

        assertThat(validationState()).isEqualTo(ValidationStatus.MODIFIED.getValue());
    }

    /**
     * L'unicité n'est vérifiée que pour un composant qui porte à la fois un concept et une liste
     * de codes : c'est ce couple qui ne doit pas être partagé par deux composants mutualisés.
     */
    @Test
    void shouldRejectAComponentSharingItsConceptAndCodeListWithAnotherOne() throws RmesException {
        MutualizedComponent component = dimension();
        component.setConcept("c1000");
        component.setCodeList("http://codelist");
        when(repoGestion.getResponseAsBoolean(any())).thenReturn(true);

        RmesException exception = assertThrows(
                RmesBadRequestException.class,
                () -> structureComponentRepository.createComponent(component, "d1000", new JSONObject()));

        assertThat(exception.getDetails()).contains("\"code\":1002");
        assertThat(exception.getDetails()).contains("A component with the same codes list and concept already exists");
    }

    @Test
    void shouldStoreTheComponentWhenNoOtherOneSharesItsConceptAndCodeList() throws RmesException {
        MutualizedComponent component = dimension();
        component.setContributor(List.of());
        component.setConcept("c1000");
        component.setCodeList("http://codelist");
        when(repoGestion.getResponseAsBoolean(any())).thenReturn(false);

        structureComponentRepository.createComponent(component, "d1000", new JSONObject());

        assertThat(objectOf(QB.CONCEPT)).startsWith("http://bauhaus/").endsWith("/c1000");
        assertThat(objectOf(QB.CODE_LIST)).isEqualTo("http://codelist");
    }

    @Test
    void shouldFallBackOnTheFullCodeListValueWhenTheComponentHasNoCodeList() throws RmesException {
        MutualizedComponent component = dimension();
        component.setContributor(List.of());
        component.setCodeList("");
        component.setFullCodeListValue("http://full-codelist");

        structureComponentRepository.createComponent(component, "d1000", new JSONObject());

        assertThat(objectOf(QB.CODE_LIST)).isEqualTo("http://full-codelist");
    }

    @Test
    void shouldTypeAComponentRangedOnCodeListAsCodedAndPointItToTheOwlClassOfItsList() throws RmesException {
        MutualizedComponent component = dimension();
        component.setContributor(List.of());
        component.setRange(INSEE.CODELIST.stringValue());
        component.setFullCodeListValue("http://codelist");
        when(repoGestion.getResponseAsObject(any()))
                .thenReturn(new JSONObject().put("uriClasseOwl", "http://owl-class"));

        structureComponentRepository.createComponent(component, "d1000", new JSONObject());

        assertThat(storedModel())
                .anyMatch(statement -> statement.getPredicate().equals(RDF.TYPE)
                        && statement.getObject().equals(QB.CODED_PROPERTY));
        assertThat(objectOf(RDFS.RANGE)).isEqualTo("http://owl-class");
    }

    @Test
    void shouldRangeAComponentOnSkosConceptWhenItsCodeListHasNoOwlClass() throws RmesException {
        MutualizedComponent component = dimension();
        component.setContributor(List.of());
        component.setRange(INSEE.CODELIST.stringValue());
        component.setFullCodeListValue("http://codelist");
        when(repoGestion.getResponseAsObject(any())).thenReturn(new JSONObject());

        structureComponentRepository.createComponent(component, "d1000", new JSONObject());

        assertThat(objectOf(RDFS.RANGE)).isEqualTo(SKOS.CONCEPT.stringValue());
    }

    @Test
    void shouldStoreThePatternOfADateComponent() throws RmesException {
        Model model = storedModelForRangedComponent(XSD.DATE.stringValue());

        assertThat(objectOf(model, RdfTriples.xsdIri("pattern"))).isEqualTo("dd/MM/yyyy");
    }

    @Test
    void shouldStoreThePatternOfADateTimeComponent() throws RmesException {
        Model model = storedModelForRangedComponent(XSD.DATETIME.stringValue());

        assertThat(objectOf(model, RdfTriples.xsdIri("pattern"))).isEqualTo("dd/MM/yyyy");
    }

    @Test
    void shouldStoreTheBoundsOfANumericComponent() throws RmesException {
        Model model = storedModelForRangedComponent(XSD.INTEGER.stringValue());

        assertThat(objectOf(model, RdfTriples.xsdIri("minLength"))).isEqualTo("1");
        assertThat(objectOf(model, RdfTriples.xsdIri("maxLength"))).isEqualTo("10");
        assertThat(objectOf(model, RdfTriples.xsdIri("minInclusive"))).isEqualTo("0");
        assertThat(objectOf(model, RdfTriples.xsdIri("maxInclusive"))).isEqualTo("100");
    }

    @Test
    void shouldStoreTheLengthsAndThePatternOfAStringComponent() throws RmesException {
        Model model = storedModelForRangedComponent(XSD.STRING.stringValue());

        assertThat(objectOf(model, RdfTriples.xsdIri("minLength"))).isEqualTo("1");
        assertThat(objectOf(model, RdfTriples.xsdIri("maxLength"))).isEqualTo("10");
        assertThat(objectOf(model, RdfTriples.xsdIri("pattern"))).isEqualTo("dd/MM/yyyy");
        assertThat(model).noneMatch(statement -> statement.getPredicate().equals(RdfTriples.xsdIri("minInclusive")));
    }

    @Test
    void shouldDeleteAnAttributeUnderItsAttributeIri() throws RmesException {
        structureComponentRepository.deleteComponent(
                deletableComponent(), "a1000", QB.ATTRIBUTE_PROPERTY.stringValue());

        verify(repoGestion).deleteObject(VF.createIRI("http://bauhaus/composants/attribut/a1000"), null);
    }

    @Test
    void shouldDeleteAMeasureUnderItsMeasureIri() throws RmesException {
        structureComponentRepository.deleteComponent(deletableComponent(), "m1000", QB.MEASURE_PROPERTY.stringValue());

        verify(repoGestion).deleteObject(VF.createIRI("http://bauhaus/composants/mesure/m1000"), null);
    }

    @Test
    void shouldDeleteADimensionUnderItsDimensionIri() throws RmesException {
        structureComponentRepository.deleteComponent(
                deletableComponent(), "d1000", QB.DIMENSION_PROPERTY.stringValue());

        verify(repoGestion).deleteObject(VF.createIRI("http://bauhaus/composants/dimension/d1000"), null);
    }

    @Test
    void shouldRejectThePublicationOfAComponentWhoseConceptIsNotValidated() throws RmesException {
        JSONObject component = publishableComponent().put(Constants.CONCEPT, "http://concept");
        when(repoGestion.getResponseAsBoolean(any())).thenReturn(false);

        RmesException exception = assertThrows(
                RmesBadRequestException.class, () -> structureComponentRepository.publishComponent(component));

        assertThat(exception.getDetails()).contains("\"code\":1006");
        assertThat(exception.getDetails()).contains("The concept should be validated");
    }

    @Test
    void shouldRejectThePublicationOfAComponentWhoseCodeListIsNotValidated() throws RmesException {
        JSONObject component = publishableComponent().put(Constants.CODELIST, "http://codelist");
        when(repoGestion.getResponseAsBoolean(any())).thenReturn(false);

        RmesException exception = assertThrows(
                RmesBadRequestException.class, () -> structureComponentRepository.publishComponent(component));

        assertThat(exception.getDetails()).contains("\"code\":1007");
        assertThat(exception.getDetails()).contains("The codes list should be validated");
    }

    @Test
    void shouldPublishAnAttributeUnderItsAttributeIri() throws RmesException {
        String id = structureComponentRepository.publishComponent(
                publishableComponent("a1000", QB.ATTRIBUTE_PROPERTY.stringValue()));

        assertThat(id).isEqualTo("a1000");
        verify(componentPublication)
                .publishComponent(VF.createIRI("http://bauhaus/composants/attribut/a1000"), QB.ATTRIBUTE_PROPERTY);
        assertThat(validationState()).isEqualTo(ValidationStatus.VALIDATED.getValue());
    }

    @Test
    void shouldPublishAMeasureUnderItsMeasureIri() throws RmesException {
        structureComponentRepository.publishComponent(publishableComponent("m1000", QB.MEASURE_PROPERTY.stringValue()));

        verify(componentPublication)
                .publishComponent(VF.createIRI("http://bauhaus/composants/mesure/m1000"), QB.MEASURE_PROPERTY);
    }

    @Test
    void shouldPublishADimensionUnderItsDimensionIri() throws RmesException {
        structureComponentRepository.publishComponent(
                publishableComponent("d1000", QB.DIMENSION_PROPERTY.stringValue()));

        verify(componentPublication)
                .publishComponent(VF.createIRI("http://bauhaus/composants/dimension/d1000"), QB.DIMENSION_PROPERTY);
    }

    private static String dimensionBody() {
        return new JSONObject()
                .put(Constants.ID, "d1000")
                .put("identifiant", "identifiant")
                .put("labelLg1", "label fr")
                .put("labelLg2", "label en")
                .put("created", CREATION_DATE)
                .put("contributor", new JSONArray())
                .put("type", QB.DIMENSION_PROPERTY.stringValue())
                .toString();
    }

    private static JSONObject deletableComponent() {
        return new JSONObject()
                .put(Constants.VALIDATION_STATE, ValidationStatus.UNPUBLISHED.getValue())
                .put("structures", new JSONArray());
    }

    private static JSONObject publishableComponent() {
        return new JSONObject()
                .put(Constants.VALIDATION_STATE, ValidationStatus.UNPUBLISHED.getValue())
                .put(Constants.CREATOR, "http://creator")
                .put("disseminationStatus", "http://status");
    }

    private static JSONObject publishableComponent(String id, String type) {
        return publishableComponent()
                .put(Constants.ID, id)
                .put("identifiant", "identifiant")
                .put("labelLg1", "label fr")
                .put("labelLg2", "label en")
                .put("created", CREATION_DATE)
                .put("contributor", new JSONArray())
                .put("type", type);
    }

    /**
     * Un composant dont toutes les facettes de restriction sont renseignées : seules celles que
     * la portée demandée autorise doivent se retrouver dans le modèle écrit.
     */
    private Model storedModelForRangedComponent(String range) throws RmesException {
        MutualizedComponent component = dimension();
        component.setContributor(List.of());
        component.setRange(range);
        component.setPattern("dd/MM/yyyy");
        component.setMinLength("1");
        component.setMaxLength("10");
        component.setMinInclusive("0");
        component.setMaxInclusive("100");

        structureComponentRepository.createComponent(component, "d1000", new JSONObject());

        return storedModel();
    }

    private String validationState() throws RmesException {
        return objectOf(INSEE.VALIDATION_STATE);
    }

    private String objectOf(IRI predicate) throws RmesException {
        return objectOf(storedModel(), predicate);
    }

    private static String objectOf(Model model, IRI predicate) {
        return model.stream()
                .filter(statement -> statement.getPredicate().equals(predicate))
                .map(statement -> statement.getObject().stringValue())
                .findFirst()
                .orElseThrow(() -> new AssertionError("no statement for " + predicate));
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
