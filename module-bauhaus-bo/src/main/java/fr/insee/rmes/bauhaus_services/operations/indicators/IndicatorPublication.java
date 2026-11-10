package fr.insee.rmes.bauhaus_services.operations.indicators;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.operations.OperationsParentRepository;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.errors.IndicatorErrorCode;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.model.links.OperationsLink;
import fr.insee.rmes.model.operations.Indicator;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.ObjectPublication;
import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.springframework.stereotype.Repository;

@Repository
public class IndicatorPublication implements ObjectPublication<Indicator> {
    final OperationsParentRepository operationsParentRepository;
    final RepositoryGestion repoGestion;
    final RepositoryPublication repositoryPublication;
    final PublicationUtils publicationUtils;

    public IndicatorPublication(
            OperationsParentRepository operationsParentRepository,
            RepositoryGestion repoGestion,
            RepositoryPublication repositoryPublication,
            PublicationUtils publicationUtils) {
        this.operationsParentRepository = operationsParentRepository;
        this.repoGestion = repoGestion;
        this.repositoryPublication = repositoryPublication;
        this.publicationUtils = publicationUtils;
    }

    @Override
    public void validate(Indicator indicator) throws RmesException {

        PublicationUtils.rejectIfAlreadyPublished(
                "Indicator",
                indicator.getId(),
                operationsParentRepository.getIndicatorsValidationStatus(indicator.getId()));

        if (indicator.isWasGeneratedByEmpty()) {
            throw RmesBadRequestException.coded(
                    IndicatorErrorCode.EMPTY_WAS_GENERATED_BY, "An indicator should be linked to a series.");
        }

        for (OperationsLink link : indicator.wasGeneratedBy) {
            var status = operationsParentRepository.getValidationStatus(link.getId());
            if (!status.equalsIgnoreCase(ValidationStatus.VALIDATED.toString())) {
                throw RmesBadRequestException.coded(
                        IndicatorErrorCode.VALIDATION_UNVALIDATED_SERIES,
                        "An indicator can be published if and only if all parent series have been published.");
            }
        }
    }

    /**
     * L'indicateur est réparti sur deux graphes : sa description dans celui des produits, ses dates
     * dans celui des opérations. Chacun est relu et republié séparément, dans son propre graphe : le
     * contexte d'un triplet relu est {@code null} hors de GraphDB.
     */
    @Override
    public void publish(String indicatorId) throws RmesException {
        Resource indicator = RdfUtils.objectIRI(ObjectType.INDICATOR, indicatorId);
        Resource productsGraph = RdfUtils.productsGraph();
        Resource operationsGraph = RdfUtils.operationsGraph();

        Model description = new LinkedHashModel();
        Model dates = new LinkedHashModel();
        try (RepositoryConnection con = repoGestion.getConnection()) {
            if (!readTriples(con, indicator, productsGraph, description)) {
                throw new RmesNotFoundException(ErrorCodes.INDICATOR_UNKNOWN_ID, "Indicator not found", indicatorId);
            }
            readTriples(con, indicator, operationsGraph, dates);
        } catch (RepositoryException e) {
            throw new RmesException(
                    HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
        }
        Resource indicatorToPublishRessource = publicationUtils.tranformBaseURIToPublish(indicator);
        repositoryPublication.publishResource(indicatorToPublishRessource, description, "indicator", productsGraph);
        repositoryPublication.publishResource(indicatorToPublishRessource, dates, "indicator", operationsGraph);
    }

    /** Recopie dans le modèle les triplets publiables de l'indicateur lus dans le graphe donné. */
    private boolean readTriples(RepositoryConnection con, Resource indicator, Resource graph, Model model)
            throws RmesException {
        try (RepositoryResult<Statement> statements = repoGestion.getStatements(con, indicator, graph)) {
            boolean found = statements.hasNext();
            while (statements.hasNext()) {
                addTripleToPublish(model, statements.next(), graph);
            }
            return found;
        }
    }

    private void addTripleToPublish(Model model, Statement st, Resource graph) {
        String pred = RdfUtils.toString(st.getPredicate());
        if (pred.endsWith("isValidated") || pred.endsWith("validationState")) {
            // nothing, wouldn't copy this attr
            return;
        }
        Resource subject = publicationUtils.tranformBaseURIToPublish(st.getSubject());
        if (pred.endsWith(Constants.WASGENERATEDBY)
                || pred.endsWith(Constants.SEEALSO)
                || pred.endsWith(Constants.REPLACES)
                || pred.endsWith(Constants.ISREPLACEDBY)
                || pred.endsWith(Constants.CREATOR)
                || pred.endsWith(Constants.CONTRIBUTOR)
                || pred.endsWith(Constants.PUBLISHER)
                || pred.endsWith("accrualPeriodicity")) {
            model.add(
                    subject,
                    st.getPredicate(),
                    publicationUtils.tranformBaseURIToPublish((Resource) st.getObject()),
                    graph);
        } else {
            // Literals
            model.add(subject, st.getPredicate(), st.getObject(), graph);
        }
    }
}
