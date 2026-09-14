package fr.insee.rmes.bauhaus_services.operations.series.validation;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.bauhaus_services.utils.OrganisationLookup;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.modules.operations.series.domain.model.Series;
import fr.insee.rmes.persistance.sparql_queries.operations.OperationSeriesQueries;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("insee")
public class SeriesInseeValidator implements SeriesValidator {

    private final RepositoryGestion repositoryGestion;
    private final BauhausLanguagesProperties languages;
    private final OperationSeriesQueries operationSeriesQueries;
    private final OrganisationLookup organisationLookup;

    public SeriesInseeValidator(
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
        new SeriesDefaultValidator(repositoryGestion, languages, operationSeriesQueries, organisationLookup)
                .validate(series);

        if (series.getAccrualPeriodicityCode() == null) {
            throw new RmesBadRequestException("The property accrualPeriodicityCode is required");
        }
        if (series.getTypeCode() == null) {
            throw new RmesBadRequestException("The property typeCode is required");
        }
    }
}
