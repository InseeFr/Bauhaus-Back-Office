package fr.insee.rmes.bauhaus_services.operations.series.validation;

import fr.insee.rmes.bauhaus_services.utils.OrganisationLookup;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.model.links.OperationsLink;
import fr.insee.rmes.modules.operations.series.domain.model.Series;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeriesDefaultValidatorTest {

    @Mock
    private RepositoryGestion repositoryGestion;

    @Mock
    private OperationSeriesQueries operationSeriesQueries;

    @Mock
    private OrganisationLookup organisationLookup;

    private SeriesDefaultValidator validator() {
        return new SeriesDefaultValidator(repositoryGestion, "fr", "en", operationSeriesQueries, organisationLookup);
    }

    private Series newSeries() {
        Series series = new Series();
        series.setId("s1");
        series.setPrefLabelLg1("label fr");
        series.setPrefLabelLg2("label en");
        return series;
    }

    @Test
    void validate_throwsBadRequest_whenACreatorIsUnknown() throws RmesException {
        lenient().when(repositoryGestion.getResponseAsBoolean(any())).thenReturn(false);
        when(organisationLookup.findUnknown(any())).thenReturn(List.of("http://bauhaus/organisations/MISSING"));

        Series series = newSeries();
        series.setCreators(List.of("http://bauhaus/organisations/MISSING"));

        assertThatThrownBy(() -> validator().validate(series))
                .isInstanceOf(RmesBadRequestException.class)
                .satisfies(t -> assertThat(((RmesBadRequestException) t).getDetails()).contains("MISSING"));
    }

    @Test
    void validate_passesThrough_whenAllOrganisationsAreKnown() throws RmesException {
        lenient().when(repositoryGestion.getResponseAsBoolean(any())).thenReturn(false);
        when(organisationLookup.findUnknown(any())).thenReturn(List.of());

        Series series = newSeries();
        series.setCreators(List.of("http://bauhaus/organisations/DG75-A001"));
        OperationsLink contributor = new OperationsLink();
        contributor.id = "http://bauhaus/organisations/DG75-B002";
        series.setContributors(List.of(contributor));

        validator().validate(series); // no exception
    }

    @Test
    void validate_collectsOrganisationsFromAllFields_creatorsContributorsPublishersDataCollectors() throws RmesException {
        lenient().when(repositoryGestion.getResponseAsBoolean(any())).thenReturn(false);
        when(organisationLookup.findUnknown(any())).thenAnswer(invocation -> {
            List<String> values = invocation.getArgument(0);
            assertThat(values).containsExactlyInAnyOrder(
                    "creator-iri",
                    "contributor-iri",
                    "publisher-iri",
                    "datacollector-iri");
            return List.of();
        });

        Series series = newSeries();
        series.setCreators(List.of("creator-iri"));
        OperationsLink contributor = new OperationsLink();
        contributor.id = "contributor-iri";
        series.setContributors(List.of(contributor));
        OperationsLink publisher = new OperationsLink();
        publisher.id = "publisher-iri";
        series.setPublishers(List.of(publisher));
        OperationsLink dataCollector = new OperationsLink();
        dataCollector.id = "datacollector-iri";
        series.setDataCollectors(List.of(dataCollector));

        validator().validate(series);
    }
}
