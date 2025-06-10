package fr.insee.rmes.bauhaus_services.code_list.export;

import java.util.List;

public record ExportedCodesList(String notation, String labelLg1, String labelLg2, List<ExportedCode> codes) {
}
