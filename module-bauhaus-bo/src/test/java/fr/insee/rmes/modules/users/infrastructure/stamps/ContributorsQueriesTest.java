package fr.insee.rmes.modules.users.infrastructure.stamps;

import static fr.insee.rmes.persistance.sparql_queries.SparqlQueryNormalizer.normalize;
import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.domain.exceptions.RmesException;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Test;

class ContributorsQueriesTest {

    private final ContributorsQueries queries = new ContributorsQueries();

    @Test
    void reads_the_dc_contributor_of_the_resource_in_its_graph() throws RmesException {
        var iri = SimpleValueFactory.getInstance().createIRI("http://bauhaus/concepts/definition/c1000");

        String sparql = normalize(queries.getContributors("http://rdf.insee.fr/graphes/concepts", iri));

        assertThat(sparql).isEqualTo(normalize("""
                SELECT ?contributor
                FROM <http://rdf.insee.fr/graphes/concepts>
                WHERE {
                    <http://bauhaus/concepts/definition/c1000> dc:contributor ?contributor .
                }
                """));
    }

    @Test
    void reads_the_dc_creator_of_the_resource_in_its_graph() throws RmesException {
        var iri = SimpleValueFactory.getInstance().createIRI("http://bauhaus/produits/indicateur/p1");

        String sparql = normalize(queries.getCreators("http://rdf.insee.fr/graphes/produits", iri));

        assertThat(sparql).isEqualTo(normalize("""
                SELECT ?contributor
                FROM <http://rdf.insee.fr/graphes/produits>
                WHERE {
                    <http://bauhaus/produits/indicateur/p1> dc:creator ?contributor .
                }
                """));
    }
}
