package fr.insee.rmes.bauhaus_services.structures.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausUriBuilder;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.structures.persistence.StructureRepository;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.QB;
import fr.insee.rmes.modules.structures.infrastructure.graphdb.StructureQueries;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptConceptsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StructureImplTest {

    @Mock
    RepositoryGestion repoGestion;

    @Mock
    StructureRepository structureRepository;

    @Mock
    CodeListService codeListService;

    @Mock
    StructureQueries structureQueries;

    @Mock
    ConceptConceptsQueries conceptConceptsQueries;

    private StructureImpl structureService;

    @BeforeEach
    void initStaticGraphsAndService() {
        RdfUtils.setGraphs(GraphsPropertiesStub.stub());
        RdfUtils.setBauhausUriBuilder(
                new BauhausUriBuilder("http://publication/", "http://bauhaus/", name -> Optional.of("structures")));
        structureService = new StructureImpl(
                repoGestion,
                null,
                null,
                null,
                structureRepository,
                codeListService,
                structureQueries,
                conceptConceptsQueries);
    }

    @Test
    void shouldSortTheStructuresByLabel() throws RmesException {
        when(structureQueries.getStructures()).thenReturn("structures-query");
        when(repoGestion.getResponseAsArray("structures-query"))
                .thenReturn(new JSONArray()
                        .put(partialStructure("dsd1001", "élan"))
                        .put(partialStructure("dsd1002", "abeille")));

        var structures = structureService.getStructures();

        assertThat(structures).extracting(s -> s.labelLg1()).containsExactly("abeille", "élan");
    }

    @Test
    void shouldDelegateTheSearchListingToTheRepository() throws RmesException {
        when(structureQueries.getStructures()).thenReturn("structures-query");
        JSONArray structures = new JSONArray().put(partialStructure("dsd1001", "label"));
        when(repoGestion.getResponseAsArray("structures-query")).thenReturn(structures);
        when(structureRepository.formatStructuresForSearch(structures)).thenReturn(structures);

        assertThat(structureService.getStructuresForSearch()).contains("dsd1001");
    }

    @Test
    void shouldReadTheStructureWithItsContributors() throws RmesException {
        JSONObject structure = new JSONObject().put(Constants.ID, "dsd1001");
        when(structureQueries.getStructureById("dsd1001")).thenReturn("by-id-query");
        when(repoGestion.getResponseAsObject("by-id-query")).thenReturn(structure);
        when(structureRepository.formatStructure(structure, "dsd1001")).thenReturn(structure);

        assertThat(structureService.getStructureById("dsd1001")).contains("dsd1001");

        verify(repoGestion).getMultipleTripletsForObject(any(), anyString(), any(), anyString());
    }

    /**
     * La vue détaillée renomme le type technique du composant, remplace l'IRI de sa liste de codes
     * par la liste elle-même, et remplace l'IRI de son concept par le concept.
     */
    @Test
    void shouldExpandTheCodeListAndTheConceptOfEachComponentOfTheDetailedStructure() throws RmesException {
        JSONObject component = new JSONObject()
                .put("type", QB.ATTRIBUTE_PROPERTY.stringValue())
                .put(Constants.CODELIST, "http://bauhaus/codes/cl1000")
                .put(Constants.CONCEPT, "http://bauhaus/concepts/c1000")
                .put("range", "http://range");
        JSONObject componentDefinition = new JSONObject()
                .put(Constants.ID, "cs1000")
                .put("created", "2026-01-01T10:00:00")
                .put("modified", "2026-01-02T10:00:00")
                .put("attachment", new JSONArray())
                .put("component", component);
        givenDetailedStructure(componentDefinition);
        when(codeListService.getCodesListByIRI("http://bauhaus/codes/cl1000")).thenReturn("[{\"code\":\"A\"}]");
        when(conceptConceptsQueries.conceptQueryForDetailStructure("http://bauhaus/concepts/c1000"))
                .thenReturn("concept-query");
        when(repoGestion.getResponseAsObject("concept-query")).thenReturn(new JSONObject().put(Constants.ID, "c1000"));

        JSONObject detailed = new JSONObject(structureService.getStructureByIdWithDetails("dsd1001"));

        JSONObject definition = detailed.getJSONArray("componentDefinitions").getJSONObject(0);
        assertThat(definition.has("attachment")).isFalse();
        assertThat(definition.has(Constants.ID)).isFalse();
        assertThat(definition.has("created")).isFalse();
        JSONObject expanded = definition.getJSONObject("component");
        assertThat(expanded.getString("type")).isEqualTo("attribute");
        assertThat(expanded.has("range")).isFalse();
        assertThat(expanded.getJSONObject(Constants.CODELIST)
                        .getJSONArray("codes")
                        .length())
                .isEqualTo(1);
        assertThat(expanded.getJSONObject(Constants.CONCEPT).getString(Constants.ID))
                .isEqualTo("c1000");
    }

    @Test
    void shouldRenameTheTypeOfAMeasureComponent() throws RmesException {
        givenDetailedStructure(componentDefinitionOfType(QB.MEASURE_PROPERTY.stringValue()));

        JSONObject detailed = new JSONObject(structureService.getStructureByIdWithDetails("dsd1001"));

        assertThat(typeOfFirstComponent(detailed)).isEqualTo("measure");
    }

    @Test
    void shouldRenameTheTypeOfADimensionComponent() throws RmesException {
        givenDetailedStructure(componentDefinitionOfType(QB.DIMENSION_PROPERTY.stringValue()));

        JSONObject detailed = new JSONObject(structureService.getStructureByIdWithDetails("dsd1001"));

        assertThat(typeOfFirstComponent(detailed)).isEqualTo("dimension");
    }

    @Test
    void shouldPublishTheStructureItJustRead() throws RmesException {
        JSONObject structure = new JSONObject().put(Constants.ID, "dsd1001");
        when(structureQueries.getStructureById("dsd1001")).thenReturn("by-id-query");
        when(repoGestion.getResponseAsObject("by-id-query")).thenReturn(structure);
        when(structureRepository.formatStructure(structure, "dsd1001")).thenReturn(structure);
        when(structureRepository.publishStructure(any(JSONObject.class))).thenReturn("published");

        assertThat(structureService.publishStructureById("dsd1001")).isEqualTo("published");
    }

    @Test
    void shouldDelegateTheWritesToTheRepository() throws RmesException {
        when(structureRepository.setStructure("body")).thenReturn("dsd1001");
        when(structureRepository.setStructure("dsd1001", "body")).thenReturn("dsd1001");

        assertThat(structureService.setStructure("body")).isEqualTo("dsd1001");
        assertThat(structureService.setStructure("dsd1001", "body")).isEqualTo("dsd1001");
        structureService.deleteStructure("dsd1001");

        verify(structureRepository).deleteStructure("dsd1001");
    }

    private void givenDetailedStructure(JSONObject componentDefinition) throws RmesException {
        JSONObject structure = new JSONObject().put(Constants.ID, "dsd1001");
        when(structureQueries.getStructureById("dsd1001")).thenReturn("by-id-query");
        when(repoGestion.getResponseAsObject("by-id-query")).thenReturn(structure);
        when(structureRepository.formatStructure(structure, "dsd1001"))
                .thenReturn(new JSONObject()
                        .put(Constants.ID, "dsd1001")
                        .put("componentDefinitions", new JSONArray().put(componentDefinition)));
    }

    private static JSONObject componentDefinitionOfType(String type) {
        return new JSONObject()
                .put("attachment", new JSONArray().put("http://attachment"))
                .put("component", new JSONObject().put("type", type));
    }

    private static String typeOfFirstComponent(JSONObject detailedStructure) {
        return detailedStructure
                .getJSONArray("componentDefinitions")
                .getJSONObject(0)
                .getJSONObject("component")
                .getString("type");
    }

    private static JSONObject partialStructure(String id, String labelLg1) {
        return new JSONObject()
                .put("iri", "http://bauhaus/structures/" + id)
                .put(Constants.ID, id)
                .put("labelLg1", labelLg1)
                .put("creator", "DG75-F302")
                .put("validationState", "Unpublished");
    }
}
