package fr.insee.rmes.bauhaus_services.operations.series.validation;

import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.ErrorCodes;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.model.operations.Series;
import fr.insee.rmes.persistance.sparql_queries.operations.series.OpSeriesQueries;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component()
@Profile("!insee")
public class SeriesDefaultValidator implements SeriesValidator {

    private final RepositoryGestion repositoryGestion;
    private final String lg1;
    private final String lg2;

    public SeriesDefaultValidator(
            RepositoryGestion repositoryGestion,
            @Value("${fr.insee.rmes.bauhaus.lg1}") String lg1,
            @Value("${fr.insee.rmes.bauhaus.lg2}") String lg2
    ) {

        this.repositoryGestion = repositoryGestion;
        this.lg1 = lg1;
        this.lg2 = lg2;
    }

    @Override
    public void validate(Series series) throws RmesException {
        if (repositoryGestion.getResponseAsBoolean(OpSeriesQueries.checkPrefLabelUnicity(series.getId(), series.getPrefLabelLg1(), lg1))) {
            throw new RmesBadRequestException(ErrorCodes.OPERATION_SERIES_EXISTING_PREF_LABEL_LG1, "This prefLabelLg1 is already used by another series.");
        }
        if (repositoryGestion.getResponseAsBoolean(OpSeriesQueries.checkPrefLabelUnicity(series.getId(), series.getPrefLabelLg2(), lg2))) {
            throw new RmesBadRequestException(ErrorCodes.OPERATION_SERIES_EXISTING_PREF_LABEL_LG2, "This prefLabelLg2 is already used by another series.");
        }
    }
}
