package fr.insee.rmes.modules.operations.documents.domain;

import fr.insee.rmes.modules.operations.documents.domain.port.serverside.SimsOwnersLookup;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InMemorySimsOwnersLookup implements SimsOwnersLookup {

    private final Map<String, List<String>> owners = new HashMap<>();

    public void owners(String simsId, List<String> stamps) {
        owners.put(simsId, stamps);
    }

    @Override
    public List<String> ownersOf(String simsId) {
        return owners.getOrDefault(simsId, List.of());
    }
}
