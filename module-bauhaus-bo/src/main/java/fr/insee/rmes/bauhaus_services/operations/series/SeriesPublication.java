package fr.insee.rmes.bauhaus_services.operations.series;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.operations.OperationsParentRepository;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.Set;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

@Repository
public class SeriesPublication {

    /** dcterms:subject : les thèmes pointent, comme dcat:theme des jeux de données, vers le concept publié. */
    private static final String THEME = "subject";

    private static final Set<String> URI_PREDICATES_TO_TRANSFORM = Set.of(
            Constants.ISPARTOF,
            Constants.SEEALSO,
            Constants.REPLACES,
            Constants.ISREPLACEDBY,
            Constants.DATA_COLLECTOR,
            Constants.CREATOR,
            Constants.CONTRIBUTOR,
            Constants.PUBLISHER,
            Constants.ACCRUAL_PERIODICITY,
            Constants.TYPE,
            THEME);

    private static final Set<String> PREDICATES_TO_IGNORE =
            Set.of(Constants.ISVALIDATED, Constants.VALIDATION_STATE, Constants.HAS_PART);

    private final OperationsParentRepository operationsParentRepository;
    private final PublicationUtils publicationUtils;
    private final RepositoryGestion repoGestion;
    private final RepositoryPublication repositoryPublication;
    private final OperationSeriesQueries operationSeriesQueries;

    public SeriesPublication(
            OperationsParentRepository operationsParentRepository,
            PublicationUtils publicationUtils,
            RepositoryGestion repoGestion,
            RepositoryPublication repositoryPublication,
            OperationSeriesQueries operationSeriesQueries) {
        this.operationsParentRepository = operationsParentRepository;
        this.publicationUtils = publicationUtils;
        this.repoGestion = repoGestion;
        this.repositoryPublication = repositoryPublication;
        this.operationSeriesQueries = operationSeriesQueries;
    }

    private static void checkIfSeriesExist(String id, RepositoryResult<Statement> statements)
            throws RmesNotFoundException {
        if (!statements.hasNext()) {
            throw new RmesNotFoundException(ErrorCodes.SERIES_UNKNOWN_ID, "Series not found", id);
        }
    }

    public void publishSeries(String id, JSONObject series, Resource graph) throws RmesException {
        PublicationUtils.rejectIfAlreadyPublished(
                "Series", id, operationsParentRepository.getFamOpSerValidationStatus(id));

        String familyId = series.getJSONObject(Constants.FAMILY).getString(Constants.ID);
        String status = operationsParentRepository.getValidationStatus(familyId);

        if (PublicationUtils.isUnublished(status)) {
            throw new RmesBadRequestException(
                    ErrorCodes.SERIES_VALIDATION_UNPUBLISHED_FAMILY,
                    "This Series cannot be published before its family is published",
                    "Series: " + id + " ; Family: " + familyId);
        }

        Model model = new LinkedHashModel();
        Resource resource = RdfUtils.seriesIRI(id);

        try (RepositoryConnection con = repoGestion.getConnection();
                RepositoryResult<Statement> statements = repoGestion.getStatements(con, resource, graph);
                RepositoryResult<Statement> hasPartStatements =
                        repoGestion.getStatementsPredicateObject(con, DCTERMS.HAS_PART, resource, graph);
                RepositoryResult<Statement> replacesStatements =
                        repoGestion.getStatementsPredicateObject(con, DCTERMS.REPLACES, resource, graph);
                RepositoryResult<Statement> isReplacedByStatements =
                        repoGestion.getStatementsPredicateObject(con, DCTERMS.IS_REPLACED_BY, resource, graph)) {
            checkIfSeriesExist(id, statements);
            readAllTriplets(
                    statements, model, hasPartStatements, replacesStatements, isReplacedByStatements, resource, graph);
        }
        Resource seriesToPublishResource = publicationUtils.tranformBaseURIToPublish(resource);
        repositoryPublication.publishResource(seriesToPublishResource, model, "serie", graph);
    }

    private void readAllTriplets(
            RepositoryResult<Statement> statements,
            Model model,
            RepositoryResult<Statement> hasPartStatements,
            RepositoryResult<Statement> replacesStatements,
            RepositoryResult<Statement> isReplacedByStatements,
            Resource resource,
            Resource graph)
            throws RmesException {
        while (statements.hasNext()) {
            Statement st = statements.next();
            String predicate = RdfUtils.toString(st.getPredicate());

            if (URI_PREDICATES_TO_TRANSFORM.stream().anyMatch(predicate::endsWith)) {
                transformSubjectAndObject(model, st, graph);
            } else if (PREDICATES_TO_IGNORE.stream().noneMatch(predicate::endsWith)) {
                model.add(
                        publicationUtils.tranformBaseURIToPublish(st.getSubject()),
                        st.getPredicate(),
                        st.getObject(),
                        graph);
            }
            addStatementsToModel(model, hasPartStatements, graph);
            addStatementsToModel(model, replacesStatements, graph);
            addStatementsToModel(model, isReplacedByStatements, graph);
        }

        /*
         We have to query all published operations linked to this series and publish all of them
        */
        addOperationsWhoHavePartWithToModel(resource, model, graph);
    }

    private void addOperationsWhoHavePartWithToModel(Resource resource, Model model, Resource graph)
            throws RmesException {
        JSONArray operations = repoGestion.getResponseAsArray(
                operationSeriesQueries.getPublishedOperationsForSeries(resource.toString()));
        JSONUtils.stream(operations)
                .map(operation -> operation.getString("operation"))
                .forEach(iri -> model.add(
                        publicationUtils.tranformBaseURIToPublish(resource),
                        DCTERMS.HAS_PART,
                        publicationUtils.tranformBaseURIToPublish(RdfUtils.createIRI(iri)),
                        graph));
    }

    public void addStatementsToModel(Model model, RepositoryResult<Statement> statements, Resource graph) {
        while (statements.hasNext()) {
            Statement statement = statements.next();
            transformSubjectAndObject(model, statement, graph);
        }
    }

    public void transformSubjectAndObject(Model model, Statement statement, Resource graph) {
        model.add(
                publicationUtils.tranformBaseURIToPublish(statement.getSubject()),
                statement.getPredicate(),
                publicationUtils.tranformBaseURIToPublish((Resource) statement.getObject()),
                graph);
    }
}
