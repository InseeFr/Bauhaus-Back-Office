package fr.insee.rmes.persistance.service.sesame.operations.series;

import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.persistance.notifications.NotificationsContract;
import fr.insee.rmes.persistance.notifications.RmesNotificationsImpl;
import fr.insee.rmes.persistance.service.sesame.ontologies.INSEE;
import fr.insee.rmes.persistance.service.sesame.operations.famOpeSerUtils.FamOpeSerUtils;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryGestion;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryPublication;
import fr.insee.rmes.persistance.service.sesame.utils.RepositoryUtils;
import fr.insee.rmes.persistance.service.sesame.utils.SesameUtils;
import fr.insee.rmes.persistance.service.sesame.utils.ValidationStatus;

public class SeriesPublication {

	static NotificationsContract notification = new RmesNotificationsImpl();
	
	public static void publishSeries(String seriesId) throws RmesException {
		SeriesUtils seriesUtils= new SeriesUtils();
		Model model = new LinkedHashModel();
		Resource series = SesameUtils.seriesIRI(seriesId);
		JSONObject serieJson = seriesUtils.getSeriesById(seriesId);
		String familyId = serieJson.getJSONObject("family").getString("id");
		String status=FamOpeSerUtils.getValidationStatus(familyId);
		
		if(status.equals(ValidationStatus.UNPUBLISHED.getValue()) | status.equals("UNDEFINED")) {
			throw new RmesUnauthorizedException(
					ErrorCodes.SERIES_VALIDATION_UNPUBLISHED_FAMILY,
					"This Series cannot be published before its family is published", 
					"Series: "+seriesId+" ; Family: "+familyId);
		}
		
		RepositoryConnection con = RepositoryUtils.getConnection(RepositoryGestion.REPOSITORY_GESTION);
		RepositoryResult<Statement> statements = RepositoryGestion.getStatements(con, series);
		
		RepositoryResult<Statement> hasPartStatements = RepositoryGestion.getHasPartStatements(con, series);
		
		try {	
			try {
				if (!statements.hasNext()) throw new RmesNotFoundException(ErrorCodes.SERIES_UNKNOWN_ID,"Series not found", seriesId);
				while (statements.hasNext()) {
					Statement st = statements.next();
					// Other URI to transform
					if (st.getPredicate().toString().endsWith("isPartOf") |
							st.getPredicate().toString().endsWith("seeAlso") |
							st.getPredicate().toString().endsWith("replaces") |
							st.getPredicate().toString().endsWith("isReplacedBy") ) {
						model.add(FamOpeSerUtils.tranformBaseURIToPublish(st.getSubject()), 
								st.getPredicate(),
								FamOpeSerUtils.tranformBaseURIToPublish((Resource) st.getObject()), 
								st.getContext());
					} else if (st.getPredicate().toString().endsWith("isValidated")
							|| st.getPredicate().toString().endsWith("validationState")
							|| st.getPredicate().toString().endsWith("hasPart")
							|| st.getPredicate().toString().endsWith("creator")
							|| st.getPredicate().toString().endsWith("contributor")) {
						// nothing, wouldn't copy this attr
					}
					// Literals
					else {
						model.add(FamOpeSerUtils.tranformBaseURIToPublish(st.getSubject()), 
								st.getPredicate(), 
								st.getObject(),
								st.getContext()
								);
					}
					while (hasPartStatements.hasNext()) {
						Statement hpst = hasPartStatements.next();
						model.add(FamOpeSerUtils.tranformBaseURIToPublish(hpst.getSubject()), 
								hpst.getPredicate(), 
								FamOpeSerUtils.tranformBaseURIToPublish((Resource) hpst.getObject()),
								hpst.getContext());
					}
					
				}
			} catch (RepositoryException e) {
				throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Config.REPOSITORY_EXCEPTION);
			}
		
		} finally {
			RepositoryGestion.closeStatements(statements);
			RepositoryGestion.closeStatements(hasPartStatements);
		}
		Resource seriesToPublishRessource = FamOpeSerUtils.tranformBaseURIToPublish(series);
		RepositoryPublication.publishSeries(seriesToPublishRessource, model);
		
	}

}

