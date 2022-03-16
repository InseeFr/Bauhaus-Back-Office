package fr.insee.rmes.bauhaus_services.operations.series;

import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
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
	ParentUtils ownersUtils;
	

	static NotificationsContract notification = new RmesNotificationsImpl();
	
	public void publishSeries(String seriesId, JSONObject serieJson) throws RmesException {
		Model model = new LinkedHashModel();
		Resource series = RdfUtils.seriesIRI(seriesId);
		String familyId = serieJson.getJSONObject(Constants.FAMILY).getString(Constants.ID);
		String status= ownersUtils.getValidationStatus(familyId);
		
		if(PublicationUtils.isPublished(status)) {
			throw new RmesUnauthorizedException(
					ErrorCodes.SERIES_VALIDATION_UNPUBLISHED_FAMILY,
					"This Series cannot be published before its family is published", 
					"Series: "+seriesId+" ; Family: "+familyId);
		}
		
		RepositoryConnection con = repoGestion.getConnection();
		RepositoryResult<Statement> statements = repoGestion.getStatements(con, series);
		
		RepositoryResult<Statement> hasPartStatements = repoGestion.getHasPartStatements(con, series);
		RepositoryResult<Statement> replacesStatements = repoGestion.getReplacesStatements(con, series);
		RepositoryResult<Statement> isReplacedByStatements = repoGestion.getIsReplacedByStatements(con, series);
		
		try {	
			try {
				if (!statements.hasNext()) {
					throw new RmesNotFoundException(ErrorCodes.SERIES_UNKNOWN_ID,"Series not found", seriesId);
				}
				while (statements.hasNext()) {
					Statement st = statements.next();
					String pred = RdfUtils.toString(st.getPredicate());
					
					// Other URI to transform
					if (pred.endsWith("isPartOf") ||
							pred.endsWith(Constants.SEEALSO) ||
							pred.endsWith(Constants.REPLACES) ||
							pred.endsWith(Constants.ISREPLACEDBY)||
							pred.endsWith(Constants.DATA_COLLECTOR) || 
							pred.endsWith(Constants.CONTRIBUTOR)  ||
							pred.endsWith(Constants.PUBLISHER)  ||
							pred.endsWith("accrualPeriodicity")||
							pred.endsWith("type")   ) {
						transformSubjectAndObject(model, st);
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
					addStatementsToModel(model, hasPartStatements);
					addStatementsToModel(model, replacesStatements);
					addStatementsToModel(model, isReplacedByStatements);
					
				}
			} catch (RepositoryException e) {
				throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
			}
		
		} finally {
			repoGestion.closeStatements(statements);
			repoGestion.closeStatements(hasPartStatements);
			con.close();
		}
		Resource seriesToPublishRessource = PublicationUtils.tranformBaseURIToPublish(series);
		RepositoryPublication.publishResource(seriesToPublishRessource, model, "serie");
		
	}

	public void addStatementsToModel(Model model, RepositoryResult<Statement> statements) {
		while (statements.hasNext()) {
			Statement statement = statements.next();
			transformSubjectAndObject(model, statement);
		}
	}

	public void transformSubjectAndObject(Model model, Statement statement) {
		model.add(PublicationUtils.tranformBaseURIToPublish(statement.getSubject()), 
				statement.getPredicate(), 
				PublicationUtils.tranformBaseURIToPublish((Resource) statement.getObject()),
				statement.getContext());
	}

}

