package fr.insee.rmes.testcontainers.documentations;

import static org.assertj.core.api.Assertions.assertThat;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.config.GraphsPropertiesStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.RepositoryInitiator;
import fr.insee.rmes.graphdb.RepositoryUtils;
import fr.insee.rmes.json.JSONUtils;
import fr.insee.rmes.modules.operations.msd.infrastructure.graphdb.DocumentationQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.testcontainers.WithGraphDBContainer;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Les créateurs d'un rapport SIMS sont ceux de sa cible : la série, la série parente d'une opération
 * ou l'indicateur ({@code sdmx-mm:target/dcterms:isPartOf?/dc:creator}).
 */
@Tag("integration")
class SimsTargetCreatorsQueryIntegrationTest extends WithGraphDBContainer {

    private final RepositoryGestion repositoryGestion = new RepositoryGestion(
            getRdfGestionConnectionDetails(), new RepositoryUtils(null, RepositoryInitiator.Type.DISABLED));

    private final DocumentationQueries documentationQueries =
            new DocumentationQueries(new BauhausLanguagesProperties("fr", "en"), GraphsPropertiesStub.stub());

    @BeforeAll
    static void initData() {
        container.withTrigFiles("sims-target-creators-it.trig");
    }

    @ParameterizedTest(name = "rapport {0} : {1}")
    @CsvSource({
        "9801, http://bauhaus/organisations/insee/SERIES-9801",
        "9802, http://bauhaus/organisations/insee/SERIES-9801",
        "9803, http://bauhaus/organisations/insee/INDICATOR-9803"
    })
    void reads_the_dc_creator_of_the_sims_target_or_of_its_parent_series(String idSims, String creator)
            throws RmesException {
        var creators = JSONUtils.stream(
                        repositoryGestion.getResponseAsArray(documentationQueries.getTargetCreators(idSims)))
                .map(row -> row.getString("creators"))
                .toList();

        assertThat(creators).isEqualTo(List.of(creator));
    }
}
