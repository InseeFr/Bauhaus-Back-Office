package fr.insee.rmes.domain.codeslist.model;

import org.jspecify.annotations.Nullable;

/**
 * Pure domain model for codes list.
 * No dependencies on infrastructure or framework classes.
 */
public class CodesListDomain {
    
    private final String id;
    private final String uri;
    private final String labelLg1;
    private final String labelLg2;
    private final String range;
    private final String descriptionLg1;
    private final String descriptionLg2;
    private final String lastCodeUriSegment;
    @Nullable private final String created;
    private final String creator;
    private final String validationState;
    private final String disseminationStatus;
    private final String modified;
    private final String iriParent;

    public CodesListDomain(
            String uri,
            String id,
            String labelLg1,
            String labelLg2,
            String descriptionLg1,
            String descriptionLg2,
            String range,
            String lastCodeUriSegment,
            String created,
            String creator,
            String validationState,
            String disseminationStatus,
            String modified,
            String iriParent
            ) {
        this.uri = uri;
        this.id = id;
        this.labelLg1 = labelLg1;
        this.labelLg2 = labelLg2;
        this.descriptionLg1 = descriptionLg1;
        this.descriptionLg2 = descriptionLg2;
        this.lastCodeUriSegment = lastCodeUriSegment;
        this.created = created;
        this.creator = creator;
        this.validationState = validationState;
        this.disseminationStatus = disseminationStatus;
        this.modified = modified;
        this.iriParent = iriParent;
        this.range = range;
    }
    
    public String getId() {
        return id;
    }
    
    public String getUri() {
        return uri;
    }
    
    public String getLabelLg1() {
        return labelLg1;
    }
    
    public String getLabelLg2() {
        return labelLg2;
    }
    
    public String getRange() {
        return range;
    }

    public String getDescriptionLg1() {
        return descriptionLg1;
    }

    public String getDescriptionLg2() {
        return descriptionLg2;
    }

    public String getLastCodeUriSegment() {
        return lastCodeUriSegment;
    }

    public String getCreated() {
        return created;
    }

    public String getCreator() {
        return creator;
    }

    public String getValidationState() {
        return validationState;
    }

    public String getDisseminationStatus() {
        return disseminationStatus;
    }

    public String getModified() {
        return modified;
    }

    public String getIriParent() {
        return iriParent;
    }

    @Override
    public String toString() {
        var i=created.length();
        return "CodesListDomain{" +
                "id='" + id + '\'' +
                ", uri='" + uri + '\'' +
                ", labelLg1='" + labelLg1 + '\'' +
                ", labelLg2='" + labelLg2 + '\'' +
                ", range='" + range + '\'' +
                '}';
    }
}