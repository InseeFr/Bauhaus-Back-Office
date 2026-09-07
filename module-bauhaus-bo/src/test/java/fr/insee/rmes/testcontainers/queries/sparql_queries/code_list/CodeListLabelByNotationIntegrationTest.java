package fr.insee.rmes.testcontainers.queries.sparql_queries.code_list;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.PaginationProperties;
import fr.insee.rmes.config.BauhausUriPropertiesStub;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.modules.codeslists.codeslists.infrastructure.graphdb.CodeListsQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code CodeListsQueries.getCodeListLabelByNotation} ({@code codes-list/getCodeListLabelByNotation.ftlh})
 * alimente {@code CodeListServiceImpl.getCodeListJson} et l'export {@code CodesListExportImpl} :
 * l'objet qu'elle rend est l'en-tête de la liste de codes (libellés lg1/lg2).
 *
 * <p>Trois traits du parcours ne sont visibles qu'à l'exécution réelle et non dans le texte de
 * la requête : la restriction à {@code rdf:type skos:ConceptScheme}, l'ouverture explicite du
 * graphe des codes, et surtout le fait que les deux {@code FILTER (lang(...))} portent sur deux
 * motifs {@code skos:prefLabel} distincts — une liste dépourvue de libellé lg2 ne rend donc
 * aucune ligne, au lieu d'un libellé lg2 vide.
 */
@Tag("integration")
class CodeListLabelByNotationIntegrationTest extends WithGraphDBContainer {

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(),
            new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    private final CodeListsQueries queries = new CodeListsQueries(
            BauhausUriPropertiesStub.stub(),
            new BauhausLanguagesProperties("fr", "en"),
            GraphsPropertiesStub.stub(),
            new PaginationProperties(5));

    @BeforeAll
    static void initData() {
        container.withTrigFiles("codes-list-label-by-notation-it.trig");
    }

    @Test
    void getCodeListLabelByNotation_returns_both_labels_of_the_code_list() throws RmesException {
        JSONObject result = repositoryGestion.getResponseAsObject(
                queries.getCodeListLabelByNotation("CL_LBL_BILINGUE"));

        assertThat(result.getString("labelLg1")).isEqualTo("Liste de codes bilingue (test)");
        assertThat(result.getString("labelLg2")).isEqualTo("Bilingual code list (test)");
    }

    @Test
    void getCodeListLabelByNotation_returns_nothing_when_the_code_list_has_no_label_in_lg2() throws RmesException {
        JSONObject result = repositoryGestion.getResponseAsObject(
                queries.getCodeListLabelByNotation("CL_LBL_FR_SEUL"));

        assertThat(result.isEmpty())
                .as("les deux FILTER lang() sont conjonctifs : sans libellé lg2, aucune ligne n'est rendue")
                .isTrue();
    }

    @Test
    void getCodeListLabelByNotation_ignores_a_code_carrying_the_requested_notation() throws RmesException {
        JSONObject result = repositoryGestion.getResponseAsObject(
                queries.getCodeListLabelByNotation("CL_LBL_CODE"));

        assertThat(result.isEmpty())
                .as("seules les ressources de type skos:ConceptScheme sont des listes de codes")
                .isTrue();
    }

    @Test
    void getCodeListLabelByNotation_returns_nothing_for_an_unknown_notation() throws RmesException {
        JSONObject result = repositoryGestion.getResponseAsObject(
                queries.getCodeListLabelByNotation("CL_LBL_INCONNUE"));

        assertThat(result.isEmpty()).isTrue();
    }
}
