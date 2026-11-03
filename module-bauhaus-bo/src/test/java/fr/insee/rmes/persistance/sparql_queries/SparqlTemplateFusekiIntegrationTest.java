package fr.insee.rmes.persistance.sparql_queries;

import static org.assertj.core.api.Assertions.assertThatCode;

import fr.insee.rmes.graphdb.RdfBasicCredentials;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.persistance.sparql_queries.SparqlTemplates.SparqlTemplate;
import fr.insee.rmes.testcontainers.FusekiContainer;
import fr.insee.rmes.testcontainers.WithFusekiContainer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.eclipse.rdf4j.repository.Repository;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

/**
 * Exécute chaque template de requête, rendu comme dans {@link SparqlTemplateRenderingTest}, sur
 * Fuseki, par le même {@link RepositoryUtils} que l'application (F8, C11).
 * <p>
 * Le parseur RDF4J accepte des extensions que Fuseki refuse : ce test verrouille le dialecte, pas
 * les résultats. Le dataset est vide, chaque requête doit seulement être acceptée par le serveur.
 */
@Tag("integration")
class SparqlTemplateFusekiIntegrationTest extends WithFusekiContainer {

    private static final Pattern ASK_FORM =
            Pattern.compile("^\\s*ASK\\b", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private final RepositoryUtils repositoryUtils = new RepositoryUtils(
            null,
            RepositoryInitiator.Type.DISABLED,
            "fuseki",
            new RdfBasicCredentials(FusekiContainer.ADMIN_USER, FusekiContainer.ADMIN_PASSWORD));

    private final Repository repository = repositoryUtils.initRepository(
            getRdfGestionConnectionDetails().getUrlServer(),
            getRdfGestionConnectionDetails().repositoryId());

    @TestFactory
    Stream<DynamicTest> everyTemplateRunsOnFuseki() throws Exception {
        return SparqlTemplates.queries().stream()
                .map(template -> DynamicTest.dynamicTest(template.name(), () -> assertRunsOnFuseki(template)))
                .toList()
                .stream();
    }

    private void assertRunsOnFuseki(SparqlTemplate template) throws Exception {
        for (String query : SparqlTemplates.render(template)) {
            assertThatCode(() -> execute(query))
                    .as(() -> template.name() + " est refusé par Fuseki :\n" + query)
                    .doesNotThrowAnyException();
        }
    }

    private void execute(String query) throws Exception {
        if (SparqlTemplates.isUpdate(query)) {
            repositoryUtils.executeUpdate(query, repository);
        } else if (ASK_FORM.matcher(query).find()) {
            repositoryUtils.getResponseForAskQuery(query, repository);
        } else {
            repositoryUtils.getResponseAsArray(query, repository);
        }
    }
}
