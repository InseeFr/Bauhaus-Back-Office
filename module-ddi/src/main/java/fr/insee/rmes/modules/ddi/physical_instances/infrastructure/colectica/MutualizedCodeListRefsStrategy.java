package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.colectica.client.ItemReference;

import java.util.List;

/**
 * Source of truth for "what is mutualized": the set of CodeList references (agency/identifier) to
 * expose via {@code GET /ddi/mutualized-codes-list}.
 *
 * <p>Two interchangeable implementations exist, selected by
 * {@code fr.insee.rmes.bauhaus.colectica.mutualized-codes-strategy}:
 * <ul>
 *   <li>{@link MutualizedCodeListRefsProvider} — walks the configured root package top-down
 *       ({@code package → CodeListScheme → CodeListGroup → CodeList}).</li>
 *   <li>{@link ConfiguredGroupsCodeListRefsProvider} — queries directly the CodeListGroup
 *       identifiers listed in configuration (one relationship call per group), avoiding the upper
 *       levels of the walk.</li>
 * </ul>
 */
public interface MutualizedCodeListRefsStrategy {

    /**
     * Returns every mutualized CodeList reference, deduplicated and in resolution order.
     * Empty when nothing is configured.
     */
    List<ItemReference> codeListRefs();
}
