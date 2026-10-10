package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import java.util.Date;
import java.util.List;

/** Group fixtures shared by the service tests of this package. */
final class PartialGroupFixtures {

    private PartialGroupFixtures() {}

    /** Three groups labelled "alpha", "Charlie" and "Bravo", in that (unsorted) order. */
    static List<PartialGroup> groupsInUnsortedOrder() {
        return List.of(
                new PartialGroup("g-a", "alpha", new Date(), "fr.insee", List.of()),
                new PartialGroup("g-c", "Charlie", new Date(), "fr.insee", List.of()),
                new PartialGroup("g-b", "Bravo", new Date(), "fr.insee", List.of()));
    }
}
