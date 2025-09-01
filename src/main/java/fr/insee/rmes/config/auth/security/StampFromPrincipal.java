package fr.insee.rmes.config.auth.security;


import fr.insee.rmes.onion.domain.model.Stamp;

import java.util.Optional;

public interface StampFromPrincipal {

    Optional<Stamp> findStamp(Object principal);

}
