package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Réponse DDI 4 échangée avec le front.
 * <p>
 * <b>Contrat de fil</b> — l'enveloppe du {@code ddi-schema.json} : exactement
 * {@code topLevelReferences} et {@code items}, ce dernier étant le tableau à plat de tous les
 * objets, discriminés par leur {@code $type} (voir {@link Ddi4VersionedItem}). Le schéma déclare
 * ces deux seules propriétés à la racine avec {@code additionalProperties: false} — d'où
 * l'absence du marqueur {@code $schema} et des clés groupées par type de la sérialisation
 * Colectica, qui invalidaient le payload.
 * <p>
 * <b>Structure interne</b> — les listes typées sont conservées : tout le back s'appuie dessus
 * (réconciliation des {@code VersionDate}, convertisseurs DDI 3, dépôt Colectica). Seule la
 * projection JSON change, via {@link #items()} en sortie et {@link #fromWire} en entrée.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Ddi4Response(
        @JsonIgnore String schema,
        @JsonIgnore List<Reference> topLevelReference,
        @JsonIgnore List<Ddi4PhysicalInstance> physicalInstance,
        @JsonIgnore List<Ddi4DataRelationship> dataRelationship,
        @JsonIgnore List<Ddi4Variable> variable,
        @JsonIgnore List<Ddi4CodeList> codeList,
        @JsonIgnore List<Ddi4Category> category,
        @JsonIgnore List<Ddi4ManagedMissingValuesRepresentation> managedMissingValuesRepresentation) {
    /** Identifiant du schéma DDI 4. Marqueur interne : il ne circule plus sur le fil. */
    public static final String SCHEMA = "ddi:4.0";

    /** Références de premier niveau de l'enveloppe. */
    @JsonProperty("topLevelReferences")
    public List<Reference> topLevelReferences() {
        return topLevelReference;
    }

    /**
     * Tous les items à plat, dans l'ordre PhysicalInstance, DataRelationship, Variable, CodeList,
     * Category, ManagedMissingValuesRepresentation. Toujours sérialisé, même vide : le schéma
     * l'exige.
     */
    @JsonProperty("items")
    public List<Ddi4VersionedItem> items() {
        return Stream.of(
                        physicalInstance,
                        dataRelationship,
                        variable,
                        codeList,
                        category,
                        managedMissingValuesRepresentation)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(Ddi4VersionedItem.class::cast)
                .toList();
    }

    /** Reconstruit les listes typées depuis l'enveloppe reçue, en dispatchant sur le {@code $type}. */
    @JsonCreator
    public static Ddi4Response fromWire(
            @JsonProperty("topLevelReferences") List<Reference> topLevelReferences,
            @JsonProperty("items") List<Ddi4VersionedItem> items) {
        List<Ddi4PhysicalInstance> physicalInstances = new ArrayList<>();
        List<Ddi4DataRelationship> dataRelationships = new ArrayList<>();
        List<Ddi4Variable> variables = new ArrayList<>();
        List<Ddi4CodeList> codeLists = new ArrayList<>();
        List<Ddi4Category> categories = new ArrayList<>();
        List<Ddi4ManagedMissingValuesRepresentation> missingValuesRepresentations = new ArrayList<>();

        for (Ddi4VersionedItem item : items == null ? List.<Ddi4VersionedItem>of() : items) {
            switch (item) {
                case Ddi4PhysicalInstance it -> physicalInstances.add(it);
                case Ddi4DataRelationship it -> dataRelationships.add(it);
                case Ddi4Variable it -> variables.add(it);
                case Ddi4CodeList it -> codeLists.add(it);
                case Ddi4Category it -> categories.add(it);
                case Ddi4ManagedMissingValuesRepresentation it -> missingValuesRepresentations.add(it);
                default ->
                    throw new IllegalArgumentException("Type d'item DDI 4 non supporté : "
                            + item.getClass().getSimpleName());
            }
        }

        return new Ddi4Response(
                SCHEMA,
                topLevelReferences,
                nullIfEmpty(physicalInstances),
                nullIfEmpty(dataRelationships),
                nullIfEmpty(variables),
                nullIfEmpty(codeLists),
                nullIfEmpty(categories),
                nullIfEmpty(missingValuesRepresentations));
    }

    /**
     * Une liste vide et une liste absente sont indistinguables sur le fil : on rend {@code null}
     * pour que la sérialisation suivante omette la section, comme avant la bascule d'enveloppe.
     */
    private static <T> List<T> nullIfEmpty(List<T> items) {
        return items.isEmpty() ? null : items;
    }
}
