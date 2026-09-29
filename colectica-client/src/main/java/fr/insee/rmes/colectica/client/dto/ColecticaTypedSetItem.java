package fr.insee.rmes.colectica.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** One entry of a {@code _query/set} response: the item reference and its item type. */
public record ColecticaTypedSetItem(
        @JsonProperty("Item1") ColecticaSetItem reference,
        @JsonProperty("Item2") String itemType) {}
