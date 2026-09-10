package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Sélection de la stratégie de récupération des code lists mutualisées, et configuration associée.
 *
 * <p>Propriétés (préfixe {@code fr.insee.rmes.bauhaus.colectica}) :
 * <pre>
 * # package-walk (défaut) ou configured-groups
 * fr.insee.rmes.bauhaus.colectica.mutualized-codes-strategy = configured-groups
 * # utilisé seulement par configured-groups : un objet par CodeListGroup (même forme que
 * # mutualized-codes-package : agency-id / identifier / version)
 * fr.insee.rmes.bauhaus.colectica.mutualized-codes-groups[0].agency-id = fr.insee
 * fr.insee.rmes.bauhaus.colectica.mutualized-codes-groups[0].identifier = 6b4fb2f8-901a-4b57-95a2-8517e761c241
 * fr.insee.rmes.bauhaus.colectica.mutualized-codes-groups[0].version = 1
 * </pre>
 *
 * @param mutualizedCodesStrategy stratégie active ; {@link Strategy#PACKAGE_WALK} par défaut.
 * @param mutualizedCodesGroups CodeListGroup interrogés par {@link Strategy#CONFIGURED_GROUPS} ;
 *                              liste vide par défaut.
 */
@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus.colectica")
public record MutualizedCodesProperties(Strategy mutualizedCodesStrategy, List<GroupRef> mutualizedCodesGroups) {
    public MutualizedCodesProperties {
        if (mutualizedCodesStrategy == null) {
            mutualizedCodesStrategy = Strategy.PACKAGE_WALK;
        }
        mutualizedCodesGroups = mutualizedCodesGroups == null ? List.of() : List.copyOf(mutualizedCodesGroups);
    }

    public enum Strategy {
        /** Parcours descendant du package racine : {@code package → CodeListScheme → CodeListGroup → CodeList}. */
        PACKAGE_WALK,
        /** Interrogation directe des CodeListGroup configurés (un appel relationship par groupe). */
        CONFIGURED_GROUPS
    }

    /**
     * Référence à un CodeListGroup mutualisé, de même forme que {@code mutualized-codes-package}.
     *
     * @param agencyId agency du groupe ; {@code null}/vide → on retombe sur le {@code defaultAgencyId}
     *                 de l'instance Colectica.
     * @param identifier identifiant du CodeListGroup.
     * @param version version (présente pour homogénéité avec le package ; non utilisée par la requête
     *                relationship, qui n'est pas versionnée).
     */
    public record GroupRef(String agencyId, String identifier, int version) {}
}
