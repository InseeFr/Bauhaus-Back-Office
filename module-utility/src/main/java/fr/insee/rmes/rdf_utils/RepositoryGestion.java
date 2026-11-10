package fr.insee.rmes.rdf_utils;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RdfConnectionDetails;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.graphdb.ontologies.EVOC;
import fr.insee.rmes.graphdb.ontologies.INSEE;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.rdf4j.common.exception.RDF4JException;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class RepositoryGestion {

    private static final String FAILURE_LOAD_OBJECT = "Failure load object : {}";
    private static final String FAILURE_REPLACE_GRAPH = "Failure replace graph : ";

    static final Logger logger = LoggerFactory.getLogger(RepositoryGestion.class);

    private final RdfConnectionDetails rdfGestion;

    private final RepositoryUtils repositoryUtils;

    public RepositoryGestion(
            @Qualifier("rdfGestionConnectionDetails") RdfConnectionDetails rdfGestion,
            RepositoryUtils repositoryUtils) {
        this.rdfGestion = rdfGestion;
        this.repositoryUtils = repositoryUtils;
    }

    @PostConstruct
    public void init() {
        repositoryUtils.initRepository(rdfGestion.getUrlServer(), rdfGestion.repositoryId());
    }

    /**
     * Ouvre une connexion sur la base de gestion. {@link RepositoryUtils#initRepository} rend
     * {@code null} quand la configuration RDF est incomplète : passer par
     * {@link RepositoryUtils#getConnection} transforme ce cas en RmesException explicite plutôt
     * qu'en NullPointerException au premier usage de la connexion.
     */
    private RepositoryConnection gestionConnection() throws RmesException {
        return repositoryUtils.getConnection(
                repositoryUtils.initRepository(rdfGestion.getUrlServer(), rdfGestion.repositoryId()));
    }

    /**
     * Method which aims to produce response from a sparql query
     *
     * @param query
     * @return String
     * @throws RmesException
     */
    public String getResponse(String query) throws RmesException {
        return repositoryUtils.getResponse(
                query, repositoryUtils.initRepository(rdfGestion.getUrlServer(), rdfGestion.repositoryId()));
    }

    /**
     * Method which aims to execute sparql update
     *
     * @param updateQuery
     * @return String
     * @throws RmesException
     */
    public HttpStatus executeUpdate(String updateQuery) throws RmesException {
        return repositoryUtils.executeUpdate(
                updateQuery, repositoryUtils.initRepository(rdfGestion.getUrlServer(), rdfGestion.repositoryId()));
    }

    /**
     * Method which aims to produce response from a sparql query
     *
     * @param query
     * @return JSONObject
     * @throws RmesException
     */
    public JSONObject getResponseAsObject(String query) throws RmesException {
        return repositoryUtils.getResponseAsObject(
                query, repositoryUtils.initRepository(rdfGestion.getUrlServer(), rdfGestion.repositoryId()));
    }

    public JSONArray getResponseAsArray(String query) throws RmesException {
        return repositoryUtils.getResponseAsArray(
                query, repositoryUtils.initRepository(rdfGestion.getUrlServer(), rdfGestion.repositoryId()));
    }

    public JSONArray getResponseAsJSONList(String query) throws RmesException {
        return repositoryUtils.getResponseAsJSONList(
                query, repositoryUtils.initRepository(rdfGestion.getUrlServer(), rdfGestion.repositoryId()));
    }

    /**
     * Method which aims to produce response from a sparql ASK query
     *
     * @param query
     * @return String
     * @throws RmesException
     * @throws JSONException
     */
    public boolean getResponseAsBoolean(String query) throws RmesException {
        return repositoryUtils.getResponseForAskQuery(
                query, repositoryUtils.initRepository(rdfGestion.getUrlServer(), rdfGestion.repositoryId()));
    }

    public RepositoryResult<Statement> getStatements(RepositoryConnection con, Resource subject) throws RmesException {
        RepositoryResult<Statement> statements = null;
        if (con == null) {
            con = gestionConnection();
        }

        try {
            statements = con.getStatements(subject, null, null, false);
        } catch (RepositoryException e) {
            throwsRmesException(e, "Failure get statements : " + subject);
        }
        return statements;
    }

    public void closeStatements(RepositoryResult<Statement> statements) throws RmesException {
        try {
            statements.close();
        } catch (RepositoryException e) {
            throwsRmesException(e, "Failure close statements : ");
        }
    }

    public void loadConcept(IRI concept, Model model, List<List<IRI>> notesToDeleteAndUpdate) throws RmesException {
        try (RepositoryConnection conn = gestionConnection()) {
            // notes to delete
            for (IRI note : notesToDeleteAndUpdate.get(0)) {
                removeSubjectFromEveryGraph(conn, note);
            }
            // notes to update
            for (IRI note : notesToDeleteAndUpdate.get(1)) {
                deleteInEveryGraph(conn, "<%s> <%s> ?value".formatted(note, EVOC.NOTE_LITERAL));
            }
            // links to delete
            clearConceptLinks(concept);

            loadSimpleObject(concept, model, conn);
        } catch (RDF4JException e) {
            throwsRmesException(e, "Failure load concept : " + concept);
        }
    }

    public void deleteTripletByPredicateAndValue(
            Resource object, IRI predicate, Resource graph, RepositoryConnection conn, Value value)
            throws RmesException {
        processConnection(
                connection -> connection.remove(object, predicate, value, graph),
                conn,
                "delete triplet whose object is " + object);
    }

    public void deleteTripletByPredicateAndValue(Resource object, IRI predicate, Resource graph, Value value)
            throws RmesException {
        try (RepositoryConnection conn = gestionConnection()) {
            conn.remove(object, predicate, value, graph);
        } catch (RepositoryException e) {
            logger.error(FAILURE_LOAD_OBJECT, object);
            logger.error(e.getMessage());
            throw new RmesException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), FAILURE_LOAD_OBJECT + object);
        }
    }

    public void deleteTripletByPredicate(Resource object, IRI predicate, Resource graph, RepositoryConnection conn)
            throws RmesException {
        deleteTripletByPredicateAndValue(object, predicate, graph, conn, null);
    }

    public void deleteTripletByPredicate(Resource object, IRI predicate, Resource graph) throws RmesException {
        deleteTripletByPredicateAndValue(object, predicate, graph, null);
    }

    public void loadSimpleObjectWithoutDeletion(IRI object, Model model, RepositoryConnection conn)
            throws RmesException {
        processConnection(connection -> connection.add(model), conn, FAILURE_LOAD_OBJECT + object);
    }

    /**
     * @param object
     * @param model
     * @param conn : can be null - initialized by the method
     * @throws RmesException
     */
    public void loadSimpleObject(IRI object, Model model, RepositoryConnection conn) throws RmesException {

        processConnection(
                connection -> {
                    logger.info("Removing existing triples for object: {}", object);
                    removeSubjectFromEveryGraph(connection, object);

                    logger.info("Adding new triples");
                    model.forEach(statement -> logger.info("Triplet: {}", statement));

                    connection.add(model);
                },
                conn,
                FAILURE_LOAD_OBJECT + object);
    }

    /**
     * Exécute les mises à jour SPARQL puis remplace les triplets de chaque objet dans son graphe, en
     * une seule transaction : si une étape échoue, la base reste dans son état d'avant l'appel.
     */
    public void replaceObjects(List<String> updates, List<SubjectModelGraph> objects) throws RmesException {
        try (RepositoryConnection connection = gestionConnection()) {
            connection.begin();
            try {
                for (String update : updates) {
                    connection.prepareUpdate(QueryLanguage.SPARQL, update).execute();
                }
                for (SubjectModelGraph object : objects) {
                    connection.remove(object.subject(), null, null, object.graph());
                    connection.add(object.model());
                }
                connection.commit();
            } catch (RDF4JException e) {
                connection.rollback();
                throw e;
            }
        } catch (RDF4JException e) {
            throw new RmesException("Failure while replacing objects", e);
        }
    }

    public void deleteObject(IRI object, RepositoryConnection conn) throws RmesException {
        processConnection(connection -> removeSubjectFromEveryGraph(connection, object), conn, "delete " + object);
    }

    public void deleteObject(IRI object) throws RmesException {
        deleteObject(object, null);
    }

    /**
     * @param graph
     * @param model
     * @param conn : can be null - initialized by the method
     * @throws RmesException
     */
    public void replaceGraph(Resource graph, Model model, RepositoryConnection conn) throws RmesException {
        processConnection(
                connection -> {
                    connection.clear(graph);
                    connection.add(model);
                },
                conn,
                FAILURE_REPLACE_GRAPH + graph);
    }

    private void processConnection(
            Consumer<RepositoryConnection> processor, RepositoryConnection initialConnection, String message)
            throws RmesException {
        try (RepositoryConnection conn = (initialConnection != null) ? initialConnection : gestionConnection()) {
            processor.accept(conn);
        } catch (RepositoryException e) {
            throw new RmesException("Failure while " + message, e);
        }
    }

    public void loadObjectWithReplaceLinks(IRI object, Model model) throws RmesException {

        try (RepositoryConnection conn = gestionConnection()) {
            clearReplaceLinks(object);
            loadSimpleObject(object, model, conn);
        } catch (RepositoryException e) {
            throwsRmesException(e, FAILURE_LOAD_OBJECT + object);
        }
    }

    public void objectsValidation(List<IRI> collectionsToValidateList, Model model) throws RmesException {
        try (RepositoryConnection conn = gestionConnection()) {
            for (IRI item : collectionsToValidateList) {
                removeValidationStatusFromEveryGraph(conn, item);
            }
            conn.add(model);
        } catch (RepositoryException e) {
            throwsRmesException(e, "Failure validation : " + collectionsToValidateList);
        }
    }

    public void objectValidation(IRI ressourceURI, Model model) throws RmesException {
        try (RepositoryConnection conn = gestionConnection()) {
            removeValidationStatusFromEveryGraph(conn, ressourceURI);
            conn.add(model);
        } catch (RepositoryException e) {
            throwsRmesException(e, "Failure validation : " + ressourceURI);
        }
    }

    /**
     * Efface le statut dans tous les graphes nommés, par SPARQL : un {@code remove} sans graphe ne
     * supprime rien hors de GraphDB, dont le graphe par défaut est l'union des graphes nommés.
     */
    private static void removeValidationStatusFromEveryGraph(RepositoryConnection conn, IRI object) {
        for (IRI predicate : List.of(INSEE.VALIDATION_STATE, INSEE.IS_VALIDATED)) {
            deleteInEveryGraph(conn, "<%s> <%s> ?value".formatted(object, predicate));
        }
    }

    /**
     * Efface les triplets du sujet dans tous les graphes nommés, par SPARQL, pour la même raison que
     * {@link #removeValidationStatusFromEveryGraph}.
     */
    private static void removeSubjectFromEveryGraph(RepositoryConnection conn, IRI subject) {
        deleteInEveryGraph(conn, "<%s> ?predicate ?value".formatted(subject));
    }

    /** Efface, dans tous les graphes nommés, les triplets qui correspondent au motif SPARQL donné. */
    private static void deleteInEveryGraph(RepositoryConnection conn, String triplePattern) {
        conn.prepareUpdate(QueryLanguage.SPARQL, "DELETE WHERE { GRAPH ?graph { %s } }".formatted(triplePattern))
                .execute();
    }

    public void clearConceptLinks(Resource concept) throws RmesException {
        List<IRI> typeOfLink = Arrays.asList(SKOS.BROADER, SKOS.NARROWER, SKOS.RELATED, DCTERMS.IS_REPLACED_BY);
        getStatementsAndRemove(concept, typeOfLink);
    }

    /**
     * Retire les liens symétriques qui pointent vers l'objet : son modèle les réécrit tous, dans les
     * deux sens, et ceux qu'il ne porte plus doivent disparaître aussi de l'autre extrémité.
     */
    private void clearReplaceLinks(Resource object) throws RmesException {
        List<IRI> typeOfLink = Arrays.asList(DCTERMS.REPLACES, DCTERMS.IS_REPLACED_BY, RDFS.SEEALSO);
        getStatementsAndRemove(object, typeOfLink);
    }

    /** Efface, dans tous les graphes nommés, les liens des types donnés qui pointent vers l'objet. */
    private void getStatementsAndRemove(Resource object, List<IRI> typeOfLink) throws RmesException {
        String exceptionCircumstances = "";
        try (RepositoryConnection conn = gestionConnection()) {
            for (IRI predicat : typeOfLink) {
                exceptionCircumstances = "remove " + predicat + " links to " + object;
                deleteInEveryGraph(conn, "?subject <%s> <%s>".formatted(predicat, object));
            }
        } catch (RDF4JException e) {
            throwsRmesException(e, "Failure " + exceptionCircumstances);
        }
    }

    public void clearStructureNodeAndComponents(Resource structure, Resource graph) throws RmesException {
        repositoryUtils.clearStructureAndComponents(
                structure, graph, repositoryUtils.initRepository(rdfGestion.getUrlServer(), rdfGestion.repositoryId()));
    }

    /**
     * Reporte dans le modèle les liens {@code dcterms:hasPart} / {@code dcterms:isPartOf} de l'objet,
     * entrants et sortants, lus dans le graphe donné, puis les y efface : le modèle les réécrit avec
     * ce graphe. Le contexte d'un triplet relu est {@code null} hors de GraphDB, le graphe vient donc
     * de l'appelant.
     */
    public void keepHierarchicalOperationLinks(Resource object, Model model, Resource graph) throws RmesException {
        try (RepositoryConnection conn = getConnection()) {
            for (IRI predicate : List.of(DCTERMS.HAS_PART, DCTERMS.IS_PART_OF)) {
                try {
                    try (RepositoryResult<Statement> statements =
                            conn.getStatements(null, predicate, object, false, graph)) {
                        addStatementsToModel(model, statements, graph);
                    }
                    try (RepositoryResult<Statement> statements =
                            conn.getStatements(object, predicate, null, false, graph)) {
                        addStatementsToModel(model, statements, graph);
                    }
                    conn.remove((Resource) null, predicate, object, graph);
                    conn.remove(object, predicate, null, graph);
                } catch (RepositoryException e) {
                    throwsRmesException(e, "Failure keepHierarchicalOperationLinks : " + object);
                }
            }
        }
    }

    private static void addStatementsToModel(Model model, RepositoryResult<Statement> statements, Resource graph) {
        while (statements.hasNext()) {
            Statement st = statements.next();
            model.add(st.getSubject(), st.getPredicate(), st.getObject(), graph);
        }
    }

    private static void throwsRmesException(Exception e, String details) throws RmesException {
        logger.error(details);
        logger.error("{} {}", e.getClass(), e.getMessage());
        throw new RmesException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getClass() + " " + e.getMessage(), details);
    }

    public void loadSimpleObject(IRI geoIRI, Model model) throws RmesException {
        loadSimpleObject(geoIRI, model, null);
    }

    public RepositoryConnection getConnection() throws RmesException {
        return repositoryUtils.getConnection(
                repositoryUtils.initRepository(rdfGestion.getUrlServer(), rdfGestion.repositoryId()));
    }

    public void overrideTriplets(IRI simsUri, Model model, Resource graph) throws RmesException {
        try (RepositoryConnection connection = gestionConnection()) {
            model.predicates().forEach(predicate -> connection.remove(simsUri, predicate, null, graph));
            connection.add(model);
        } catch (RepositoryException e) {
            throw new RmesException("Failure override triplets" + simsUri, e);
        }
    }

    public RepositoryResult<Statement> getCompleteGraph(RepositoryConnection con, Resource graphIri)
            throws RmesException {
        return repositoryUtils.getCompleteGraph(con, graphIri);
    }

    public void getMultipleTripletsForObject(JSONObject object, String objectKey, String query, String queryKey)
            throws RmesException {
        JSONArray array = this.getResponseAsArray(query);
        List<String> results = new ArrayList<>();
        array.iterator().forEachRemaining(r -> results.add(((JSONObject) r).getString(queryKey)));
        object.put(objectKey, results);
    }

    /**
     * Triplets du sujet dans le graphe donné. À préférer à {@link #getStatements(RepositoryConnection,
     * Resource)} : sans graphe nommé, la lecture ne rend rien sur un triplestore dont le graphe par
     * défaut n'est pas l'union des graphes nommés, et les triplets lus n'y portent pas leur graphe.
     */
    public RepositoryResult<Statement> getStatements(RepositoryConnection con, Resource subject, Resource graph)
            throws RmesException {
        RepositoryResult<Statement> statements = null;
        if (con == null) {
            con = gestionConnection();
        }

        try {
            statements = con.getStatements(subject, null, null, false, graph);
        } catch (RepositoryException e) {
            throwsRmesException(e, "Failure get statements : " + subject + " in " + graph);
        }
        return statements;
    }

    /**
     * Triplets de prédicat et d'objet donnés dans le graphe donné, pour la même raison que {@link
     * #getStatements(RepositoryConnection, Resource, Resource)}.
     */
    public RepositoryResult<Statement> getStatementsPredicateObject(
            RepositoryConnection con, IRI predicate, Resource object, Resource graph) throws RmesException {
        RepositoryResult<Statement> statements = null;
        if (con == null) {
            con = gestionConnection();
        }

        try {
            statements = con.getStatements(null, predicate, object, false, graph);
        } catch (RepositoryException e) {
            throwsRmesException(e, "Failure get " + predicate + " statements : " + object + " in " + graph);
        }
        return statements;
    }
}
