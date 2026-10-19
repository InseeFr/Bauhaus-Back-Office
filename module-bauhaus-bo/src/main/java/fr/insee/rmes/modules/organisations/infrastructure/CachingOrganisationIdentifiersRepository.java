package fr.insee.rmes.modules.organisations.infrastructure;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import fr.insee.rmes.modules.organisations.domain.exceptions.OrganisationFetchException;
import fr.insee.rmes.modules.organisations.domain.model.CompactOrganisation;
import fr.insee.rmes.modules.organisations.domain.model.OrganisationSummary;
import fr.insee.rmes.modules.organisations.domain.port.serverside.OrganisationsRepository;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Mémoïse la correspondance {@code dcterms:identifier} ↔ {@code adms:identifier} lue à chaque
 * requête pour compléter les timbres de l'utilisateur. Une absence de correspondance est conservée,
 * un échec de lecture ne l'est pas. Les autres lectures sont déléguées telles quelles.
 */
public class CachingOrganisationIdentifiersRepository implements OrganisationsRepository {

    private final OrganisationsRepository delegate;
    /** Identifiant correspondant, ou {@link #NO_IDENTIFIER} quand le graphe n'en déclare aucun. */
    private final Cache<String, String> admsByDcterms;

    private final Cache<String, String> dctermsByAdms;

    private static final String NO_IDENTIFIER = "";

    public CachingOrganisationIdentifiersRepository(OrganisationsRepository delegate, Duration ttl) {
        this.delegate = delegate;
        this.admsByDcterms = Caffeine.newBuilder().expireAfterWrite(ttl).build();
        this.dctermsByAdms = Caffeine.newBuilder().expireAfterWrite(ttl).build();
    }

    @Override
    public CompactOrganisation getCompactOrganisation(String id) throws OrganisationFetchException {
        return delegate.getCompactOrganisation(id);
    }

    @Override
    public List<OrganisationSummary> getOrganisations() throws OrganisationFetchException {
        return delegate.getOrganisations();
    }

    @Override
    public List<CompactOrganisation> getCompactOrganisations(List<String> ids) throws OrganisationFetchException {
        return delegate.getCompactOrganisations(ids);
    }

    @Override
    public boolean checkIfOrganisationExists(String iri) throws OrganisationFetchException {
        return delegate.checkIfOrganisationExists(iri);
    }

    @Override
    public Optional<String> getDctermsIdentifier(String admsIdentifier) throws OrganisationFetchException {
        String cached = dctermsByAdms.getIfPresent(admsIdentifier);
        if (cached == null) {
            cached = delegate.getDctermsIdentifier(admsIdentifier).orElse(NO_IDENTIFIER);
            dctermsByAdms.put(admsIdentifier, cached);
        }
        return Optional.of(cached).filter(identifier -> !identifier.equals(NO_IDENTIFIER));
    }

    @Override
    public Optional<String> getAdmsIdentifier(String dctermsIdentifier) throws OrganisationFetchException {
        String cached = admsByDcterms.getIfPresent(dctermsIdentifier);
        if (cached == null) {
            cached = delegate.getAdmsIdentifier(dctermsIdentifier).orElse(NO_IDENTIFIER);
            admsByDcterms.put(dctermsIdentifier, cached);
        }
        return Optional.of(cached).filter(identifier -> !identifier.equals(NO_IDENTIFIER));
    }
}
