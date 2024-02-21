package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.bauhaus_services.rdf_utils.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.stamps.StampsRestrictionServiceImpl;
import fr.insee.rmes.config.auth.UserProvider;
import fr.insee.rmes.config.auth.user.AuthorizeMethodDecider;
import fr.insee.rmes.exceptions.RmesException;
import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class StampAuthorizationChecker extends StampsRestrictionServiceImpl {

	private static final Logger logger = LoggerFactory.getLogger(StampAuthorizationChecker.class);

	@Autowired
	public StampAuthorizationChecker(RepositoryGestion repoGestion, AuthorizeMethodDecider authorizeMethodDecider, UserProvider userProvider) {
		super(repoGestion, authorizeMethodDecider, userProvider);
	}

	public boolean isSeriesManagerWithStamp(String seriesId, String stamp) {
		try {
			return isSeriesManagerWithStamp(findIRI(requireNonNull(seriesId)), requireNonNull(stamp));
		} catch (RmesException e) {
			logger.error("Error while checking authorization for user with stamp {} to modify {}", stamp, seriesId);
			return false;
		}
	}

	public boolean isCodesListManagerWithStamp(String codesListId, String stamp) {
		try {
			return isCodesListManagerWithStamp(findCodesListIRI(requireNonNull(codesListId)), requireNonNull(stamp));
		} catch (RmesException e) {
			logger.error("Error while checking authorization for user with stamp {} to modify {}", stamp, codesListId);
			return false;
		}
	}

	private IRI findIRI(String seriesId) {
		return RdfUtils.objectIRI(ObjectType.SERIES, seriesId);
	}

	private IRI findCodesListIRI(String codesListId) {
		return RdfUtils.objectIRI(ObjectType.CODE_LIST, codesListId);
	}
}
