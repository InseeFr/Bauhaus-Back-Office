package fr.insee.rmes.bauhaus_services.operations.series;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.persistance.ontologies.DCTERMS;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SeriesPublication extends RdfService {

    @Autowired
    ParentUtils ownersUtils;

    public SeriesPublication(ParentUtils ownersUtils) {
        this.ownersUtils = ownersUtils;
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

        try {
            while (statements.hasNext()) {
                Statement st = statements.next();
                String pred = st.getPredicate().toString();

                // Other URI to transform
                if (pred.endsWith("isPartOf") ||
                        pred.endsWith(Constants.SEEALSO) ||
                        pred.endsWith(Constants.REPLACES) ||
                        pred.endsWith(Constants.ISREPLACEDBY) ||
                        pred.endsWith(Constants.DATA_COLLECTOR) ||
                        pred.endsWith(Constants.CONTRIBUTOR) ||
                        pred.endsWith(Constants.PUBLISHER) ||
                        pred.endsWith("accrualPeriodicity") ||
                        pred.endsWith("type")) {
                    transformSubjectAndObject(model, st);
                } else if (pred.endsWith("isValidated")
                        || pred.endsWith("validationState")
                        || pred.endsWith("hasPart")) {
                    // nothing, wouldn't copy this attr
                }
                // Literals
                else {
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

            /**
             * We have to query all published operations linked to this series and publish all of them
             */
            addOperationsWhoHavePartWithToModel(resource, model);


        } finally {
            repoGestion.closeStatements(statements);
            repoGestion.closeStatements(hasPartStatements);
            con.close();
        }
        Resource seriesToPublishRessource = publicationUtils.tranformBaseURIToPublish(resource);
        repositoryPublication.publishResource(seriesToPublishRessource, model, "serie");

    }


    private void addOperationsWhoHavePartWithToModel(Resource resource, Model model) throws RmesException {
        JSONArray operations = repoGestion.getResponseAsArray(OpSeriesQueries.getPublishedOperationsForSeries(resource.toString()));
        JSONUtils.stream(operations)
                .map(operation -> operation.getString("operation"))
                .forEach(iri -> {
                    model.add(
                            publicationUtils.tranformBaseURIToPublish(resource),
                            DCTERMS.HAS_PART,
                            publicationUtils.tranformBaseURIToPublish(RdfUtils.createIRI(iri)),
                            RdfUtils.operationsGraph()
                    );
                });
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

