package fr.insee.rmes.bauhaus_services.organizations;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.OperationsObjectMapper;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.QueryUtils;
import fr.insee.rmes.modules.commons.configuration.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.modules.organisations.domain.model.Organization;
import fr.insee.rmes.modules.organisations.infrastructure.graphdb.OrganizationQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.IdGenerator;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class OrganizationsServiceImpl extends RdfService implements OrganizationsService {

    private final OrganizationRepository organizationRepository;

    private final OperationsObjectMapper operationsObjectMapper;

    private final OrganizationQueries organizationQueries;

    public OrganizationsServiceImpl(
            RepositoryGestion repoGestion,
            IdGenerator idGenerator,
            RepositoryPublication repositoryPublication,
            PublicationUtils publicationUtils,
            OrganizationRepository organizationRepository,
            OperationsObjectMapper operationsObjectMapper,
            OrganizationQueries organizationQueries) {
        super(repoGestion, idGenerator, repositoryPublication, publicationUtils);
        this.organizationRepository = organizationRepository;
        this.operationsObjectMapper = operationsObjectMapper;
        this.organizationQueries = organizationQueries;
    }

    @Override
    public String getOrganizationJsonString(String organizationIdentifier) throws RmesException {
        JSONObject orgaJson = organizationRepository.getOrganizationJson(organizationIdentifier);
        return QueryUtils.correctEmptyGroupConcat(orgaJson.toString());
    }

    @Override
    public IdLabelTwoLangs getOrganization(String organizationIdentifier) throws RmesException {
        return organizationRepository.buildOrganizationFromJson(
                organizationRepository.getOrganizationJson(organizationIdentifier));
    }

    @Override
    public String getOrganizationUriById(String organizationIdentifier) throws RmesException {
        if (StringUtils.isEmpty(organizationIdentifier)) {
            return null;
        }
        JSONObject orga = repoGestion.getResponseAsObject(organizationQueries.getUriById(organizationIdentifier));
        if (!orga.has(Constants.URI)) {
            return null;
        }
        return QueryUtils.correctEmptyGroupConcat(orga.getString(Constants.URI));
    }

    @Override
    public List<Organization> getOrganizations() throws RmesException {
        JSONArray orgsJson = repoGestion.getResponseAsArray(organizationQueries.organizationsTwoLangsQuery());
        List<Object> objects =
                operationsObjectMapper.buildObjectListFromJson(orgsJson, Organization.getClassOperationsLink());
        List<Organization> result = new ArrayList<>();
        for (Object o : objects) {
            result.add((Organization) o);
        }
        return result;
    }
}
