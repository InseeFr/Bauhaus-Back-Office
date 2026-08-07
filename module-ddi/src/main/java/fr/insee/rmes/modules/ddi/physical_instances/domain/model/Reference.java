package fr.insee.rmes.modules.ddi.physical_instances.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DDI 4 reference (unified type for all DDI 3 reference variants).
 * <p>
 * The {@code type} field is the DDI 4 {@code $type} discriminator: one of the
 * 161 substitution group values (see {@link Ddi4ObjectType}). The URN follows
 * the {@code urn:ddi:<agency>:<id>:<version>} pattern; use {@link #of} to
 * synthesise it from the identity fields.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Reference(
        @JsonProperty("$type") String type,
        @JsonProperty("URN") String urn,
        @JsonProperty("Agency") String agency,
        @JsonProperty("ID") String id,
        @JsonProperty("Version") String version
) {

    public static Reference of(String agency, String id, String version, String type) {
        return new Reference(type, synthesizeUrn(agency, id, version), agency, id, version);
    }

    public static Reference of(String agency, String id, String version, Ddi4ObjectType type) {
        return of(agency, id, version, type.name());
    }

    public static String synthesizeUrn(String agency, String id, String version) {
        return "urn:ddi:" + agency + ":" + id + ":" + version;
    }
}
