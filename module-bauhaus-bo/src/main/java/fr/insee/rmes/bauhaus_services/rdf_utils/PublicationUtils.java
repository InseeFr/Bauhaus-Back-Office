package fr.insee.rmes.bauhaus_services.rdf_utils;

import static fr.insee.rmes.PropertiesKeys.BASE_URI_GESTION;
import static fr.insee.rmes.PropertiesKeys.BASE_URI_PUBLICATION;
import static java.util.Objects.requireNonNull;

import fr.insee.rmes.Constants;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import jakarta.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Set;
import org.apache.http.HttpStatus;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public record PublicationUtils(
        String baseUriGestion,
        String baseUriPublication,
        RepositoryGestion repositoryGestion,
        RepositoryPublication repositoryPublication) {
    public PublicationUtils(
            @Value("${" + BASE_URI_GESTION + "}") String baseUriGestion,
            @Value("${" + BASE_URI_PUBLICATION + "}") String baseUriPublication,
            RepositoryGestion repositoryGestion,
            RepositoryPublication repositoryPublication) {
        this.baseUriGestion = baseUriGestion;
        this.baseUriPublication = baseUriPublication;
        this.repositoryGestion = repositoryGestion;
        this.repositoryPublication = repositoryPublication;
    }

    public Resource tranformBaseURIToPublish(Resource resource) {
        if (!resource.toString().contains(this.baseUriGestion)) return resource;
        String newResource = resource.toString().replace(this.baseUriGestion, this.baseUriPublication);
        return RdfUtils.toURI(newResource);
    }

    public static boolean stringEndsWithItemFromList(@NotNull String inputStr, @NotNull String[] items) {
        return Arrays.stream(items).parallel().anyMatch(requireNonNull(inputStr)::endsWith);
    }

    public static boolean isUnublished(String status) {
        return ValidationStatus.UNPUBLISHED.getValue().equals(status) || Constants.UNDEFINED.equals(status);
    }

    /**
     * A resource is already published when its validation state is exactly {@code Validated}.
     * {@code Modified} means it has been published then edited again : republishing it is the
     * normal way to propagate the edition, so it is not considered as already published.
     */
    public static boolean isPublished(String status) {
        return ValidationStatus.VALIDATED.getValue().equalsIgnoreCase(status);
    }

    /**
     * Guard shared by every publication endpoint : publishing twice the very same version of a
     * resource is a client mistake, not a no-op, so it is rejected with a 400.
     *
     * @param resourceType the human readable type of the resource, used in the error message
     */
    public static void rejectIfAlreadyPublished(String resourceType, String id, String status)
            throws RmesBadRequestException {
        if (isPublished(status)) {
            throw new RmesBadRequestException(
                    ErrorCodes.ALREADY_PUBLISHED,
                    "This " + resourceType.toLowerCase() + " is already published",
                    resourceType + ": " + id);
        }
    }

    public void publishResource(Resource resource, Set<String> denyList) throws RmesException {
        Model model = new LinkedHashModel();
        try (RepositoryConnection connection = repositoryGestion.getConnection();
                RepositoryResult<Statement> statements = repositoryGestion.getStatements(connection, resource)) {
            while (statements.hasNext()) {
                Statement statement = statements.next();
                String predicate = RdfUtils.toString(statement.getPredicate());

                boolean isDeniedPredicate =
                        !denyList.stream().filter(predicate::endsWith).toList().isEmpty();

                if (!isDeniedPredicate) {
                    try {
                        model.add(
                                tranformBaseURIToPublish(statement.getSubject()),
                                statement.getPredicate(),
                                tranformBaseURIToPublish((Resource) statement.getObject()),
                                statement.getContext());

                        if (statement.getObject().isBNode()) {
                            publishResource((Resource) statement.getObject(), Set.of());
                        }
                    } catch (ClassCastException _) {
                        model.add(
                                tranformBaseURIToPublish(statement.getSubject()),
                                statement.getPredicate(),
                                statement.getObject(),
                                statement.getContext());
                    }
                }
            }
        } catch (RepositoryException e) {
            throw new RmesException(
                    HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
        }
        Resource resourceToPublish = tranformBaseURIToPublish(resource);
        repositoryPublication.publishResource(resourceToPublish, model, null);
    }
}
