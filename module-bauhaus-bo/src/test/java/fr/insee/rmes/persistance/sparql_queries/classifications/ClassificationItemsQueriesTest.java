package fr.insee.rmes.persistance.sparql_queries.classifications;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.junit.jupiter.api.Test;

/**
 * Les quatre requêtes d'un poste de nomenclature partagent le même adressage : l'IRI du poste se
 * déduit de l'identifiant de la nomenclature et de celui du poste.
 */
class ClassificationItemsQueriesTest {

    private final ClassificationItemsQueries classificationItemsQueries =
            new ClassificationItemsQueries(new BauhausLanguagesProperties("fr", "en"));

    @Test
    void itemQuery_addressesTheItemByTheUriOfItsClassification() throws RmesException {
        String query = classificationItemsQueries.itemQuery("nafr2", "01.1");

        assertThat(query).contains("\"/codes/nafr2/\"");
        assertThat(query).contains("\"/01.1\"");
        assertThat(query).contains("\"fr\"");
        assertThat(query).contains("\"en\"");
    }

    @Test
    void itemAltQuery_addressesTheItemByTheUriOfItsClassification() throws RmesException {
        String query = classificationItemsQueries.itemAltQuery("nafr2", "01.1");

        assertThat(query).contains("\"/codes/nafr2/\"");
        assertThat(query).contains("\"/01.1\"");
        assertThat(query).contains("skosxl:literalForm");
    }

    @Test
    void itemNotesQuery_selectsTheAskedVersionOfTheNotes() throws RmesException {
        String query = classificationItemsQueries.itemNotesQuery("nafr2", "01.1", 3);

        assertThat(query).contains("\"/codes/nafr2/\"");
        assertThat(query).contains("\"3\"");
    }

    @Test
    void itemNarrowersQuery_addressesTheItemByTheUriOfItsClassification() throws RmesException {
        String query = classificationItemsQueries.itemNarrowersQuery("nafr2", "01.1");

        assertThat(query).contains("\"/codes/nafr2/\"");
        assertThat(query).contains("\"/01.1\"");
        assertThat(query).contains("narrower");
    }
}
