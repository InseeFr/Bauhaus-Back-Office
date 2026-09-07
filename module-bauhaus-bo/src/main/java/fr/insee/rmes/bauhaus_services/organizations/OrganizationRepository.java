package fr.insee.rmes.bauhaus_services.organizations;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.OperationsObjectMapper;
import fr.insee.rmes.graphdb.QueryUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.IdGenerator;
import fr.insee.rmes.modules.commons.configuration.swagger.model.IdLabelTwoLangs;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.organisations.domain.OrganisationLabel;
import fr.insee.rmes.modules.organisations.infrastructure.graphdb.OrganizationQueries;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

@Repository
public class OrganizationRepository  extends RdfService {

	private final OperationsObjectMapper operationsObjectMapper;

	private final OrganizationQueries organizationQueries;

	public OrganizationRepository(RepositoryGestion repoGestion, IdGenerator idGenerator,
								  RepositoryPublication repositoryPublication,
								  PublicationUtils publicationUtils,
								  OperationsObjectMapper operationsObjectMapper, OrganizationQueries organizationQueries) {
		super(repoGestion, idGenerator, repositoryPublication, publicationUtils);
		this.operationsObjectMapper = operationsObjectMapper;
		this.organizationQueries = organizationQueries;
	}
	
	public String getUri(String code) throws RmesException{
		if (StringUtils.isEmpty(code) ) {return null;}
		JSONObject orga = repoGestion.getResponseAsObject(organizationQueries.getUriById(code));
		return QueryUtils.correctEmptyGroupConcat(orga.getString(Constants.URI));
	}

	public IdLabelTwoLangs buildOrganizationFromJson(JSONObject organizationJson) {
		return operationsObjectMapper.buildIdLabelTwoLangsFromJson(organizationJson);	
	}
	
	public JSONObject getOrganizationJson(String organizationIdentifier) throws RmesException {
		JSONObject orga = repoGestion.getResponseAsObject(organizationQueries.organizationQuery(organizationIdentifier));
		foldAcronymIntoLabels(orga);
		orga.put(Constants.ID, organizationIdentifier);
		return orga;

	}

	private static void foldAcronymIntoLabels(JSONObject orga) {
		String acronym = orga.optString("acronym", null);
		if (acronym == null || acronym.isBlank()) {
			return;
		}
		orga.put("labelLg1", OrganisationLabel.withAcronym(orga.optString("labelLg1", null), acronym));
		orga.put("labelLg2", OrganisationLabel.withAcronym(orga.optString("labelLg2", null), acronym));
		orga.remove("acronym");
	}
	
}
