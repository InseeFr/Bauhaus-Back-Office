package fr.insee.rmes.bauhaus_services.operations.series;

import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.famOpeSerUtils.FamOpeSerUtils;
import fr.insee.rmes.bauhaus_services.rdfUtils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdfUtils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdfUtils.RepositoryPublication;
import fr.insee.rmes.bauhaus_services.rdfUtils.RdfUtils;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.external_services.notifications.NotificationsContract;
import fr.insee.rmes.external_services.notifications.RmesNotificationsImpl;

public class SeriesPublication {
	
	@Autowired
	static 	FamOpeSerUtils famOpeSerUtils;

	static NotificationsContract notification = new RmesNotificationsImpl();
	
	public static void publishSeries(String seriesId) throws RmesException {
		SeriesUtils seriesUtils= new SeriesUtils();
		Model model = new LinkedHashModel();
		Resource series = RdfUtils.seriesIRI(seriesId);
		JSONObject serieJson = seriesUtils.getSeriesById(seriesId);
		String familyId = serieJson.getJSONObject("family").getString(Constants.ID);
		String status= famOpeSerUtils.getValidationStatus(familyId);
		
		if(PublicationUtils.isPublished(status)) {
			throw new RmesUnauthorizedException(
					ErrorCodes.SERIES_VALIDATION_UNPUBLISHED_FAMILY,
					"This Series cannot be published before its family is published", 
					"Series: "+seriesId+" ; Family: "+familyId);
		}
		
		RepositoryConnection con = PublicationUtils.getRepositoryConnectionGestion();
		RepositoryResult<Statement> statements = RepositoryGestion.getStatements(con, series);
		
		RepositoryResult<Statement> hasPartStatements = RepositoryGestion.getHasPartStatements(con, series);
		
		try {	
			try {
				if (!statements.hasNext()) {
					throw new RmesNotFoundException(ErrorCodes.SERIES_UNKNOWN_ID,"Series not found", seriesId);
				}
				while (statements.hasNext()) {
					Statement st = statements.next();
					// Other URI to transform
					if (st.getPredicate().toString().endsWith("isPartOf") ||
							st.getPredicate().toString().endsWith("seeAlso") ||
							st.getPredicate().toString().endsWith("replaces") ||
							st.getPredicate().toString().endsWith("isReplacedBy") ) {
						model.add(PublicationUtils.tranformBaseURIToPublish(st.getSubject()), 
								st.getPredicate(),
								PublicationUtils.tranformBaseURIToPublish((Resource) st.getObject()), 
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
						model.add(PublicationUtils.tranformBaseURIToPublish(st.getSubject()), 
								st.getPredicate(), 
								st.getObject(),
								st.getContext()
								);
					}
					while (hasPartStatements.hasNext()) {
						Statement hpst = hasPartStatements.next();
						model.add(PublicationUtils.tranformBaseURIToPublish(hpst.getSubject()), 
								hpst.getPredicate(), 
								PublicationUtils.tranformBaseURIToPublish((Resource) hpst.getObject()),
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
		Resource seriesToPublishRessource = PublicationUtils.tranformBaseURIToPublish(series);
		RepositoryPublication.publishResource(seriesToPublishRessource, model, "serie");
		
	}

}

