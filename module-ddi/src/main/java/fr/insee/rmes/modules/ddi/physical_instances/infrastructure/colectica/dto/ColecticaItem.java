package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record ColecticaItem(
    @JsonProperty("Summary")
    Map<String, Object> summary,
    
    @JsonProperty("ItemName")
    Map<String, String> itemName,

    @JsonProperty("Label")
    Map<String, String> label,
    
    @JsonProperty("Description")
    Map<String, String> description,
    
    @JsonProperty("VersionRationale")
    Map<String, String> versionRationale,
    
    @JsonProperty("MetadataRank")
    Integer metadataRank,

    @JsonProperty("RepositoryName")
    String repositoryName,

    @JsonProperty("IsAuthoritative")
    Boolean isAuthoritative,
    
    @JsonProperty("Tags")
    List<String> tags,
    
    @JsonProperty("ItemType")
    String itemType,
    
    @JsonProperty("AgencyId")
    String agencyId,
    
    @JsonProperty("Version")
    Integer version,
    
    @JsonProperty("Identifier")
    String identifier,
    
    @JsonProperty("Item")
    Object item,
    
    @JsonProperty("Notes")
    String notes,
    
    @JsonProperty("VersionDate")
    String versionDate,
    
    @JsonProperty("VersionResponsibility")
    String versionResponsibility,
    
    @JsonProperty("IsPublished")
    Boolean isPublished,

    @JsonProperty("IsDeprecated")
    Boolean isDeprecated,

    @JsonProperty("IsProvisional")
    Boolean isProvisional,
    
    @JsonProperty("ItemFormat")
    String itemFormat,
    
    @JsonProperty("TransactionId")
    Long transactionId,

    @JsonProperty("VersionCreationType")
    Integer versionCreationType
) {}