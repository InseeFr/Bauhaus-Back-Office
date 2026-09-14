package fr.insee.rmes.bauhaus_services.operations.series.validation;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.bauhaus_services.utils.OrganisationLookup;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.model.links.OperationsLink;
import fr.insee.rmes.modules.operations.series.domain.model.Series;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component()
@Profile("!insee")
public class SeriesDefaultValidator implements SeriesValidator {

    private final RepositoryGestion repositoryGestion;
    private final BauhausLanguagesProperties languages;
    private final OperationSeriesQueries operationSeriesQueries;
    private final OrganisationLookup organisationLookup;

    public SeriesDefaultValidator(
            RepositoryGestion repositoryGestion,
            BauhausLanguagesProperties languages,
            OperationSeriesQueries operationSeriesQueries,
            OrganisationLookup organisationLookup) {

        this.repositoryGestion = repositoryGestion;
        this.languages = languages;
        this.operationSeriesQueries = operationSeriesQueries;
        this.organisationLookup = organisationLookup;
    }

    @Override
    public void validate(Series series) throws RmesException {
        if (repositoryGestion.getResponseAsBoolean(operationSeriesQueries.checkPrefLabelUnicity(
                series.getId(), series.getPrefLabelLg1(), languages.lg1()))) {
            throw new RmesBadRequestException(
                    ErrorCodes.OPERATION_SERIES_EXISTING_PREF_LABEL_LG1,
                    "This prefLabelLg1 is already used by another series.");
        }
        if (repositoryGestion.getResponseAsBoolean(operationSeriesQueries.checkPrefLabelUnicity(
                series.getId(), series.getPrefLabelLg2(), languages.lg2()))) {
            throw new RmesBadRequestException(
                    ErrorCodes.OPERATION_SERIES_EXISTING_PREF_LABEL_LG2,
                    "This prefLabelLg2 is already used by another series.");
        }
        validateOrganisations(series);
    }

    private void validateOrganisations(Series series) throws RmesException {
        if (organisationLookup == null) {
            return;
        }
        List<String> values = collectOrganisationValues(series);
        if (values.isEmpty()) {
            return;
        }
        List<String> unknown = organisationLookup.findUnknown(values);
        if (!unknown.isEmpty()) {
            throw new RmesBadRequestException("Unknown organisation references: " + unknown);
        }
    }

    private static List<String> collectOrganisationValues(Series series) {
        List<String> values = new ArrayList<>();
        if (series.getCreators() != null) {
            values.addAll(series.getCreators());
        }
        addLinkIds(values, series.getContributors());
        addLinkIds(values, series.getPublishers());
        addLinkIds(values, series.getDataCollectors());
        return values;
    }

    private static void addLinkIds(List<String> target, List<OperationsLink> links) {
        if (links == null) {
            return;
        }
        for (OperationsLink link : links) {
            if (link != null && !link.isEmpty()) {
                target.add(link.getId());
            }
        }
    }
}
