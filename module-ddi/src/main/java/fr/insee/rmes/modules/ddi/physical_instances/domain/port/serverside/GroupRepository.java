package fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Server-side port for persisting DDI Group items.
 * <p>
 * Extends the generic {@link DdiItemRepository} with Group-specific operations
 * such as deprecating a targeted set of groups.
 */
public interface GroupRepository extends DdiItemRepository<Ddi4Group> {

    List<PartialGroup> getAll();

    /**
     * Le groupe d'identifiant {@code id}, ou {@link Optional#empty()} s'il est absent du dépôt DDI.
     * Toute autre défaillance du dépôt remonte : un groupe introuvable et un dépôt injoignable ne
     * veulent pas dire la même chose.
     */
    Optional<Ddi4Group> find(String agencyId, String id);

    /**
     * Deprecates only the existing groups whose identifier is in {@code groupIds}. Groups absent
     * from Colectica are ignored; an empty collection deprecates nothing.
     */
    void deprecate(Collection<String> groupIds);
}
