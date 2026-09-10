package fr.insee.rmes.bauhaus_services.structures.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.structures.persistence.StructureComponentRepository;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.graphdb.ontologies.QB;
import fr.insee.rmes.modules.structures.infrastructure.graphdb.StructureQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StructureComponentImplTest {

    @Mock
    RepositoryGestion repoGestion;

    @Mock
    StructureComponentRepository structureComponentRepository;

    @Mock
    StructureQueries structureQueries;

    private StructureComponentImpl structureComponentService;

    @BeforeEach
    void setUp() {
        structureComponentService = new StructureComponentImpl(
                repoGestion, null, null, null, structureComponentRepository, structureQueries);
    }

    @Test
    void shouldListEveryKindOfMutualizedComponentForSearch() throws RmesException {
        when(structureQueries.getComponents(true, true, true)).thenReturn("components-query");
        when(repoGestion.getResponseAsArray("components-query"))
                .thenReturn(new JSONArray().put(new JSONObject().put(Constants.ID, "d1000")));

        assertThat(structureComponentService.getComponentsForSearch()).contains("d1000");
    }

    @Test
    void shouldListOnlyTheAttributes() throws RmesException {
        when(structureQueries.getComponents(true, false, false)).thenReturn("attributes-query");
        when(repoGestion.getResponseAsArray("attributes-query"))
                .thenReturn(new JSONArray().put(new JSONObject().put(Constants.ID, "a1000")));

        assertThat(structureComponentService.getAttributes()).contains("a1000");
    }

    @Test
    void shouldSortTheComponentsByLabel() throws RmesException {
        when(structureQueries.getComponents(true, true, true)).thenReturn("components-query");
        when(repoGestion.getResponseAsArray("components-query"))
                .thenReturn(new JSONArray()
                        .put(partialComponent("d1001", "élan"))
                        .put(partialComponent("d1002", "abeille")));

        assertThat(structureComponentService.getComponents())
                .extracting(component -> component.labelLg1())
                .containsExactly("abeille", "élan");
    }

    @Test
    void shouldRejectTheReadOfAComponentThatDoesNotExist() throws RmesException {
        when(structureQueries.getComponent("d1000")).thenReturn("component-query");
        when(repoGestion.getResponseAsArray("component-query")).thenReturn(new JSONArray());

        RmesException exception =
                assertThrows(RmesNotFoundException.class, () -> structureComponentService.getComponent("d1000"));

        assertThat(exception.getDetails()).contains("This component does not exist");
    }

    /**
     * La requête renvoie une ligne par attribut porté par le composant : le service les remet à
     * plat en couples {@code attribute_n} / {@code attributeValue_n}, ceux que le formulaire attend.
     */
    @Test
    void shouldFlattenTheAttributesCarriedByTheComponent() throws RmesException {
        givenComponentRows(
                componentRow().put("attributeIRI", "http://attribut/1").put("valueIri", "http://valeur/1"),
                componentRow().put("attributeIRI", "http://attribut/2").put("valueIri", "http://valeur/2"),
                componentRow().put("attributeIRI", "").put("valueIri", ""));

        structureComponentService.getComponent("d1000");

        JSONObject formatted = formattedComponent();
        assertThat(formatted.getString("attribute_0")).isEqualTo("http://attribut/1");
        assertThat(formatted.getString("attributeValue_0")).isEqualTo("http://valeur/1");
        assertThat(formatted.getString("attribute_1")).isEqualTo("http://attribut/2");
        assertThat(formatted.has("attribute_2")).isFalse();
        assertThat(formatted.has("attributeIRI")).isFalse();
        assertThat(formatted.has("valueIri")).isFalse();
        assertThat(formatted.has("component")).isFalse();
        verify(repoGestion).getMultipleTripletsForObject(any(), anyString(), any(), anyString());
    }

    @Test
    void shouldDeleteTheComponentUnderItsType() throws RmesException {
        givenComponentRows(componentRow());

        structureComponentService.deleteComponent("d1000");

        verify(structureComponentRepository)
                .deleteComponent(any(JSONObject.class), eq("d1000"), eq(QB.DIMENSION_PROPERTY.stringValue()));
    }

    @Test
    void shouldPublishTheComponentItJustRead() throws RmesException {
        givenComponentRows(componentRow());
        when(structureComponentRepository.publishComponent(any(JSONObject.class)))
                .thenReturn("d1000");

        assertThat(structureComponentService.publishComponent("d1000")).isEqualTo("d1000");
    }

    @Test
    void shouldDelegateTheWritesToTheRepository() throws RmesException {
        when(structureComponentRepository.createComponent("body")).thenReturn("d1000");
        when(structureComponentRepository.updateComponent("d1000", "body")).thenReturn("d1000");

        assertThat(structureComponentService.createComponent("body")).isEqualTo("d1000");
        assertThat(structureComponentService.updateComponent("d1000", "body")).isEqualTo("d1000");
    }

    private void givenComponentRows(JSONObject... rows) throws RmesException {
        JSONArray response = new JSONArray();
        for (JSONObject row : rows) {
            response.put(row);
        }
        when(structureQueries.getComponent("d1000")).thenReturn("component-query");
        when(repoGestion.getResponseAsArray("component-query")).thenReturn(response);
        when(structureComponentRepository.formatComponent(eq("d1000"), any(JSONObject.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));
    }

    private JSONObject formattedComponent() throws RmesException {
        ArgumentCaptor<JSONObject> componentCaptor = ArgumentCaptor.forClass(JSONObject.class);
        verify(structureComponentRepository).formatComponent(eq("d1000"), componentCaptor.capture());
        return componentCaptor.getValue();
    }

    private static JSONObject componentRow() {
        return new JSONObject()
                .put(Constants.ID, "d1000")
                .put("component", "http://bauhaus/composants/dimension/d1000")
                .put("type", QB.DIMENSION_PROPERTY.stringValue());
    }

    private static JSONObject partialComponent(String id, String labelLg1) {
        return new JSONObject()
                .put("iri", "http://bauhaus/composants/dimension/" + id)
                .put(Constants.ID, id)
                .put("identifiant", id)
                .put("labelLg1", labelLg1)
                .put("type", QB.DIMENSION_PROPERTY.stringValue())
                .put("validationState", "Unpublished")
                .put("creator", "DG75-F302");
    }
}
