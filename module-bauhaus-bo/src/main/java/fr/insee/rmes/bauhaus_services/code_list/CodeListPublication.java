package fr.insee.rmes.bauhaus_services.code_list;

import fr.insee.rmes.Constants;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesNotFoundException;
import fr.insee.rmes.exceptions.errors.CodesListErrorCodes;
import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.springframework.stereotype.Repository;

@Repository
public class CodeListPublication extends RdfService {

	private void checkIfResourceExists(RepositoryResult<Statement> statements, Resource codeList) throws RmesNotFoundException {
		if(!statements.hasNext()){
			throw new RmesNotFoundException(CodesListErrorCodes.CODE_LIST_UNKNOWN_ID, "CodeList not found", codeList.stringValue());
		}
	}

	private boolean shouldExcludeTriplet(Statement statement){
		String pred = RdfUtils.toString(statement.getPredicate());
		return pred.endsWith("validationState")
				|| pred.endsWith(Constants.CREATOR)
				|| pred.endsWith(Constants.CONTRIBUTOR)
				|| pred.endsWith("lastCodeUriSegment");
	}


	private void publishCodeListAndCodesWithConnection(Resource codeListOrCode, RepositoryConnection connection) throws RmesException {
		RepositoryResult<Statement> statements = repoGestion.getStatements(connection, codeListOrCode);

		try {
			checkIfResourceExists(statements, codeListOrCode);

			Model model = new LinkedHashModel();

			while (statements.hasNext()) {
				Statement st = statements.next();

				if (shouldExcludeTriplet(st)) {
					continue;
				}


				String predicate = RdfUtils.toString(st.getPredicate());
				String object = st.getObject().stringValue();
				if (RDFS.SEEALSO.toString().equalsIgnoreCase(predicate)) {
					publishSeeAlsoTriplets(object, connection);

					model.add(publicationUtils.tranformBaseURIToPublish(st.getSubject()),
							st.getPredicate(),
							publicationUtils.tranformBaseURIToPublish(RdfUtils.createIRI(st.getObject().stringValue())),
							st.getContext());

				} else {
					model.add(publicationUtils.tranformBaseURIToPublish(st.getSubject()),
							st.getPredicate(),
							st.getObject(),
							st.getContext());
				}
			}

			addCodesStatement(codeListOrCode, connection);

			Resource codeListPublishResource = publicationUtils.tranformBaseURIToPublish(codeListOrCode);
			repositoryPublication.publishResource(codeListPublishResource, model, Constants.CODELIST);
		} catch (RmesException e) {
			throw new RuntimeException(e);
		} finally {
			repoGestion.closeStatements(statements);
		}

	}
	public void publishCodeListAndCodes(Resource codeListOrCode) throws RmesException {
		try (RepositoryConnection connection = repoGestion.getConnection()) {
			publishCodeListAndCodesWithConnection(codeListOrCode, connection);
		} catch (RepositoryException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
		}
	}

	private void addCodesStatement(Resource codeListOrCode, RepositoryConnection connection) throws RmesException {

		RepositoryResult<Statement> statements = repoGestion.getStatementsPredicateObject(connection, SKOS.IN_SCHEME, codeListOrCode);
		while (statements.hasNext()){
			Model model = new LinkedHashModel();
			Statement st = statements.next();
			RepositoryResult<Statement> codeStatements = repoGestion.getStatements(connection, st.getSubject());
			while (codeStatements.hasNext()){
				Statement codeStatement = codeStatements.next();

				if (shouldExcludeTriplet(codeStatement)) {
					continue;
				}


				if(RDF.TYPE.toString().equalsIgnoreCase(codeStatement.getPredicate().toString()) ||
						SKOS.IN_SCHEME.toString().equalsIgnoreCase(codeStatement.getPredicate().toString())){
					model.add(publicationUtils.tranformBaseURIToPublish(codeStatement.getSubject()),
							codeStatement.getPredicate(),
							publicationUtils.tranformBaseURIToPublish(RdfUtils.createIRI(codeStatement.getObject().stringValue())),
							codeStatement.getContext());
				} else {
					model.add(publicationUtils.tranformBaseURIToPublish(codeStatement.getSubject()),
							codeStatement.getPredicate(),
							codeStatement.getObject(),
							codeStatement.getContext());
				}

			}
			Resource codePublishResource = publicationUtils.tranformBaseURIToPublish(st.getSubject());
			repositoryPublication.publishResource(codePublishResource, model, Constants.CODELIST);
		}
	}

	private void publishSeeAlsoTriplets(String seeAlso, RepositoryConnection connection) throws RmesException {
		Model model = new LinkedHashModel();

		IRI seeAlsoIri = RdfUtils.createIRI(seeAlso);
		RepositoryResult<Statement> statements = repoGestion.getStatements(connection, seeAlsoIri);
		try {
			checkIfResourceExists(statements, seeAlsoIri);

			while (statements.hasNext()) {
				Statement st = statements.next();

				if (shouldExcludeTriplet(st)) {
					continue;
				}

				model.add(publicationUtils.tranformBaseURIToPublish(st.getSubject()),
						st.getPredicate(),
						st.getObject(),
						st.getContext());
			}


			Resource codeListPublishResource = publicationUtils.tranformBaseURIToPublish(seeAlsoIri);
			repositoryPublication.publishResource(codeListPublishResource, model, Constants.CODELIST);

		} catch (RepositoryException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
		}
		finally {
			repoGestion.closeStatements(statements);
		}
	}
}