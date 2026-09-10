package fr.insee.rmes.modules.organisations.infrastructure.graphdb;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class OrganizationQueriesTest extends WithGraphDBContainer {
    RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    private OrganizationQueries organizationQueries;

    @BeforeAll
    static void initData() {
        container.withTrigFiles("organizations.trig");
    }

    @BeforeEach
    void setUp() {
        organizationQueries =
                new OrganizationQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());
    }

    @Test
    void should_return_organization() throws Exception {
        JSONObject result = repositoryGestion.getResponseAsObject(organizationQueries.organizationQuery("HIE2000069"));
        assertNotNull(result.getString("labelLg1"));
        assertEquals(
                "Direction régionale de Nouvelle-Aquitaine - siège de Poitiers (DR86-ETB86)",
                result.getString("labelLg1"));
    }

    @Test
    void should_return_organizations() throws Exception {
        JSONArray result = repositoryGestion.getResponseAsArray(organizationQueries.organizationsQuery());
        assertEquals(220, result.length());

        JSONObject hieOrg = JSONUtils.stream(result)
                .filter(obj -> "HIE2000069".equals(obj.getString("id")))
                .findFirst()
                .orElse(null);

        assertNotNull(hieOrg);
        assertNotNull(hieOrg.getString("iri"));
        assertEquals("HIE2000069", hieOrg.getString("id"));
        assertNotNull(hieOrg.getString("label"));
    }

    @Test
    void should_return_stamp_of_organizations() throws Exception {
        JSONArray result = repositoryGestion.getResponseAsArray(organizationQueries.organizationsQuery());

        JSONObject hieOrg = JSONUtils.stream(result)
                .filter(obj -> "HIE2000069".equals(obj.getString("id")))
                .findFirst()
                .orElse(null);

        assertNotNull(hieOrg);
        assertEquals("DR86-DIR", hieOrg.optString("stamp"));
    }

    @Test
    void should_return_organizations_two_langs() throws Exception {
        JSONArray result = repositoryGestion.getResponseAsArray(organizationQueries.organizationsTwoLangsQuery());
        assertEquals(219, result.length());

        JSONObject hieOrg = JSONUtils.stream(result)
                .filter(obj -> "HIE2000069".equals(obj.getString("id")))
                .findFirst()
                .orElse(null);

        assertNotNull(hieOrg);
        assertEquals("HIE2000069", hieOrg.getString("id"));
        assertEquals("DR86-DIR", hieOrg.getString("identifier"));
        assertNotNull(hieOrg.getString("labelLg1"));
    }

    @Test
    void should_return_organization_uri() throws Exception {
        JSONObject result = repositoryGestion.getResponseAsObject(organizationQueries.getUriById("HIE2000069"));
        assertEquals("http://bauhaus/organisations/insee/HIE2000069", result.getString("uri"));
    }

    @Test
    void should_return_compact_organization() throws Exception {
        JSONObject result = repositoryGestion.getResponseAsObject(
                organizationQueries.generateCompactOrganisationQuery("HIE2000069"));

        assertNotNull(result);
        assertEquals("HIE2000069", result.getString("identifier"));
        assertNotNull(result.getString("label"));
        assertNotNull(result.getString("iri"));
        assertEquals("http://bauhaus/organisations/insee/HIE2000069", result.getString("iri"));
    }

    @Test
    void should_return_compact_organizations() throws Exception {
        List<String> identifiers = Arrays.asList("HIE2000069", "HIE2000070", "HIE2000071");
        JSONArray result = repositoryGestion.getResponseAsArray(
                organizationQueries.generateCompactOrganisationsQuery(identifiers));

        assertNotNull(result);
        assertTrue(result.length() >= 3, "Should return at least 3 organizations");

        JSONUtils.stream(result).forEach(org -> {
            assertNotNull(org.getString("identifier"));
            assertNotNull(org.getString("label"));
            assertNotNull(org.getString("iri"));
        });

        List<String> identifiersFound =
                JSONUtils.stream(result).map(org -> org.getString("identifier")).toList();

        assertTrue(identifiersFound.contains("HIE2000069"), "Should find HIE2000069");
        assertTrue(identifiersFound.contains("HIE2000070"), "Should find HIE2000070");
        assertTrue(identifiersFound.contains("HIE2000071"), "Should find HIE2000071");
    }

    @Test
    void should_return_true_when_organization_exists() throws Exception {
        String iri = "http://bauhaus/organisations/insee/HIE2000069";
        boolean result =
                repositoryGestion.getResponseAsBoolean(organizationQueries.checkIfOrganisationExistsQuery(iri));

        assertTrue(result, "Organization with IRI " + iri + " should exist");
    }

    @Test
    void should_return_false_when_organization_does_not_exist() throws Exception {
        String iri = "http://bauhaus/organisations/insee/NON_EXISTENT";
        boolean result =
                repositoryGestion.getResponseAsBoolean(organizationQueries.checkIfOrganisationExistsQuery(iri));

        assertFalse(result, "Organization with IRI " + iri + " should not exist");
    }
}
