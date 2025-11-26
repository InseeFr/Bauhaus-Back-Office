package fr.insee.rmes.modules.organisations.infrastructure.graphdb;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.organisations.domain.exceptions.OrganisationFetchException;
import fr.insee.rmes.modules.organisations.domain.model.CompactOrganisation;
import fr.insee.rmes.modules.organisations.domain.port.serverside.OrganisationsRepository;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.Deserializer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class GraphDBOrganisationsRepository implements OrganisationsRepository {
    private final RepositoryGestion repositoryGestion;

    public GraphDBOrganisationsRepository(RepositoryGestion repositoryGestion) {
        this.repositoryGestion = repositoryGestion;
    }


    @Override
    public CompactOrganisation getCompactOrganisation(String id) throws OrganisationFetchException {
        try {
            var organisation = this.repositoryGestion.getResponseAsObject(OrganizationQueries.generateCompactOrganisationQuery(id));
            var graphDbOrganisation = Deserializer.deserializeJSONObject(organisation, GraphDbCompactOrganisation.class);
            return graphDbOrganisation.toDomain();

        } catch (Exception e) {
            throw new OrganisationFetchException();
        }
    }

    @Override
    public List<CompactOrganisation> getCompactOrganisations(List<String> ids) throws OrganisationFetchException {
        try {
            JSONArray organisations = this.repositoryGestion.getResponseAsArray(OrganizationQueries.generateCompactOrganisationsQuery(ids));
            List<CompactOrganisation> result = new ArrayList<>();

            for (int i = 0; i < organisations.length(); i++) {
                JSONObject orgJson = organisations.getJSONObject(i);
                GraphDbCompactOrganisation graphDbOrg = Deserializer.deserializeJSONObject(orgJson, GraphDbCompactOrganisation.class);
                result.add(graphDbOrg.toDomain());
            }

            return result;
        } catch (Exception e) {
            throw new OrganisationFetchException();
        }
    }

    @Override
    public boolean checkIfOrganisationExists(String iri) throws OrganisationFetchException {
        try {
            return this.repositoryGestion.getResponseAsBoolean(OrganizationQueries.checkIfOrganisationExistsQuery(iri));
        } catch (Exception e) {
            throw new OrganisationFetchException();
        }
    }
}
