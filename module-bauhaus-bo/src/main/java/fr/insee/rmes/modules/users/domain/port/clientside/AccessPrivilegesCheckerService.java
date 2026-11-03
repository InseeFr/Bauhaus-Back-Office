package fr.insee.rmes.modules.users.domain.port.clientside;

import fr.insee.rmes.modules.commons.hexagonal.ClientSidePort;
import fr.insee.rmes.modules.users.domain.exceptions.MissingUserInformationException;
import java.util.Collection;

@ClientSidePort
public interface AccessPrivilegesCheckerService {

    boolean hasAccess(String module, String privilege, String id, Object user) throws MissingUserInformationException;

    /**
     * Droit de créer une ressource du module au nom des organisations données ({@code dc:creator}
     * ou {@code dc:contributor} du corps de la requête, en IRI ou en timbre). Sous la stratégie
     * STAMP, l'une d'elles au moins doit porter un timbre de l'utilisateur.
     */
    boolean hasCreateAccess(String module, Collection<String> owners, Object user)
            throws MissingUserInformationException;
}
