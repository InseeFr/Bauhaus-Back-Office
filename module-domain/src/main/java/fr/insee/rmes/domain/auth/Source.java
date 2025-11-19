package fr.insee.rmes.domain.auth;

public enum Source {
    PROCONNECT("proconnect"),
    INSEE("insee"),
    SSM("ssm");

    private final String value;

    Source(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Source fromValue(String value) {
        if (value == null) {
            return null;
        }
        
        for (Source source : Source.values()) {
            if (source.value.equals(value)) {
                return source;
            }
        }
        
        throw new IllegalArgumentException("Unknown source value: " + value);
    }
}