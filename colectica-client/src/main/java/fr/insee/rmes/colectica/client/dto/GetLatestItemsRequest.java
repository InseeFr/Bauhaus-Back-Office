package fr.insee.rmes.colectica.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.rmes.colectica.client.ItemReference;
import java.util.List;

/** Body of {@code POST item/_getListLatest}: versionless references, resolved to their latest version. */
public record GetLatestItemsRequest(
        @JsonProperty("Identifiers") List<ItemReference> identifiers) {}
