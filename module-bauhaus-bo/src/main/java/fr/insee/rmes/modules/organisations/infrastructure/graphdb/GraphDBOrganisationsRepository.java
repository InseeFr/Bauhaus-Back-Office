package fr.insee.rmes.modules.organisations.infrastructure.graphdb;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.ADMS;
import fr.insee.rmes.modules.organisations.domain.exceptions.OrganisationFetchException;
import fr.insee.rmes.modules.organisations.domain.model.CompactOrganisation;
import fr.insee.rmes.modules.organisations.domain.port.serverside.OrganisationsRepository;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.Deserializer;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @Override
    public Optional<String> getDctermsIdentifier(String admsIdentifier) throws OrganisationFetchException {
        try {
            return getOrganisationIdentifier(OrganizationQueries.getOrganizationIdenfier(ADMS.HAS_IDENTIFIER, admsIdentifier, DCTERMS.IDENTIFIER));
        } catch (RmesException e) {
            throw new OrganisationFetchException();
        }
    }

    private @NonNull Optional<String> getOrganisationIdentifier(String query) throws OrganisationFetchException {
        try {
            var organisation = this.repositoryGestion.getResponseAsObject(query);
            if(organisation.isEmpty()){
                return Optional.empty();
            }

            return Optional.of(organisation.getString("value"));
        } catch (RmesException e) {
            throw new OrganisationFetchException();
        }
    }

    @Override
    public Optional<String> getAdmsIdentifier(String dctermsIdentifier) throws OrganisationFetchException {
        try {
            return getOrganisationIdentifier(OrganizationQueries.getOrganizationIdenfier(DCTERMS.IDENTIFIER, dctermsIdentifier, ADMS.HAS_IDENTIFIER));
        } catch (RmesException e) {
            throw new OrganisationFetchException();
        }
    }
}
