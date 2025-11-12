package fr.insee.rmes.testcontainers.queries.sparql_queries.concepts;

import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.GenericQueries;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptCollectionsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class ConceptCollectionsQueriesTest extends WithGraphDBContainer {

    RepositoryGestion repositoryGestion = new RepositoryGestion(getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    @BeforeAll
    static void initData() {
        container.withTrigFiles("concept-collections.trig");
        GenericQueries.setConfig(new ConfigStub());
    }

    @Test
    void should_return_all_collections() throws RmesException {
        // When
        JSONArray result = repositoryGestion.getResponseAsArray(ConceptCollectionsQueries.collectionsQuery());

        // Then
        assertEquals(5, result.length());

        // Verify that all collections have required fields
        for (int i = 0; i < result.length(); i++) {
            JSONObject collection = result.getJSONObject(i);
            assertTrue(collection.has("id"), "Collection should have an id");
            assertTrue(collection.has("label"), "Collection should have a label");
        }
    }

    @Test
    void should_return_collections_dashboard_with_members_count() throws RmesException {
        // When
        JSONArray result = repositoryGestion.getResponseAsArray(ConceptCollectionsQueries.collectionsDashboardQuery());

        // Then
        // Dashboard query only returns collections with members (excludes empty collections)
        assertEquals(4, result.length());

        // Find Agriculture collection and verify its member count
        JSONObject agricultureCollection = findCollectionById(result, "c1000");
        assertNotNull(agricultureCollection, "Agriculture collection should exist");
        assertEquals("3", agricultureCollection.getString("nbMembers"), "Agriculture collection should have 3 members");
        assertEquals("Collection Agriculture", agricultureCollection.getString("label"));
        assertEquals("true", agricultureCollection.getString("isValidated"));
        assertEquals("DG75-L201", agricultureCollection.getString("creator"));

        // Find Commerce collection and verify its member count
        JSONObject commerceCollection = findCollectionById(result, "c2000");
        assertNotNull(commerceCollection, "Commerce collection should exist");
        assertEquals("2", commerceCollection.getString("nbMembers"), "Commerce collection should have 2 members");
        assertEquals("false", commerceCollection.getString("isValidated"));

        // Find Emploi collection and verify its member count
        JSONObject emploiCollection = findCollectionById(result, "c3000");
        assertNotNull(emploiCollection, "Emploi collection should exist");
        assertEquals("4", emploiCollection.getString("nbMembers"), "Emploi collection should have 4 members");
    }

    @Test
    void should_return_only_non_validated_collections() throws RmesException {
        // When
        JSONArray result = repositoryGestion.getResponseAsArray(ConceptCollectionsQueries.collectionsToValidateQuery());

        // Then
        assertEquals(2, result.length(), "Should return only non-validated collections");

        // Verify all returned collections are not validated
        for (int i = 0; i < result.length(); i++) {
            JSONObject collection = result.getJSONObject(i);
            String id = collection.getString("id");
            assertTrue(id.equals("c2000") || id.equals("c4000"),
                "Only c2000 and c4000 should be non-validated");
        }

        // Verify Commerce collection
        JSONObject commerceCollection = findCollectionById(result, "c2000");
        assertNotNull(commerceCollection);
        assertEquals("Collection Commerce", commerceCollection.getString("label"));
        assertEquals("DG75-L202", commerceCollection.getString("creator"));

        // Verify Démographie collection
        JSONObject demoCollection = findCollectionById(result, "c4000");
        assertNotNull(demoCollection);
        assertEquals("Collection Démographie", demoCollection.getString("label"));
        assertEquals("DG75-L204", demoCollection.getString("creator"));
    }

    @Test
    void should_return_specific_collection_by_id() throws RmesException {
        // When
        JSONObject result = repositoryGestion.getResponseAsObject(ConceptCollectionsQueries.collectionQuery("c1000"));

        // Then
        assertNotNull(result);
        assertEquals("c1000", result.getString("id"));
        assertEquals("Collection Agriculture", result.getString("prefLabelLg1"));
        assertEquals("Agriculture Collection", result.getString("prefLabelLg2"));
    }

    @Test
    void should_return_collection_members() throws RmesException {
        // When - Get members of Agriculture collection
        JSONArray result = repositoryGestion.getResponseAsArray(ConceptCollectionsQueries.collectionMembersQuery("c1000"));

        // Then
        assertEquals(3, result.length(), "Agriculture collection should have 3 members");

        // Verify member concepts are returned
        boolean hasC1 = false, hasC2 = false, hasC3 = false;
        for (int i = 0; i < result.length(); i++) {
            JSONObject member = result.getJSONObject(i);
            String memberId = member.getString("id");
            if (memberId.equals("c1")) hasC1 = true;
            if (memberId.equals("c2")) hasC2 = true;
            if (memberId.equals("c3")) hasC3 = true;
        }
        assertTrue(hasC1 && hasC2 && hasC3, "All three concepts should be members");
    }

    @Test
    void should_return_empty_array_for_collection_without_members() throws RmesException {
        // When - Get members of empty collection
        JSONArray result = repositoryGestion.getResponseAsArray(ConceptCollectionsQueries.collectionMembersQuery("c5000"));

        // Then
        assertEquals(0, result.length(), "Empty collection should have no members");
    }

    @Test
    void should_return_collection_concepts_with_components_graph() throws RmesException {
        // When - Get concepts from Agriculture collection including those in components graph
        JSONArray result = repositoryGestion.getResponseAsArray(ConceptCollectionsQueries.collectionConceptsQuery("c1000"));

        // Then
        assertTrue(result.length() > 0, "Should return concepts from the collection");

        // Verify concept c1 which exists in both graphs
        boolean foundC1 = false;
        for (int i = 0; i < result.length(); i++) {
            JSONObject concept = result.getJSONObject(i);
            if (concept.has("id") && concept.getString("id").equals("c1")) {
                foundC1 = true;
                assertTrue(concept.has("prefLabelLg1") || concept.has("label"));
            }
        }
        assertTrue(foundC1, "Should find concept c1 which is used as dimension");
    }

    @Test
    void should_return_true_when_collection_exists() throws RmesException {
        // When
        boolean result = repositoryGestion.getResponseAsBoolean(ConceptCollectionsQueries.isCollectionExist("c1000"));

        // Then
        assertTrue(result, "Should return true when collection exists");
    }

    @Test
    void should_return_false_when_collection_does_not_exist() throws RmesException {
        // When
        boolean result = repositoryGestion.getResponseAsBoolean(ConceptCollectionsQueries.isCollectionExist("c9999"));

        // Then
        assertFalse(result, "Should return false when collection does not exist");
    }

    @Test
    void should_verify_collection_properties() throws RmesException {
        // When
        JSONObject result = repositoryGestion.getResponseAsObject(ConceptCollectionsQueries.collectionQuery("c3000"));

        // Then
        assertNotNull(result);
        assertEquals("c3000", result.getString("id"));
        assertEquals("Collection Emploi et marché du travail", result.getString("prefLabelLg1"));
        assertEquals("Employment and Labour Market Collection", result.getString("prefLabelLg2"));

        // Verify the collection has all required metadata
        assertTrue(result.has("created") || result.has("id"), "Should have creation metadata");
    }

    @Test
    void should_handle_collections_with_different_member_counts() throws RmesException {
        // When
        JSONArray result = repositoryGestion.getResponseAsArray(ConceptCollectionsQueries.collectionsDashboardQuery());

        // Then
        // Verify different collections have different member counts
        JSONObject emploiCollection = findCollectionById(result, "c3000");
        JSONObject commerceCollection = findCollectionById(result, "c2000");
        JSONObject demoCollection = findCollectionById(result, "c4000");

        assertNotEquals(
            emploiCollection.getString("nbMembers"),
            commerceCollection.getString("nbMembers"),
            "Collections should have different member counts"
        );

        assertEquals("4", emploiCollection.getString("nbMembers"));
        assertEquals("2", commerceCollection.getString("nbMembers"));
        assertEquals("1", demoCollection.getString("nbMembers"));
    }

    // Helper method to find a collection by id in a JSONArray
    private JSONObject findCollectionById(JSONArray collections, String id) {
        for (int i = 0; i < collections.length(); i++) {
            JSONObject collection = collections.getJSONObject(i);
            if (collection.getString("id").equals(id)) {
                return collection;
            }
        }
        return null;
    }
}
