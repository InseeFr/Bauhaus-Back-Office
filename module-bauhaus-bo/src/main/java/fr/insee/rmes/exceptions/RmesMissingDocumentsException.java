package fr.insee.rmes.exceptions;

import org.json.JSONArray;

import java.util.Collection;

/**
 * Raised when a SIMS publication is requested while some of the documents it references
 * are missing from the management storage. The publication is blocked (no RDF write, no
 * file copy) and a 400 response is returned, carrying the dedicated error code and the
 * list of missing document ids so that the front-end can list them to the user.
 */
public class RmesMissingDocumentsException extends RmesBadRequestException {

	private static final long serialVersionUID = 862L;

	public RmesMissingDocumentsException(Collection<String> missingDocumentIds) {
		super(ErrorCodes.SIMS_PUBLICATION_MISSING_DOCUMENTS,
				"Some documents referenced by this metadataReport are missing from storage",
				new JSONArray(missingDocumentIds));
	}
}
