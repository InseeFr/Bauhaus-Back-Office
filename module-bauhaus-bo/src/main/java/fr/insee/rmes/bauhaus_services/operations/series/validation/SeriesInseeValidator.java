package fr.insee.rmes.bauhaus_services.operations.series.validation;

import fr.insee.rmes.infrastructure.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.model.operations.Series;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("insee")
public class SeriesInseeValidator implements SeriesValidator {

    private final RepositoryGestion repositoryGestion;
    private final String lg1;
    private final String lg2;

    public SeriesInseeValidator(
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
        new SeriesDefaultValidator(repositoryGestion, lg1, lg2).validate(series);

        if (series.getAccrualPeriodicityCode() == null) {
            throw new RmesBadRequestException("The property accrualPeriodicityCode is required");
        }
        if (series.getTypeCode() == null) {
            throw new RmesBadRequestException("The property typeCode is required");
        }

    }
}