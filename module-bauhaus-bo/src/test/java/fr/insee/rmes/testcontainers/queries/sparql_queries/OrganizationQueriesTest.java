package fr.insee.rmes.testcontainers.queries.sparql_queries;

import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.persistance.sparql_queries.organizations.OrganizationQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.queries.WithGraphDBContainer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

}
