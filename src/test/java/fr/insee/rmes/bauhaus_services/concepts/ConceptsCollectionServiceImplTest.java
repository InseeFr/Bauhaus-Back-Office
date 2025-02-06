package fr.insee.rmes.bauhaus_services.concepts;

import fr.insee.rmes.Stubber;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConceptsCollectionServiceImplTest {
    @Mock
    RepositoryGestion repoGestion;

    @BeforeAll
    static void initGenericQueries(){
        GenericQueries.setConfig(new ConfigStub());
    }

    @Test
    void shouldGetCollectionsList() throws RmesException {
        ConceptsCollectionServiceImpl collectionsImpl = new ConceptsCollectionServiceImpl(null, null, 50);
        Stubber.forRdfService(collectionsImpl).injectRepoGestion(repoGestion);

        JSONArray array = new JSONArray();
        array.put(new JSONObject().put("id", "1").put("label", "label 1"));
        array.put(new JSONObject().put("id", "2").put("label", "elabel 1"));
        array.put(new JSONObject().put("id", "3").put("label", "alabel 1"));
        array.put(new JSONObject().put("id", "4").put("label", "élabel 1"));
        when(repoGestion.getResponseAsArray(anyString())).thenReturn(array);
        var collections = collectionsImpl.getCollections().stream().toList();

        assertEquals(4, collections.size());

        assertEquals("3", collections.get(0).id());
        assertEquals("alabel 1", collections.get(0).label());

        assertEquals("2", collections.get(1).id());
        assertEquals("elabel 1", collections.get(1).label());

        assertEquals("4", collections.get(2).id());
        assertEquals("élabel 1", collections.get(2).label());

        assertEquals("1", collections.get(3).id());
        assertEquals("label 1", collections.get(3).label());
    }
}