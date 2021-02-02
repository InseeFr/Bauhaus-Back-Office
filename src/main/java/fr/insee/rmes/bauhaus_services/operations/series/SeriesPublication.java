package fr.insee.rmes.bauhaus_services.operations.series;

import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.RmesUnauthorizedException;
import fr.insee.rmes.external_services.notifications.NotificationsContract;
import fr.insee.rmes.external_services.notifications.RmesNotificationsImpl;

@Repository
public class SeriesPublication extends RdfService {
	
	@Autowired
	FamOpeSerIndUtils famOpeSerUtils;

	@Autowired
	private SeriesUtils seriesUtils;
	

	static NotificationsContract notification = new RmesNotificationsImpl();
	
	public void publishSeries(String seriesId) throws RmesException {
		Model model = new LinkedHashModel();
		Resource series = RdfUtils.seriesIRI(seriesId);
		JSONObject serieJson = seriesUtils.getSeriesJsonById(seriesId);
		String familyId = serieJson.getJSONObject(Constants.FAMILY).getString(Constants.ID);
		String status= famOpeSerUtils.getValidationStatus(familyId);
		
		if(PublicationUtils.isPublished(status)) {
			throw new RmesUnauthorizedException(
					ErrorCodes.SERIES_VALIDATION_UNPUBLISHED_FAMILY,
					"This Series cannot be published before its family is published", 
					"Series: "+seriesId+" ; Family: "+familyId);
		}
		
		RepositoryConnection con = repoGestion.getConnection();
		RepositoryResult<Statement> statements = repoGestion.getStatements(con, series);
		
		RepositoryResult<Statement> hasPartStatements = repoGestion.getHasPartStatements(con, series);
		
		try {	
			try {
				if (!statements.hasNext()) {
					throw new RmesNotFoundException(ErrorCodes.SERIES_UNKNOWN_ID,"Series not found", seriesId);
				}
				while (statements.hasNext()) {
					Statement st = statements.next();
					String pred = ((SimpleIRI) st.getPredicate()).toString();
					
					// Other URI to transform
					if (pred.endsWith("isPartOf") ||
							pred.endsWith("seeAlso") ||
							pred.endsWith("replaces") ||
							pred.endsWith("isReplacedBy")||
							pred.endsWith("dataCollector") || 
							pred.endsWith("contributor")  ||
							pred.endsWith("publisher")  ||
							pred.endsWith("accrualPeriodicity")||
							pred.endsWith("type")   ) {
						model.add(PublicationUtils.tranformBaseURIToPublish(st.getSubject()), 
								st.getPredicate(),
								PublicationUtils.tranformBaseURIToPublish((Resource) st.getObject()), 
								st.getContext());
					} else if (pred.endsWith("isValidated")
							|| pred.endsWith("validationState")
							|| pred.endsWith("hasPart")) {
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
				throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
			}
		
		} finally {
			repoGestion.closeStatements(statements);
			repoGestion.closeStatements(hasPartStatements);
		}
		Resource seriesToPublishRessource = PublicationUtils.tranformBaseURIToPublish(series);
		RepositoryPublication.publishResource(seriesToPublishRessource, model, "serie");
		
	}

}

