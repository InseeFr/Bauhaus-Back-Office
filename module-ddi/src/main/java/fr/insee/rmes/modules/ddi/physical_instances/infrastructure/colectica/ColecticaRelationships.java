package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.RelationshipDirection;
import java.util.List;

/** Marches dans le graphe de relations Colectica. */
final class ColecticaRelationships {

    private ColecticaRelationships() {
    }

    /**
     * Descente {@code bysubject} niveau par niveau depuis {@code root}, chaque étape étant filtrée
     * côté serveur sur le type enfant attendu. Renvoie les références du dernier niveau, ou une liste
     * vide dès qu'un niveau intermédiaire est vide.
     */
    static List<ItemReference> descend(
        ColecticaClient colecticaClient, ItemReference root, List<String> childTypes
    ) {
        List<ItemReference> refs = List.of(root);
        for (String childType : childTypes) {
            refs = refs.stream()
                .flatMap(ref -> colecticaClient.findRelatedDescriptions(
                    RelationshipDirection.BY_SUBJECT, ref, List.of(childType)).stream())
                .distinct()
                .toList();
            if (refs.isEmpty()) {
                return List.of();
            }
        }
        return refs;
    }

    /** Identifiants des items de {@code childType} directement référencés par {@code parent}. */
    static List<ItemReference> childrenOfType(
        ColecticaClient colecticaClient, ItemReference parent, String childType
    ) {
        return colecticaClient.findRelatedDescriptions(
            RelationshipDirection.BY_SUBJECT, parent, List.of(childType));
    }
}
