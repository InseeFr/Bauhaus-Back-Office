package fr.insee.rmes.bauhaus_services.operations.series;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.graphdb.ontologies.DCTERMS;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import fr.insee.rmes.utils.JSONUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public class SeriesPublication {

    private static final Set<String> URI_PREDICATES_TO_TRANSFORM = Set.of(
        Constants.ISPARTOF,
        Constants.SEEALSO,
        Constants.REPLACES,
        Constants.ISREPLACEDBY,
        Constants.DATA_COLLECTOR,
        Constants.CONTRIBUTOR,
        Constants.PUBLISHER,
        Constants.ACCRUAL_PERIODICITY,
        Constants.TYPE
    );

    private static final Set<String> PREDICATES_TO_IGNORE = Set.of(
        Constants.ISVALIDATED,
        Constants.VALIDATION_STATE,
        Constants.HAS_PART
    );

    private final ParentUtils ownersUtils;
    private final PublicationUtils publicationUtils;
    private final RepositoryGestion repoGestion;
    private final RepositoryPublication repositoryPublication;

    public SeriesPublication(ParentUtils ownersUtils, PublicationUtils publicationUtils, RepositoryGestion repoGestion, RepositoryPublication repositoryPublication) {
        this.ownersUtils = ownersUtils;
        this.publicationUtils = publicationUtils;
        this.repoGestion = repoGestion;
        this.repositoryPublication = repositoryPublication;
    }

    private static void checkIfSeriesExist(String id, RepositoryResult<Statement> statements) throws RmesNotFoundException {
        if (!statements.hasNext()) {
            throw new RmesNotFoundException(ErrorCodes.SERIES_UNKNOWN_ID, "Series not found", id);
        }
    }

    public void publishSeries(String id, JSONObject series) throws RmesException {
        String familyId = series.getJSONObject(Constants.FAMILY).getString(Constants.ID);
        String status = ownersUtils.getValidationStatus(familyId);

        if (PublicationUtils.isUnublished(status)) {
            throw new RmesBadRequestException(
                    ErrorCodes.SERIES_VALIDATION_UNPUBLISHED_FAMILY,
                    "This Series cannot be published before its family is published",
                    "Series: " + id + " ; Family: " + familyId);
        }

        Model model = new LinkedHashModel();
        Resource resource = RdfUtils.seriesIRI(id);

        RepositoryConnection con = repoGestion.getConnection();
        RepositoryResult<Statement> statements = repoGestion.getStatements(con, resource);

        checkIfSeriesExist(id, statements);

        RepositoryResult<Statement> hasPartStatements = repoGestion.getHasPartStatements(con, resource);
        RepositoryResult<Statement> replacesStatements = repoGestion.getReplacesStatements(con, resource);
        RepositoryResult<Statement> isReplacedByStatements = repoGestion.getIsReplacedByStatements(con, resource);

        readAllTriplets(statements, model, hasPartStatements, replacesStatements, isReplacedByStatements, resource, con);
        Resource seriesToPublishResource = publicationUtils.tranformBaseURIToPublish(resource);
        repositoryPublication.publishResource(seriesToPublishResource, model, "serie");

    }

    private void readAllTriplets(RepositoryResult<Statement> statements, Model model, RepositoryResult<Statement> hasPartStatements, RepositoryResult<Statement> replacesStatements, RepositoryResult<Statement> isReplacedByStatements, Resource resource, RepositoryConnection con) throws RmesException {
        try (con) {
            while (statements.hasNext()) {
                Statement st = statements.next();
                String predicate = RdfUtils.toString(st.getPredicate());

                if (URI_PREDICATES_TO_TRANSFORM.stream().anyMatch(predicate::endsWith)) {
                    transformSubjectAndObject(model, st);
                } else if (PREDICATES_TO_IGNORE.stream().noneMatch(predicate::endsWith)) {
                    model.add(publicationUtils.tranformBaseURIToPublish(st.getSubject()),
                            st.getPredicate(),
                            st.getObject(),
                            st.getContext()
                    );
                }
                addStatementsToModel(model, hasPartStatements);
                addStatementsToModel(model, replacesStatements);
                addStatementsToModel(model, isReplacedByStatements);
            }

            /*
              We have to query all published operations linked to this series and publish all of them
             */
            addOperationsWhoHavePartWithToModel(resource, model);


        } finally {
            repoGestion.closeStatements(statements);
            repoGestion.closeStatements(hasPartStatements);
        }
    }


    private void addOperationsWhoHavePartWithToModel(Resource resource, Model model) throws RmesException {
        JSONArray operations = repoGestion.getResponseAsArray(OpSeriesQueries.getPublishedOperationsForSeries(resource.toString()));
        JSONUtils.stream(operations)
                .map(operation -> operation.getString("operation"))
                .forEach(iri -> model.add(
                        publicationUtils.tranformBaseURIToPublish(resource),
                        DCTERMS.HAS_PART,
                        publicationUtils.tranformBaseURIToPublish(RdfUtils.createIRI(iri)),
                        RdfUtils.operationsGraph()
                ));
    }

    public void addStatementsToModel(Model model, RepositoryResult<Statement> statements) {
        while (statements.hasNext()) {
            Statement statement = statements.next();
            transformSubjectAndObject(model, statement);
        }
    }

    public void transformSubjectAndObject(Model model, Statement statement) {
        model.add(publicationUtils.tranformBaseURIToPublish(statement.getSubject()),
                statement.getPredicate(),
                publicationUtils.tranformBaseURIToPublish((Resource) statement.getObject()),
                statement.getContext());
    }

}

