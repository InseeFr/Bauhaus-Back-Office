package fr.insee.rmes.modules.ddi.physical_instances.webservice.response;

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