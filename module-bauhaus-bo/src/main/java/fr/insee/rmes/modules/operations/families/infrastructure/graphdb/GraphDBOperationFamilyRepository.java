package fr.insee.rmes.modules.operations.families.infrastructure.graphdb;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.graphdb.ontologies.ADMS;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.modules.commons.hexagonal.ServerSideAdaptor;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamily;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamilySeries;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamilySeriesWithReport;
import fr.insee.rmes.modules.operations.families.domain.model.OperationFamilySubject;
import fr.insee.rmes.modules.operations.families.domain.model.PartialOperationFamily;
import fr.insee.rmes.modules.operations.families.domain.port.serverside.OperationFamilyRepository;
import fr.insee.rmes.modules.shared_kernel.domain.model.Language;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.DiacriticSorter;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.springframework.stereotype.Repository;

@ServerSideAdaptor
@Repository
public class GraphDBOperationFamilyRepository implements OperationFamilyRepository {

    private final RepositoryGestion repositoryGestion;
    private final OperationFamilyQueries operationFamilyQueries;
    private final OperationQueries operationQueries;
    private final RepositoryPublication repositoryPublication;
    private final PublicationUtils publicationUtils;
    private final BauhausLanguagesProperties languages;

    public GraphDBOperationFamilyRepository(
            RepositoryGestion repositoryGestion,
            OperationFamilyQueries operationFamilyQueries,
            OperationQueries operationQueries,
            RepositoryPublication repositoryPublication,
            PublicationUtils publicationUtils,
            BauhausLanguagesProperties languages) {
        this.repositoryGestion = repositoryGestion;
        this.operationFamilyQueries = operationFamilyQueries;
        this.operationQueries = operationQueries;
        this.repositoryPublication = repositoryPublication;
        this.publicationUtils = publicationUtils;
        this.languages = languages;
    }

    @Override
    public List<PartialOperationFamily> getFamilies() throws RmesException {
        var families = this.repositoryGestion.getResponseAsArray(operationFamilyQueries.familiesQuery());

        return DiacriticSorter.sort(families, PartialOperationFamily[].class, PartialOperationFamily::label);
    }

    @Override
    public OperationFamily getFullFamily(String id) throws RmesException {
        var family = getFamily(id);
        var series = getFamilySeries(id);
        if (!series.isEmpty()) {
            family = family.withSeries(series);
        }

        var subjects = getFamilySubjects(id);
        if (!subjects.isEmpty()) {
            family = family.withSubject(subjects);
        }

        return family;
    }

    @Override
    public OperationFamily getFamily(String id) throws RmesException {
        var family = this.repositoryGestion.getResponseAsObject(operationFamilyQueries.familyQuery(id));

        if (family.isEmpty()) {
            throw new RmesException(HttpStatus.SC_BAD_REQUEST, "Family " + id + " not found", "Maybe id is wrong");
        }
        XhtmlToMarkdownUtils.convertJSONObject(family);

        return OperationFamily.fromJson(family);
    }

    @Override
    public List<OperationFamilySeries> getFamilySeries(String id) throws RmesException {
        var array = repositoryGestion.getResponseAsArray(operationFamilyQueries.getSeries(id));
        List<OperationFamilySeries> series = new ArrayList<>();

        if (!array.isEmpty()) {
            JSONUtils.stream(array).forEach(attribute -> series.add(OperationFamilySeries.fromJSON(attribute)));
        }
        return series;
    }

    @Override
    public List<OperationFamilySubject> getFamilySubjects(String id) throws RmesException {
        var array = repositoryGestion.getResponseAsArray(operationFamilyQueries.getSubjects(id));
        List<OperationFamilySubject> subjects = new ArrayList<>();

        if (!array.isEmpty()) {
            JSONUtils.stream(array).forEach(attribute -> subjects.add(OperationFamilySubject.fromJSON(attribute)));
        }
        return subjects;
    }

    /**
     * Une ligne vide est le résultat d'une requête SPARQL sans solution : la filtrer évite de
     * renvoyer au front une série dont tous les champs sont nuls.
     */
    @Override
    public List<OperationFamilySeriesWithReport> getSeriesWithReport(String id) throws RmesException {
        var array = repositoryGestion.getResponseAsArray(operationFamilyQueries.seriesWithReportQuery(id));
        return JSONUtils.stream(array)
                .filter(series -> !series.isEmpty())
                .map(series -> new OperationFamilySeriesWithReport(
                        series.optString("id", null),
                        series.optString("labelLg1", null),
                        series.optString("labelLg2", null),
                        series.optString("idSims", null)))
                .toList();
    }

    @Override
    public String generateId() throws RmesException {
        var json = repositoryGestion.getResponseAsObject(operationQueries.lastId());
        if (json.isEmpty()) {
            return "1000";
        }
        String lastId = json.getString(Constants.ID);
        if (lastId.equals(Constants.UNDEFINED)) {
            return "1000";
        }
        return "s" + (Integer.parseInt(lastId) + 1);
    }

    @Override
    public boolean exists(String id) throws RmesException {
        return repositoryGestion.getResponseAsBoolean(
                operationQueries.checkIfFamOpeSerExists(RdfUtils.toString(familyIRI(id))));
    }

    @Override
    public boolean isPrefLabelAlreadyUsed(String id, String prefLabel, Language language) throws RmesException {
        return repositoryGestion.getResponseAsBoolean(
                operationFamilyQueries.checkPrefLabelUnicity(id, prefLabel, language));
    }

    @Override
    public ValidationStatus getValidationStatus(String id) throws RmesException {
        var state = repositoryGestion.getResponseAsObject(operationQueries.getPublicationState(id));
        return ValidationStatus.fromValue(state.optString("state", null));
    }

    @Override
    public void save(OperationFamily family) throws RmesException {
        Model model = new LinkedHashModel();
        IRI familyURI = familyIRI(family.id());
        Resource graph = RdfUtils.operationsGraph();

        /*Const*/
        model.add(familyURI, RDF.TYPE, INSEE.FAMILY, graph);
        model.add(familyURI, ADMS.HAS_IDENTIFIER, RdfUtils.setLiteralString(family.id()), graph);
        /*Required*/
        model.add(familyURI, SKOS.PREF_LABEL, RdfUtils.setLiteralString(family.prefLabelLg1(), lg1()), graph);
        model.add(familyURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(family.validationState()), graph);
        /*Optional*/
        RdfUtils.addTripleString(familyURI, SKOS.PREF_LABEL, family.prefLabelLg2(), lg2(), model, graph);
        RdfUtils.addTripleStringMdToXhtml(familyURI, DCTERMS.ABSTRACT, family.abstractLg1(), lg1(), model, graph);
        RdfUtils.addTripleStringMdToXhtml(familyURI, DCTERMS.ABSTRACT, family.abstractLg2(), lg2(), model, graph);
        RdfUtils.addTripleDateTime(familyURI, DCTERMS.CREATED, family.created(), model, graph);
        RdfUtils.addTripleDateTime(familyURI, DCTERMS.MODIFIED, family.modified(), model, graph);

        repositoryGestion.keepHierarchicalOperationLinks(familyURI, model);
        repositoryGestion.loadSimpleObject(familyURI, model);
    }

    @Override
    public void publish(String id) throws RmesException {
        IRI familyURI = familyIRI(id);
        repositoryPublication.publishResource(
                publicationUtils.tranformBaseURIToPublish(familyURI), triplesToPublish(familyURI), Constants.FAMILY);

        Model model = new LinkedHashModel();
        Resource graph = RdfUtils.operationsGraph();
        model.add(familyURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.VALIDATED), graph);
        model.remove(familyURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.UNPUBLISHED), graph);
        model.remove(familyURI, INSEE.VALIDATION_STATE, RdfUtils.setLiteralString(ValidationStatus.MODIFIED), graph);
        repositoryGestion.objectValidation(familyURI, model);
    }

    /**
     * Recopie des triplets de gestion vers la publication. L'état de validation et les liens
     * hiérarchiques restent dans le graphe de gestion : le graphe de publication ne porte que
     * la famille telle que le public la voit.
     */
    private Model triplesToPublish(IRI familyURI) throws RmesException {
        Model model = new LinkedHashModel();
        try (RepositoryConnection connection = repositoryGestion.getConnection();
                RepositoryResult<Statement> statements = repositoryGestion.getStatements(connection, familyURI)) {
            if (!statements.hasNext()) {
                throw new RmesNotFoundException(
                        ErrorCodes.FAMILY_UNKNOWN_ID, "Family not found", familyURI.getLocalName());
            }
            while (statements.hasNext()) {
                Statement statement = statements.next();
                String predicate = RdfUtils.toString(statement.getPredicate());
                if (isPublished(predicate)) {
                    model.add(
                            publicationUtils.tranformBaseURIToPublish(statement.getSubject()),
                            statement.getPredicate(),
                            statement.getObject(),
                            statement.getContext());
                }
            }
        } catch (RepositoryException e) {
            throw new RmesException(
                    HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
        }
        return model;
    }

    private static boolean isPublished(String predicate) {
        return !predicate.endsWith("isValidated")
                && !predicate.endsWith("validationState")
                && !predicate.endsWith("hasPart")
                && !predicate.endsWith(Constants.PUBLISHER)
                && !predicate.endsWith(Constants.CONTRIBUTOR);
    }

    private static IRI familyIRI(String id) {
        return RdfUtils.objectIRI(ObjectType.FAMILY, id);
    }

    private String lg1() {
        return languages.lg1();
    }

    private String lg2() {
        return languages.lg2();
    }
}
