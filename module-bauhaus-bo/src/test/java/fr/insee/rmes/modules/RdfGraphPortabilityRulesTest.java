package fr.insee.rmes.modules;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.jupiter.api.Test;

/**
 * Vérifie ce que détectent les règles de {@link RdfGraphPortabilityArchTest}, sur des classes
 * témoins : leur version gelée ne dit rien d'une règle qui ne verrait rien.
 */
class RdfGraphPortabilityRulesTest {

    @Test
    void reading_the_graph_of_a_statement_is_reported() {
        assertThat(violations(RdfGraphPortabilityArchTest.NO_STATEMENT_GET_CONTEXT, ReadsTheGraphOfAStatement.class))
                .singleElement()
                .asString()
                .contains(ReadsTheGraphOfAStatement.class.getName() + ".graphOf(")
                .contains("getContext()");
    }

    @Test
    void removing_triples_without_a_named_graph_is_reported() {
        assertThat(violations(
                        RdfGraphPortabilityArchTest.NO_NATIVE_ACCESS_WITHOUT_NAMED_GRAPH,
                        RemovesWithoutNamedGraph.class))
                .singleElement()
                .asString()
                .contains(RemovesWithoutNamedGraph.class.getName() + ".delete(")
                .contains("RepositoryConnection.remove");
    }

    @Test
    void reading_triples_without_a_named_graph_is_reported() {
        assertThat(violations(
                        RdfGraphPortabilityArchTest.NO_NATIVE_ACCESS_WITHOUT_NAMED_GRAPH, ReadsWithoutNamedGraph.class))
                .hasSize(2)
                .allSatisfy(violation -> assertThat(violation).contains("RepositoryConnection.getStatements"));
    }

    @Test
    void a_call_without_named_graph_inside_a_lambda_is_reported() {
        assertThat(violations(
                        RdfGraphPortabilityArchTest.NO_NATIVE_ACCESS_WITHOUT_NAMED_GRAPH,
                        RemovesWithoutNamedGraphInALambda.class))
                .singleElement()
                .asString()
                .contains("RepositoryConnection.remove");
    }

    @Test
    void native_calls_naming_their_graph_and_in_memory_models_are_allowed() {
        assertThat(violations(RdfGraphPortabilityArchTest.NO_NATIVE_ACCESS_WITHOUT_NAMED_GRAPH, NamesItsGraph.class))
                .isEmpty();
    }

    private static List<String> violations(ArchRule rule, Class<?> fixture) {
        JavaClasses classes = new ClassFileImporter().importClasses(fixture);
        return rule.evaluate(classes).getFailureReport().getDetails();
    }

    static class ReadsTheGraphOfAStatement {
        Resource graphOf(Statement statement) {
            return statement.getContext();
        }
    }

    static class RemovesWithoutNamedGraph {
        void delete(RepositoryConnection connection, IRI subject) {
            connection.remove(subject, null, null);
        }
    }

    static class ReadsWithoutNamedGraph {
        void read(RepositoryConnection connection, IRI subject, IRI predicate) {
            connection.getStatements(subject, null, null, false);
            connection.getStatements(null, predicate, subject);
        }
    }

    static class RemovesWithoutNamedGraphInALambda {
        Runnable delete(RepositoryConnection connection, IRI subject) {
            return () -> connection.remove(subject, null, null);
        }
    }

    static class NamesItsGraph {
        void readAndDelete(RepositoryConnection connection, Model model, IRI subject, IRI graph) {
            connection.getStatements(subject, null, null, false, graph);
            connection.remove(subject, null, null, graph);
            model.remove(subject, null, null);
        }
    }
}
