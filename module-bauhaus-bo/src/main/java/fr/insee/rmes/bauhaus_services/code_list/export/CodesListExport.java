package fr.insee.rmes.bauhaus_services.code_list.export;

import fr.insee.rmes.onion.domain.exceptions.RmesException;

public interface CodesListExport {
    ExportedCodesList exportCodesList(String notation) throws RmesException;
}
