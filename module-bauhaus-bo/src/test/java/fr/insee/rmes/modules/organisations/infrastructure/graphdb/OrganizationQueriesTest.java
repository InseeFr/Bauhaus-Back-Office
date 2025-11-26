package fr.insee.rmes.modules.organisations.infrastructure.graphdb;

import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("integration")
class OrganizationQueriesTest extends WithGraphDBContainer {
    RepositoryGestion repositoryGestion = new RepositoryGestion(getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    @BeforeAll
    static void initData(){
        container.withTrigFiles("organizations.trig");
    }

    @Test
    void should_return_organization() throws Exception {
        OrganizationQueries.setConfig(new ConfigStub());
        JSONObject result = repositoryGestion.getResponseAsObject(OrganizationQueries.organizationQuery("HIE2000069"));
        assertNotNull(result.getString("labelLg1"));
        assertEquals("Direction régionale de Nouvelle-Aquitaine - siège de Poitiers (DR86-ETB86)", result.getString("labelLg1"));
    }

    @Test
    void should_return_organizations() throws Exception {
        OrganizationQueries.setConfig(new ConfigStub());
        JSONArray result = repositoryGestion.getResponseAsArray(OrganizationQueries.organizationsQuery());
        assertEquals(220, result.length());

        JSONObject hieOrg = null;
        for (int i = 0; i < result.length(); i++) {
            JSONObject obj = result.getJSONObject(i);
            if ("HIE2000069".equals(obj.getString("id"))) {
                hieOrg = obj;
                break;
            }
        }

        assertNotNull(hieOrg);
        assertNotNull(hieOrg.getString("iri"));
        assertEquals("HIE2000069", hieOrg.getString("id"));
        assertNotNull(hieOrg.getString("label"));

    }

    @Test
    void should_return_organizations_two_langs() throws Exception {
        OrganizationQueries.setConfig(new ConfigStub());
        JSONArray result = repositoryGestion.getResponseAsArray(OrganizationQueries.organizationsTwoLangsQuery());
        assertEquals(219, result.length());

        JSONObject hieOrg = null;
        for (int i = 0; i < result.length(); i++) {
            JSONObject obj = result.getJSONObject(i);
            if ("HIE2000069".equals(obj.getString("id"))) {
                hieOrg = obj;
                break;
            }
        }

        assertNotNull(hieOrg);
        assertEquals("HIE2000069", hieOrg.getString("id"));
        assertEquals("DR86-DIR", hieOrg.getString("identifier"));
        assertNotNull(hieOrg.getString("labelLg1"));

    }

    @Test
    void should_return_organization_uri() throws Exception {
        OrganizationQueries.setConfig(new ConfigStub());
        JSONObject result = repositoryGestion.getResponseAsObject(OrganizationQueries.getUriById("HIE2000069"));
        assertEquals("http://bauhaus/organisations/insee/HIE2000069", result.getString("uri"));
    }

    @Test
    void should_return_compact_organization() throws Exception {
        OrganizationQueries.setConfig(new ConfigStub());
        JSONObject result = repositoryGestion.getResponseAsObject(OrganizationQueries.generateCompactOrganisationQuery("HIE2000069"));

        assertNotNull(result);
        assertEquals("HIE2000069", result.getString("identifier"));
        assertNotNull(result.getString("label"));
        assertNotNull(result.getString("iri"));
        assertEquals("http://bauhaus/organisations/insee/HIE2000069", result.getString("iri"));
    }

    @Test
    void should_return_compact_organizations() throws Exception {
        OrganizationQueries.setConfig(new ConfigStub());
        List<String> identifiers = Arrays.asList("HIE2000069", "HIE2000070", "HIE2000071");
        JSONArray result = repositoryGestion.getResponseAsArray(OrganizationQueries.generateCompactOrganisationsQuery(identifiers));

        assertNotNull(result);
        assertTrue(result.length() >= 3, "Should return at least 3 organizations");

        // Verify that we got the expected organizations
        boolean foundHIE69 = false;
        boolean foundHIE70 = false;
        boolean foundHIE71 = false;

        for (int i = 0; i < result.length(); i++) {
            JSONObject org = result.getJSONObject(i);
            assertNotNull(org.getString("identifier"));
            assertNotNull(org.getString("label"));
            assertNotNull(org.getString("iri"));

            String id = org.getString("identifier");
            if ("HIE2000069".equals(id)) foundHIE69 = true;
            if ("HIE2000070".equals(id)) foundHIE70 = true;
            if ("HIE2000071".equals(id)) foundHIE71 = true;
        }

        assertTrue(foundHIE69, "Should find HIE2000069");
        assertTrue(foundHIE70, "Should find HIE2000070");
        assertTrue(foundHIE71, "Should find HIE2000071");
    }

    @Test
    void should_return_true_when_organization_exists() throws Exception {
        OrganizationQueries.setConfig(new ConfigStub());
        String iri = "http://bauhaus/organisations/insee/HIE2000069";
        boolean result = repositoryGestion.getResponseAsBoolean(OrganizationQueries.checkIfOrganisationExistsQuery(iri));

        assertTrue(result, "Organization with IRI " + iri + " should exist");
    }

    @Test
    void should_return_false_when_organization_does_not_exist() throws Exception {
        OrganizationQueries.setConfig(new ConfigStub());
        String iri = "http://bauhaus/organisations/insee/NON_EXISTENT";
        boolean result = repositoryGestion.getResponseAsBoolean(OrganizationQueries.checkIfOrganisationExistsQuery(iri));

        assertFalse(result, "Organization with IRI " + iri + " should not exist");
    }

}
