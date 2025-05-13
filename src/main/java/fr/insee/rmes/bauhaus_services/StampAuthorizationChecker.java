package fr.insee.rmes.bauhaus_services;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.stamps.StampsRestrictionServiceImpl;
import fr.insee.rmes.config.auth.UserProvider;
import fr.insee.rmes.config.auth.user.AuthorizeMethodDecider;
import fr.insee.rmes.config.auth.user.Stamp;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.persistance.sparql_queries.code_list.CodeListQueries;
import org.eclipse.rdf4j.model.IRI;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class StampAuthorizationChecker extends StampsRestrictionServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(StampAuthorizationChecker.class);
    public static final String CHECKING_AUTHORIZATION_ERROR_MESSAGE = "Error while checking authorization for user with stamp {} to modify or delete {}";

    @Autowired
    public StampAuthorizationChecker(RepositoryGestion repoGestion, AuthorizeMethodDecider authorizeMethodDecider, UserProvider userProvider) {
        super(repoGestion, authorizeMethodDecider, userProvider);
    }

    public boolean isCodesListManagerWithStamp(String codesListId, Stamp stamp) {
        try {
            return isCodesListManagerWithStamp(findCodesListIRI(requireNonNull(codesListId)), requireNonNull(stamp).stamp());
        } catch (RmesException e) {
            logger.error(CHECKING_AUTHORIZATION_ERROR_MESSAGE, stamp, codesListId);
            return false;
        }
    }


    public boolean isCodesListManagerWithStamp(IRI iri, String stamp) throws RmesException {
        return isManagerForModule(stamp, iri, CodeListQueries::getContributorsByCodesListUri, Constants.CONTRIBUTORS);
    }

    private IRI findCodesListIRI(String codesListId) throws RmesException {
        JSONObject codeList = repoGestion.getResponseAsObject(CodeListQueries.getCodeListIRIByNotation(codesListId));
        String uriString = codeList.getString("iri");
        return RdfUtils.createIRI(uriString);
    }

}

