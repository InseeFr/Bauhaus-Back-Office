package fr.insee.rmes.webservice.response.ddi;

import java.util.List;

public record ValidationResponse(
        boolean valid,
        List<String> errors
) {
    public static ValidationResponse success() {
        return new ValidationResponse(true, List.of());
    }

    public static ValidationResponse failure(List<String> errors) {
        return new ValidationResponse(false, errors);
    }
}