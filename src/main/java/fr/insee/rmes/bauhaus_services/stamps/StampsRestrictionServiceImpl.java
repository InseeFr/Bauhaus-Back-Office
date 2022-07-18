package fr.insee.rmes.bauhaus_services.stamps;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;
import fr.insee.rmes.config.auth.user.AuthorizeMethodDecider;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.concepts.ConceptsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.indicators.IndicatorsQueries;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;


@Service
public class StampsRestrictionServiceImpl implements StampsRestrictionsService {
	
	@Autowired
	protected RepositoryGestion repoGestion;
	
	static final Logger logger = LogManager.getLogger(StampsRestrictionServiceImpl.class);

	@Autowired
	Config config;
	

	
	@Autowired
	private AuthorizeMethodDecider authorizeMethodDecider;

	@Override
	public boolean isConceptOrCollectionOwner(IRI uri) throws RmesException {
		User user = getUser();
		if (authorizeMethodDecider.isAdmin(user)) {
			return true;
		}
		String uriAsString = "<" + RdfUtils.toString(uri) + ">";
		JSONObject owner = repoGestion.getResponseAsObject(ConceptsQueries.getOwner(uriAsString));
		Boolean isConceptOwner = true;
		if (!owner.getString(Constants.OWNER).equals(user.getStamp())) {
			isConceptOwner = false;
		}
		return isConceptOwner;
	}

	@Override
	public boolean isConceptsOrCollectionsOwner(List<IRI> uris) throws RmesException {
		User user = getUser();
		if (authorizeMethodDecider.isAdmin(user)) {
			return true;
		}
		StringBuilder sb = new StringBuilder();
		uris.forEach(u -> sb.append("<" + RdfUtils.toString(u) + "> "));
		String uriAsString = sb.toString();
		JSONArray owners = repoGestion.getResponseAsArray(ConceptsQueries.getOwner(uriAsString));
		Boolean isConceptsOwner = true;
		for (int i = 0; i < owners.length(); i++) {
			if (!owners.getJSONObject(i).getString(Constants.OWNER).equals(user.getStamp())) {
				isConceptsOwner = false;
			}
		}
		return isConceptsOwner;
	}





	public void setFakeUser(String user) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
				DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JSONObject userObject = new JSONObject(user);

		JSONArray roles = userObject.getJSONArray("roles");

		new User("fakeUser",roles, userObject.getString(Constants.STAMP));
	}


	private boolean isConceptOwner(List<IRI> uris) throws RmesException {
		User user = getUser();
		StringBuilder sb = new StringBuilder();
		uris.forEach(u -> sb.append("<" + RdfUtils.toString(u) + "> "));
		String uriAsString = sb.toString();
		JSONArray owners = repoGestion.getResponseAsArray(ConceptsQueries.getOwner(uriAsString));
		Boolean isConceptOwner = true;
		for (int i = 0; i < owners.length(); i++) {
			if (!owners.getJSONObject(i).getString(Constants.OWNER).equals(user.getStamp())) {
				isConceptOwner = false;
			}
		}
		return isConceptOwner;
	}

	private boolean isConceptManager(List<IRI> uris) throws RmesException {
		User user = getUser();
		StringBuilder sb = new StringBuilder();
		uris.forEach(u -> sb.append("<" + RdfUtils.toString(u) + "> "));
		String uriAsString = sb.toString();
		JSONArray managers = repoGestion.getResponseAsArray(ConceptsQueries.getManager(uriAsString));
		Boolean isConceptManager = true;
		for (int i = 0; i < managers.length(); i++) {
			if (!managers.getJSONObject(i).getString(Constants.MANAGER).equals(user.getStamp())) {
				isConceptManager = false;
			}
		}
		return isConceptManager;
	}

	public boolean isSeriesManager(IRI uri) throws RmesException {
		User user = getUser();
		JSONArray managers = repoGestion.getResponseAsArray(OpSeriesQueries.getCreatorsBySeriesUri(RdfUtils.toString(uri)));
		Boolean isSeriesManager = false;
		for (int i = 0; i < managers.length(); i++) {
			if (managers.getJSONObject(i).getString(Constants.CREATORS).equals(user.getStamp())) {
				isSeriesManager = true;
			}
		}
		return isSeriesManager;
	}

	private boolean isIndicatorManager(IRI iri) throws RmesException {
		User user = getUser();
		String uriAsString = "<" + RdfUtils.toString(iri) + "> ";
		JSONArray creators = repoGestion.getResponseAsArray(IndicatorsQueries.getCreatorsByIndicatorUri(uriAsString));
		Boolean isIndicatorManager = false;
		if (creators.length() > 0) {
			for (int i = 0; i < creators.length(); i++) {
				if (creators.getJSONObject(i).getString(Constants.CREATORS).equals(user.getStamp())) {
					isIndicatorManager = true;
				}
			}
		}
		return isIndicatorManager;
	}

	private boolean canModifyOrValidateIndicator(IRI iri) throws RmesException {
		User user = getUser();
		return (authorizeMethodDecider.isAdmin(user) || (isIndicatorManager(iri) && authorizeMethodDecider.isIndicatorContributor(user)));
	}

	@Override
	public boolean canModifyIndicator(IRI uri) throws RmesException {
		return canModifyOrValidateIndicator(uri);
	}

	@Override
	public boolean canValidateIndicator(IRI uri) throws RmesException {
		return canModifyOrValidateIndicator(uri);
	}

	@Override
	public boolean canModifySims(IRI seriesOrIndicatorUri) throws RmesException {
		User user = getUser();
		return (authorizeMethodDecider.isAdmin(user) || authorizeMethodDecider.isCnis(user) || (isSeriesManager(seriesOrIndicatorUri) && authorizeMethodDecider.isSeriesContributor(user))
				|| (isIndicatorManager(seriesOrIndicatorUri) && authorizeMethodDecider.isIndicatorContributor(user)));
	}

	@Override
	public boolean canCreateConcept() throws RmesException {
		User user = getUser();
		return (authorizeMethodDecider.isAdmin(user) || authorizeMethodDecider.isConceptsContributor(user));
	}

	@Override
	public boolean canCreateFamily() throws RmesException {
		return canCreateFamilySeriesOrIndicator();
	}

	private boolean canCreateFamilySeriesOrIndicator() {
		return authorizeMethodDecider.isAdmin();
	}

	@Override
	public boolean canCreateSeries() throws RmesException {
		return canCreateFamilySeriesOrIndicator();
	}

	@Override
	public boolean canCreateIndicator() throws RmesException {
		return canCreateFamilySeriesOrIndicator();
	}

	@Override
	public boolean canCreateOperation(IRI seriesURI) throws RmesException {
		User user = getUser();
		return (authorizeMethodDecider.isAdmin(user) || (isSeriesManager(seriesURI) && authorizeMethodDecider.isSeriesContributor(user)));
	}

	@Override
	public boolean canCreateSims(IRI seriesOrIndicatorUri) throws RmesException {
		User user = getUser();
		return (authorizeMethodDecider.isAdmin(user) || (isSeriesManager(seriesOrIndicatorUri) && authorizeMethodDecider.isSeriesContributor(user))
				|| (isIndicatorManager(seriesOrIndicatorUri) && authorizeMethodDecider.isIndicatorContributor(user)));
	}

	@Override
	public boolean canDeleteSims() throws RmesException {
		return authorizeMethodDecider.isAdmin();
	}
	
	@Override
	public boolean canModifyConcept(IRI uri) throws RmesException {
		User user = getUser();
		List<IRI> uris = new ArrayList<>();
		uris.add(uri);
		return (authorizeMethodDecider.isAdmin(user) || authorizeMethodDecider.isConceptsContributor(user) || (isConceptManager(uris) && authorizeMethodDecider.isConceptContributor(user))
				|| (isConceptOwner(uris)) && authorizeMethodDecider.isConceptCreator(user));
	}

	@Override
	public boolean canModifySeries(IRI uri) throws RmesException {
		User user = getUser();
		return ((isSeriesManager(uri) && authorizeMethodDecider.isSeriesContributor(user)) || authorizeMethodDecider.isAdmin(user) || authorizeMethodDecider.isCnis(user) );
	}

	@Override
	public boolean canModifyOperation(IRI seriesURI) throws RmesException {
		return canModifySeries(seriesURI);
	}

	@Override
	public boolean canValidateSeries(IRI uri) throws RmesException {
		User user = getUser();
		return (authorizeMethodDecider.isAdmin(user) || (isSeriesManager(uri) && authorizeMethodDecider.isSeriesContributor(user)));
	}

	@Override
	public boolean canValidateOperation(IRI seriesURI) throws RmesException {
		return canValidateSeries(seriesURI);
	}

	@Override
	public boolean canManageDocumentsAndLinks() throws RmesException {
		User user = getUser();
		return (authorizeMethodDecider.isAdmin(user) || authorizeMethodDecider.isSeriesContributor(user) || authorizeMethodDecider.isIndicatorContributor(user));
	}

	@Override
	public boolean canValidateClassification(IRI uri) throws RmesException {
		return authorizeMethodDecider.isAdmin();
	}

	@Override
	public User getUser() throws RmesException {
		return authorizeMethodDecider.getUser();
	}

	@Override
	public boolean isAdmin() {
		return authorizeMethodDecider.isAdmin();
	}

	

}
