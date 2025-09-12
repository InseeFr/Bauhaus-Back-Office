package fr.insee.rmes.bauhaus_services.operations.series.validation;

import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.model.operations.Series;

public interface SeriesValidator {
    void validate(Series series) throws RmesException;
}