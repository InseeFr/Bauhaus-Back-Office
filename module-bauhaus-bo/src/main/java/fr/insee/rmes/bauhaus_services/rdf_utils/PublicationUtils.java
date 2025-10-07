package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.onion.domain.exceptions.RmesException;
import fr.insee.rmes.model.ValidationStatus;
import jakarta.validation.constraints.NotNull;
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

import java.util.Arrays;
import java.util.Set;

import static fr.insee.rmes.config.PropertiesKeys.BASE_URI_GESTION;
import static fr.insee.rmes.config.PropertiesKeys.BASE_URI_PUBLICATION;
import static java.util.Objects.requireNonNull;

@Service
public record PublicationUtils(String baseUriGestion, String baseUriPublication, RepositoryGestion repositoryGestion, RepositoryPublication repositoryPublication) {
    public PublicationUtils(@Value("${" + BASE_URI_GESTION + "}") String baseUriGestion,
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

    public void publishResource(Resource resource, Set<String> denyList) throws RmesException {
        Model model = new LinkedHashModel();
        RepositoryConnection connection = repositoryGestion.getConnection();
        RepositoryResult<Statement> statements = repositoryGestion.getStatements(connection, resource);

        try {
            try {
                while (statements.hasNext()) {
                    Statement statement = statements.next();
                    String predicate = RdfUtils.toString(statement.getPredicate());

                    boolean isDeniedPredicate = !denyList.stream().filter(predicate::endsWith).toList().isEmpty();

                    if(!isDeniedPredicate){
                        try {
                            model.add(tranformBaseURIToPublish(statement.getSubject()),
                                    statement.getPredicate(),
                                    tranformBaseURIToPublish((Resource) statement.getObject()),
                                    statement.getContext());

                            if(statement.getObject().isBNode()){
                                publishResource((Resource) statement.getObject(), Set.of());
                            }
                        } catch(ClassCastException ignored){
                            model.add(tranformBaseURIToPublish(statement.getSubject()),
                                    statement.getPredicate(),
                                    statement.getObject(),
                                    statement.getContext());
                        }
                    }
                }

            } catch (RepositoryException e) {
                throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), Constants.REPOSITORY_EXCEPTION);
            }

        } finally {
            repositoryGestion.closeStatements(statements);
            connection.close();
        }
        Resource resourceToPublish = tranformBaseURIToPublish(resource);
        repositoryPublication.publishResource(resourceToPublish, model, null);
    }
}
