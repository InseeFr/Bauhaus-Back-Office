package fr.insee.rmes.bauhaus_services.concepts.concepts;

import fr.insee.rmes.AppSpringBootTest;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.concepts.publication.ConceptsPublication;
import fr.insee.rmes.bauhaus_services.notes.NoteManager;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.model.concepts.ConceptForExport;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptConceptsQueries;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@AppSpringBootTest
class ConceptsUtilsTest {


    @InjectMocks
    ConceptsUtils conceptsUtils = new ConceptsUtils(new ConceptsPublication(),new NoteManager(null),5);

    @MockitoBean
    RepositoryGestion repoGestion;

    @MockitoBean
    RepositoryPublication repositoryPublication;

    @Test
    void shouldReturnGetConceptExportFileName() {

        ConceptsPublication conceptsPublication =  new ConceptsPublication();
        NoteManager noteManager = new NoteManager(null);
        ConceptsUtils conceptsUtilsExample = new ConceptsUtils(conceptsPublication,noteManager,19);

        ConceptForExport conceptForExport = new ConceptForExport();
        conceptForExport.setId("id");
        conceptForExport.setPrefLabelLg1("prefLabel1");
        conceptForExport.setPrefLabelLg2("prefLabel2");

        String response = conceptsUtilsExample.getConceptExportFileName(conceptForExport);

        assertEquals("idPreflabel1",response);
    }

    @Test
    void shouldCreateID() throws RmesException {
        List<String> identifiers = List.of("0007","0008","0009");
        List<String> actual = new ArrayList<>();
            for (String element : identifiers ){
                JSONObject json = new JSONObject().put(Constants.NOTATION,element);
                when(repoGestion.getResponseAsObject(ConceptConceptsQueries.lastConceptID())).thenReturn(json);
                actual.add(conceptsUtils.createID());
                }
        List<String> expected = List.of("c8","c9","c10");
        assertEquals(expected,actual);
    }

    @Test
    void shouldReturnFalseWhenCheckIfConceptExists() throws RmesException {
        String id= "2025";
        when(repoGestion.getResponseAsBoolean(ConceptConceptsQueries.checkIfExists(id))).thenReturn(false);
        assertFalse(conceptsUtils.checkIfConceptExists(id));
    }

    @Test
    void shouldThrowRmesNotFoundExceptionWhenGetConceptById() throws RmesException {
        String id= "2025";
        when(repoGestion.getResponseAsBoolean(ConceptConceptsQueries.checkIfExists(id))).thenReturn(false);
        RmesException exception = assertThrows(RmesNotFoundException.class, () ->conceptsUtils.getConceptById(id));
        Assertions.assertTrue(exception.getDetails().contains("This concept cannot be found in database"));
    }

    @Test
    void shouldCheckIfConceptExists() throws RmesException {
        when(repoGestion.getResponseAsBoolean(ConceptConceptsQueries.checkIfExists("mocked id"))).thenReturn(true);
        Assertions.assertTrue(conceptsUtils.checkIfConceptExists("mocked id"));
    }

    @Test
    void shouldDeleteConcept() throws RmesException {
        RdfUtils.setConfig(new ConfigStub());
        when(repoGestion.executeUpdate(ConceptConceptsQueries.deleteConcept(RdfUtils.toString(RdfUtils.objectIRI(ObjectType.CONCEPT,"mocked id")),RdfUtils.conceptGraph().toString()))).thenReturn(HttpStatus.OK);
        when(repositoryPublication.executeUpdate(ConceptConceptsQueries.deleteConcept(RdfUtils.toString(RdfUtils.objectIRIPublication(ObjectType.CONCEPT,"mocked id")),RdfUtils.conceptGraph().toString()))).thenReturn(HttpStatus.BAD_REQUEST);
        HttpStatus actual = conceptsUtils.deleteConcept("mocked id");
        assertEquals(HttpStatus.BAD_REQUEST,actual);
    }

    @Test
    void shouldGetRelatedConcepts() throws RmesException {
        JSONArray jsonArray = new JSONArray().put("mocked Array");
        when(repoGestion.getResponseAsArray(ConceptConceptsQueries.getRelatedConceptsQuery("mocked id"))).thenReturn(jsonArray);
        JSONArray actual = conceptsUtils.getRelatedConcepts("mocked id");
        assertEquals(jsonArray,actual);
    }

    @Test
    void shouldGetGraphsWithConcept() throws RmesException {
        JSONArray jsonArray = new JSONArray().put("mocked Array");
        when(repoGestion.getResponseAsArray(ConceptConceptsQueries.getGraphWithConceptQuery("mocked id"))).thenReturn(jsonArray);
        JSONArray actual = conceptsUtils.getGraphsWithConcept("mocked id");
        assertEquals(jsonArray,actual);
    }

    }