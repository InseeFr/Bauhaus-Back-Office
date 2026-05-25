package fr.insee.rmes.bauhaus_services.operations;

import fr.insee.rmes.bauhaus_services.OperationsDocumentationsService;
import fr.insee.rmes.bauhaus_services.operations.documentations.DocumentationsUtils;
import fr.insee.rmes.graphdb.QueryUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryPublication;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.utils.IdGenerator;
import fr.insee.rmes.modules.operations.msd.domain.port.serverside.DocumentationRepository;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.model.operations.documentations.Documentation;
import fr.insee.rmes.model.operations.documentations.MSD;
import fr.insee.rmes.onion.infrastructure.graphdb.operations.queries.DocumentationQueries;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@Service
public class OperationsDocumentationsImpl  extends RdfService implements OperationsDocumentationsService {

	static final Logger logger = LoggerFactory.getLogger(OperationsDocumentationsImpl.class);

	private final org.springframework.core.io.Resource simsDefaultValue;

	private final DocumentationsUtils documentationsUtils;

	private final ParentUtils ownersUtils;

	private final DocumentationRepository documentationRepository;

	private final DocumentationQueries documentationQueries;

	public OperationsDocumentationsImpl(RepositoryGestion repoGestion, IdGenerator idGenerator,
										RepositoryPublication repositoryPublication,
										PublicationUtils publicationUtils,
										@Value("classpath:bauhaus-sims.json") org.springframework.core.io.Resource simsDefaultValue,
										DocumentationsUtils documentationsUtils,
										ParentUtils ownersUtils,
										DocumentationRepository documentationRepository,
										DocumentationQueries documentationQueries) {
		super(repoGestion, idGenerator, repositoryPublication, publicationUtils);
		this.simsDefaultValue = simsDefaultValue;
		this.documentationsUtils = documentationsUtils;
		this.ownersUtils = ownersUtils;
		this.documentationRepository = documentationRepository;
		this.documentationQueries = documentationQueries;
	}


	/***************************************************************************************************
	 * DOCUMENTATION
	 * @throws RmesException 
	 *****************************************************************************************************/

	@Override
	public String getMSDJson() throws RmesException {
		String resQuery = repoGestion.getResponseAsArray(documentationQueries.msdQuery()).toString();
		return QueryUtils.correctEmptyGroupConcat(resQuery);
	}

	@Override
	public String getMetadataReportDefaultValue() throws IOException {
		try (InputStream is = this.simsDefaultValue.getInputStream()) {
			return StreamUtils.copyToString(is, Charset.defaultCharset());
		}
	}

	@Override
	public MSD getMSD() throws RmesException {
		return documentationsUtils.getMSD();
	}

	@Override
	public String getMetadataReport(String id) throws RmesException {
		JSONObject documentation = documentationsUtils.getDocumentationByIdSims(id);
		return documentation.toString();
	}

	@Override
	public Documentation getFullSimsForXml(String id) throws RmesException {
		return  documentationsUtils.getFullSimsForXml(id);
	}

	@Override
	public String getFullSimsForJson(String id) throws RmesException {
		return  documentationsUtils.getFullSimsForJson(id).toString();
	}

	@Override
	public String getMetadataReportOwner(String id) throws RmesException {
		return ownersUtils.getDocumentationOwnersByIdSims(id);
	}


	/**
	 * CREATE
	 */
	@Override
	public String createMetadataReport(String body) throws RmesException {
		return documentationsUtils.setMetadataReport(null, body, true);
	}


	/**
	 * UPDATE
	 */
	@Override
	public void setMetadataReport(String id, String body) throws RmesException {
		documentationsUtils.setMetadataReport(id, body, false);
	}

	/**
	 * DELETE
	 */
	@Override
	public HttpStatus deleteMetadataReport(String id) throws RmesException {
		return documentationsUtils.deleteMetadataReport(id);
	}

	/**
	 * PUBLISH
	 */
	@Override
	public void publishMetadataReport(String id) throws RmesException {
		documentationsUtils.publishMetadataReport(id);
	}

}
