package fr.insee.rmes.bauhaus_services.classifications;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.IdGenerator;
import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ClassificationPublication extends RdfService {

    private static final Logger logger = LoggerFactory.getLogger(ClassificationPublication.class);

    public ClassificationPublication(
            RepositoryGestion repoGestion,
            IdGenerator idGenerator,
            RepositoryPublication repositoryPublication,
            PublicationUtils publicationUtils) {
        super(repoGestion, idGenerator, repositoryPublication, publicationUtils);
    }

    String[] ignoredAttrs = {"isValidated", "validationState", "conceptVersion"};

    public void publishClassification(Resource graphIri) throws RmesException {
        Model model = new LinkedHashModel();

        logger.debug("publishClassification - reading management graph [{}]", graphIri);
        try (RepositoryConnection con = repoGestion.getConnection();
                RepositoryResult<Statement> classifStatements = repoGestion.getCompleteGraph(con, graphIri)) {
            if (!classifStatements.hasNext()) {
                logger.debug("publishClassification - no triple found in graph [{}]", graphIri);
                throw new RmesNotFoundException(
                        ErrorCodes.CLASSIFICATION_UNKNOWN_ID, "Classification not found", graphIri.stringValue());
            }
            transformStatementToPublish(model, classifStatements);
        } catch (RepositoryException e) {
            logger.debug("publishClassification - failed to read graph [{}]: {}", graphIri, e.getMessage());
            throw new RmesException(
                    HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
        }

        logger.debug(
                "publishClassification - sending {} triples to the publication repository for graph [{}]",
                model.size(),
                graphIri);
        repositoryPublication.publishContext(graphIri, model, "classification");
        logger.debug("publishClassification - publishContext completed for graph [{}]", graphIri);
    }

    public void transformStatementToPublish(Model model, RepositoryResult<Statement> classifStatements) {
        int read = 0;
        int ignored = 0;
        while (classifStatements.hasNext()) {
            Statement st = classifStatements.next();
            read++;
            // Triplets that don't get published
            String predicate = RdfUtils.toString(st.getPredicate());
            if (!isTripletForPublication(predicate)) {
                // nothing, wouldn't copy this attr
                ignored++;
                logger.debug("transformStatementToPublish - triple skipped (predicate not published): {}", predicate);
            } else {
                transformTripleToPublish(model, st);
            }
        }
        logger.debug(
                "transformStatementToPublish - {} triples read, {} skipped, {} kept for publication",
                read,
                ignored,
                model.size());
    }

    private boolean isTripletForPublication(String predicate) {
        return !PublicationUtils.stringEndsWithItemFromList(predicate, ignoredAttrs);
    }
}
