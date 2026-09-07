package fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside;

import fr.insee.rmes.modules.commons.hexagonal.ServerSidePort;

/**
 * Accès au document JSON Schema DDI 4. Le schéma est une ressource livrée avec l'application, mais
 * sa lecture reste de l'infrastructure : le domaine ne connaît que ce contrat.
 */
@ServerSidePort
public interface Ddi4SchemaRepository {

    /** @return le document JSON Schema, débarrassé de son BOM UTF-8 */
    String schemaDocument();
}
