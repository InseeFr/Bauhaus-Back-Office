package fr.insee.rmes.bauhaus_services.code_list;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.modules.codeslists.codeslists.infrastructure.graphdb.CodeListsQueries;
import fr.insee.rmes.modules.codeslists.codeslists.webservice.CodeRequest;
import fr.insee.rmes.modules.commons.configuration.swagger.model.code_list.CodeList;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.List;
import java.util.Optional;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.PROV;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Écriture d'une liste de codes, avec le vrai {@link RdfUtils} : ce que ces tests vérifient, ce
 * sont les triplets produits, donc les IRI et les graphes doivent être ceux de la configuration
 * et non des bouchons.
 */
@ExtendWith(MockitoExtension.class)
class CodeListServiceImplWriteTest {

    @Mock
    RepositoryGestion repoGestion;

    @Mock
    CodeListsQueries codeListsQueries;

    @Mock
    CodeListPublication codeListPublication;

    private CodeListServiceImpl codeListService;

    @BeforeEach
    void initStaticGraphsAndService() {
        RdfUtils.setGraphs(GraphsPropertiesStub.stub());
        RdfUtils.setBauhausUriBuilder(
                new BauhausUriBuilder("http://publication/", "http://bauhaus/", name -> Optional.of("codes")));
        codeListService = new CodeListServiceImpl(
                repoGestion,
                null,
                null,
                new BauhausLanguagesProperties("fr", "en"),
                null,
                codeListPublication,
                codeListsQueries);
    }

    @Test
    void shouldRejectAPartialCodeListWithoutAnyCode() {
        JSONObject codeList = fullCodeList().put("codes", new JSONObject());

        RmesException exception = assertThrows(
                RmesBadRequestException.class, () -> codeListService.validateCodeList(codeList, CodeListKind.PARTIAL));

        assertThat(exception.getDetails()).contains("A code list should contain at least one code");
    }

    @Test
    void shouldRejectAPartialCodeListWithoutACodesProperty() {
        JSONObject codeList = fullCodeList();

        RmesException exception = assertThrows(
                RmesBadRequestException.class, () -> codeListService.validateCodeList(codeList, CodeListKind.PARTIAL));

        assertThat(exception.getDetails()).contains("A code list should contain at least one code");
    }

    /** Une liste complète est identifiée par son IRI, sa notation et sa classe OWL : les trois ensemble. */
    @Test
    void shouldRejectAFullCodeListWhoseIdentityIsAlreadyTaken() throws RmesException {
        when(codeListsQueries.checkCodeListUnicity(anyString(), anyString(), anyString(), any(CodeListKind.class)))
                .thenReturn("unicity-query");
        when(repoGestion.getResponseAsBoolean("unicity-query")).thenReturn(true);

        RmesException exception = assertThrows(
                RmesBadRequestException.class,
                () -> codeListService.setCodesList(fullCodeList().toString(), CodeListKind.FULL));

        assertThat(exception.getDetails()).contains("The identifier, IRI and OWL class should be unique");
    }

    @Test
    void shouldCheckTheUnicityOfAPartialCodeListWithoutAnyOwlClass() throws RmesException {
        when(codeListsQueries.checkCodeListUnicity("id", "http://bauhaus/codes/id", "", CodeListKind.PARTIAL))
                .thenReturn("unicity-query");
        when(repoGestion.getResponseAsBoolean("unicity-query")).thenReturn(true);

        assertThrows(
                RmesBadRequestException.class,
                () -> codeListService.setCodesList(partialCodeListWithOneCode().toString(), CodeListKind.PARTIAL));
    }

    @Test
    void shouldRejectAnUpdateWhoseBodyCarriesAnotherId() {
        RmesException exception = assertThrows(
                RmesBadRequestException.class,
                () -> codeListService.setCodesList("another-id", fullCodeList().toString(), CodeListKind.FULL));

        assertThat(exception.getDetails()).contains("The id of the list should match the id of the url");
    }

    @Test
    void shouldRejectTheUpdateOfACodeListThatDoesNotExistYet() throws RmesException {
        when(codeListsQueries.getCodesListByIri(anyString())).thenReturn("by-iri-query");
        when(repoGestion.getResponseAsObject("by-iri-query")).thenReturn(new JSONObject());

        RmesException exception = assertThrows(
                RmesNotFoundException.class,
                () -> codeListService.setCodesList("id", fullCodeList().toString(), CodeListKind.FULL));

        assertThat(exception.getDetails()).contains("CodeList not found");
    }

    /** La date de création appartient à la base, pas au corps de la requête. */
    @Test
    void shouldKeepThePersistedCreationDateWhenUpdatingACodeList() throws RmesException {
        when(codeListsQueries.getCodesListByIri(anyString())).thenReturn("by-iri-query");
        when(repoGestion.getResponseAsObject("by-iri-query"))
                .thenReturn(new JSONObject().put(Constants.CREATED, "2020-01-01T10:00:00"));

        codeListService.setCodesList("id", fullCodeList().toString(), CodeListKind.FULL);

        assertThat(objectsOf(storedModel(), org.eclipse.rdf4j.model.vocabulary.DCTERMS.CREATED))
                .containsExactly("2020-01-01T10:00:00");
    }

    @Test
    void shouldFlagAnAlreadyPublishedCodeListAsModified() throws RmesException {
        JSONObject codeList =
                fullCodeList().put(CodeListServiceImpl.VALIDATION_STATE, ValidationStatus.VALIDATED.getValue());

        codeListService.setCodesList(codeList.toString(), CodeListKind.FULL);

        assertThat(objectsOf(storedModel(), INSEE.VALIDATION_STATE))
                .containsExactly(ValidationStatus.MODIFIED.getValue());
    }

    @Test
    void shouldStoreTheOptionalPropertiesOfAFullCodeList() throws RmesException {
        JSONObject codeList = fullCodeList()
                .put("disseminationStatus", "http://status")
                .put(Constants.DESCRIPTION_LG1, "description fr")
                .put(Constants.DESCRIPTION_LG2, "description en")
                .put(Constants.CREATOR, "http://creator")
                .put(Constants.CONTRIBUTOR, new JSONArray().put("http://contributor"));

        codeListService.setCodesList(codeList.toString(), CodeListKind.FULL);

        Model model = storedModel();
        assertThat(objectsOf(model, INSEE.DISSEMINATIONSTATUS)).containsExactly("http://status");
        assertThat(objectsOf(model, SKOS.DEFINITION)).containsExactlyInAnyOrder("description fr", "description en");
        assertThat(objectsOf(model, DC.CREATOR)).containsExactly("http://creator");
        assertThat(objectsOf(model, DC.CONTRIBUTOR)).containsExactly("http://contributor");
        assertThat(objectsOf(model, RDF.TYPE)).contains(SKOS.CONCEPT_SCHEME.stringValue());
    }

    @Test
    void shouldStoreThePartialCodeListAsACollectionOfItsCodesDerivedFromItsParent() throws RmesException {
        JSONObject codeList = partialCodeListWithOneCode().put("iriParent", "http://bauhaus/codes/parent");

        codeListService.setCodesList(codeList.toString(), CodeListKind.PARTIAL);

        Model model = storedModel();
        assertThat(objectsOf(model, RDF.TYPE)).contains(SKOS.COLLECTION.stringValue());
        assertThat(objectsOf(model, SKOS.MEMBER)).containsExactly("http://bauhaus/codes/id/A");
        assertThat(objectsOf(model, PROV.WAS_DERIVED_FROM)).containsExactly("http://bauhaus/codes/parent");
    }

    @Test
    void shouldNotLookUpTheUriOfACodeWhenTheNotationsAreMissing() throws RmesException {
        assertThat(codeListService.getCodeUri("", "code")).isNull();
        assertThat(codeListService.getCodeUri("notation", "")).isNull();
    }

    @Test
    void shouldReadTheUriOfACodeFromItsNotations() throws RmesException {
        when(codeListsQueries.getCodeUriByNotation("notation", "A")).thenReturn("code-uri-query");
        when(repoGestion.getResponseAsObject("code-uri-query"))
                .thenReturn(new JSONObject().put(Constants.URI, "http://bauhaus/codes/id/A"));

        assertThat(codeListService.getCodeUri("notation", "A")).isEqualTo("http://bauhaus/codes/id/A");
    }

    @Test
    void shouldReadACodeListByItsIri() throws RmesException {
        when(codeListsQueries.geCodesListByIRI("http://bauhaus/codes/id")).thenReturn("by-iri-query");
        when(repoGestion.getResponseAsArray("by-iri-query"))
                .thenReturn(new JSONArray().put(new JSONObject().put(Constants.ID, "id")));

        assertThat(codeListService.getCodesListByIRI("http://bauhaus/codes/id")).contains("\"id\":\"id\"");
    }

    private static JSONObject fullCodeList() {
        return new JSONObject()
                .put(Constants.ID, "id")
                .put(Constants.LABEL_LG1, "label fr")
                .put(Constants.LABEL_LG2, "label en")
                .put("lastListUriSegment", "id")
                .put("lastClassUriSegment", "Classe")
                .put("lastCodeUriSegment", "code");
    }

    private static JSONObject partialCodeListWithOneCode() {
        return new JSONObject()
                .put(Constants.ID, "id")
                .put(Constants.LABEL_LG1, "label fr")
                .put(Constants.LABEL_LG2, "label en")
                .put("codes", new JSONObject().put("A", new JSONObject().put("iri", "http://bauhaus/codes/id/A")));
    }

    private Model storedModel() throws RmesException {
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(repoGestion).loadSimpleObject(any(IRI.class), modelCaptor.capture(), isNull());
        return modelCaptor.getValue();
    }

    private static List<String> objectsOf(Model model, IRI predicate) {
        return model.stream()
                .filter(statement -> statement.getPredicate().equals(predicate))
                .map(statement -> statement.getObject().stringValue())
                .toList();
    }

    @Test
    void shouldGroupTheCodesUnderTheirOwnListWhenSearching() throws RmesException, JsonProcessingException {
        when(codeListsQueries.getCodesListsForSearch(CodeListKind.FULL)).thenReturn("lists-query");
        when(codeListsQueries.getCodesForSearch(CodeListKind.FULL)).thenReturn("codes-query");
        when(repoGestion.getResponseAsArray("lists-query"))
                .thenReturn(new JSONArray().put(codeListForSearch("first")).put(codeListForSearch("second")));
        when(repoGestion.getResponseAsArray("codes-query"))
                .thenReturn(new JSONArray()
                        .put(new JSONObject().put(Constants.ID, "first").put("code", "A"))
                        .put(new JSONObject().put(Constants.ID, "second").put("code", "B")));

        List<CodeList> codeLists = codeListService.getDetailedCodesListForSearch(CodeListKind.FULL);

        assertThat(codeLists).hasSize(2);
        assertThat(codeLists.getFirst().codes).hasSize(1);
    }

    @Test
    void shouldRejectTheDeletionOfAPublishedCodeList() throws RmesException {
        stubDetailedPartialCodeList(
                new JSONObject()
                        .put("iri", "http://bauhaus/codes/id")
                        .put(CodeListServiceImpl.VALIDATION_STATE, ValidationStatus.VALIDATED.getValue()),
                new JSONArray());

        RmesException exception = assertThrows(
                RmesBadRequestException.class, () -> codeListService.deleteCodeList("id", CodeListKind.FULL));

        assertThat(exception.getDetails()).contains("Only unpublished codelist can be deleted");
    }

    @Test
    void shouldRejectTheDeletionOfAFullCodeListStillCarryingPartialOnes() throws RmesException {
        stubDetailedPartialCodeList(unpublishedCodeList(), new JSONArray());
        when(codeListsQueries.getPartialCodeListByParentUri("http://bauhaus/codes/id"))
                .thenReturn("partials-query");
        when(repoGestion.getResponseAsArray("partials-query"))
                .thenReturn(new JSONArray().put(new JSONObject().put(Constants.ID, "partial")));

        RmesException exception = assertThrows(
                RmesBadRequestException.class, () -> codeListService.deleteCodeList("id", CodeListKind.FULL));

        assertThat(exception.getDetails()).contains("Only codelist with partial codelists can be deleted");
    }

    @Test
    void shouldDeleteTheCodesOfAFullCodeListAlongWithTheListItself() throws RmesException {
        stubDetailedPartialCodeList(
                unpublishedCodeList(),
                new JSONArray().put(new JSONObject().put("code", "A").put("iri", "http://bauhaus/codes/id/A")));
        when(codeListsQueries.getPartialCodeListByParentUri("http://bauhaus/codes/id"))
                .thenReturn("partials-query");
        when(repoGestion.getResponseAsArray("partials-query")).thenReturn(new JSONArray());

        codeListService.deleteCodeList("id", CodeListKind.FULL);

        verify(repoGestion).deleteObject(RdfUtils.toURI("http://bauhaus/codes/id/A"), null);
        verify(repoGestion).deleteObject(RdfUtils.toURI("http://bauhaus/codes/id"), null);
    }

    @Test
    void shouldPublishTheCodeListAndItsCodesAndValidateIt() throws RmesException {
        stubDetailedPartialCodeList(unpublishedCodeList(), new JSONArray());

        codeListService.publishCodeList("id", CodeListKind.FULL);

        IRI codeListIri = RdfUtils.createIRI("http://bauhaus/codes/id");
        verify(codeListPublication).publishCodeListAndCodes(codeListIri);
        ArgumentCaptor<Model> modelCaptor = ArgumentCaptor.forClass(Model.class);
        verify(repoGestion).objectValidation(eq(codeListIri), modelCaptor.capture());
        assertThat(objectsOf(modelCaptor.getValue(), INSEE.VALIDATION_STATE))
                .containsExactly(ValidationStatus.VALIDATED.getValue());
    }

    @Test
    void shouldStoreTheDescriptionsOfAnAddedCode() throws RmesException {
        CodeRequest code = new CodeRequest("A", "label fr", "label en", "description fr", "description en");
        when(codeListsQueries.getCodeByNotation("id", "A")).thenReturn("code-query");
        when(repoGestion.getResponseAsObject("code-query")).thenReturn(new JSONObject());
        when(codeListsQueries.getDetailedCodeListByNotation("id")).thenReturn("detailed-query");
        when(repoGestion.getResponseAsObject("detailed-query"))
                .thenReturn(fullCodeList().put("iri", "http://bauhaus/codes/id"));

        codeListService.addCodeFromCodeList("id", code);

        assertThat(objectsOf(storedModel(), SKOS.DEFINITION))
                .containsExactlyInAnyOrder("description fr", "description en");
    }

    private void stubDetailedPartialCodeList(JSONObject codeList, JSONArray codes) throws RmesException {
        when(codeListsQueries.getDetailedCodeListByNotation("id")).thenReturn("detailed-query");
        when(repoGestion.getResponseAsObject("detailed-query")).thenReturn(codeList);
        when(codeListsQueries.getDetailedCodes("id", CodeListKind.PARTIAL, null, 0, 0, null))
                .thenReturn("detailed-codes-query");
        when(repoGestion.getResponseAsArray("detailed-codes-query")).thenReturn(codes);
    }

    private static JSONObject unpublishedCodeList() {
        return new JSONObject()
                .put("iri", "http://bauhaus/codes/id")
                .put(CodeListServiceImpl.VALIDATION_STATE, ValidationStatus.UNPUBLISHED.getValue());
    }

    private static JSONObject codeListForSearch(String id) {
        return new JSONObject()
                .put(Constants.ID, id)
                .put("uri", "http://bauhaus/codes/" + id)
                .put(Constants.LABEL_LG1, "label fr")
                .put(Constants.LABEL_LG2, "label en");
    }
}
