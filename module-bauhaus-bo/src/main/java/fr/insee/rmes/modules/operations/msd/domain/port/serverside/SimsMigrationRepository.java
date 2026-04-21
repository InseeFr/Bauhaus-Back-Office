package fr.insee.rmes.modules.operations.msd.domain.port.serverside;

import fr.insee.rmes.modules.commons.domain.GenericInternalServerException;
import fr.insee.rmes.modules.operations.msd.domain.model.SimsConvertedTextNode;
import fr.insee.rmes.modules.operations.msd.domain.model.SimsTextNode;

import java.util.List;

public interface SimsMigrationRepository {
    List<SimsTextNode> findHtmlTextNodes(int limit, int offset) throws GenericInternalServerException;
    void bulkUpdateGestionTextNodes(List<SimsConvertedTextNode> nodes) throws GenericInternalServerException;

    List<SimsTextNode> findPublicationHtmlTextNodes(int limit, int offset) throws GenericInternalServerException;
    void bulkUpdatePublicationTextNodes(List<SimsConvertedTextNode> nodes) throws GenericInternalServerException;
}
