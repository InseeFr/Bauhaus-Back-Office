package fr.insee.rmes.bauhaus_services.code_list.export;

import fr.insee.rmes.domain.exceptions.RmesException;

public interface CodesListExport {
    ExportedCodesList exportCodesList(String notation) throws RmesException;
}
