package fr.insee.rmes.domain.codeslist.model;

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
    
    public CodesListDomain(String id, String uri, String labelLg1, String labelLg2, String range) {
        this.id = id;
        this.uri = uri;
        this.labelLg1 = labelLg1;
        this.labelLg2 = labelLg2;
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
    
    @Override
    public String toString() {
        return "CodesListDomain{" +
                "id='" + id + '\'' +
                ", uri='" + uri + '\'' +
                ", labelLg1='" + labelLg1 + '\'' +
                ", labelLg2='" + labelLg2 + '\'' +
                ", range='" + range + '\'' +
                '}';
    }
}