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
 * Réponse DDI 4 d'une StudyUnit et des PhysicalInstances qu'elle référence (#1145), sous la même
 * enveloppe de fil que {@link Ddi4Response} : {@code topLevelReferences} + {@code items}, les objets
 * étant discriminés par leur {@code $type} (voir {@link Ddi4Item}). Les listes typées restent
 * internes.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Ddi4StudyUnitResponse(
        @JsonIgnore String schema,
        @JsonIgnore List<Reference> topLevelReference,
        @JsonIgnore List<Ddi4StudyUnit> studyUnit,
        @JsonIgnore List<Ddi4PhysicalInstance> physicalInstance
) {

    /** Références de premier niveau de l'enveloppe. */
    @JsonProperty("topLevelReferences")
    public List<Reference> topLevelReferences() {
        return topLevelReference;
    }

    /**
     * La StudyUnit puis ses PhysicalInstances, à plat : le conteneur précède les objets qu'il
     * référence, comme dans la {@code FragmentInstance} XML. Toujours sérialisé, même vide : le
     * schéma l'exige.
     */
    @JsonProperty("items")
    public List<Ddi4Item> items() {
        return Stream.of(studyUnit, physicalInstance)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(Ddi4Item.class::cast)
                .toList();
    }

    /** Reconstruit les listes typées depuis l'enveloppe reçue, en dispatchant sur le {@code $type}. */
    @JsonCreator
    public static Ddi4StudyUnitResponse fromWire(
            @JsonProperty("topLevelReferences") List<Reference> topLevelReferences,
            @JsonProperty("items") List<Ddi4Item> items
    ) {
        List<Ddi4StudyUnit> studyUnits = new ArrayList<>();
        List<Ddi4PhysicalInstance> physicalInstances = new ArrayList<>();

        for (Ddi4Item item : items == null ? List.<Ddi4Item>of() : items) {
            switch (item) {
                case Ddi4StudyUnit it -> studyUnits.add(it);
                case Ddi4PhysicalInstance it -> physicalInstances.add(it);
                default -> throw new IllegalArgumentException(
                        "Type d'item DDI 4 non supporté : " + item.getClass().getSimpleName());
            }
        }

        return new Ddi4StudyUnitResponse(Ddi4Response.SCHEMA, topLevelReferences,
                studyUnits.isEmpty() ? null : studyUnits,
                physicalInstances.isEmpty() ? null : physicalInstances);
    }
}
