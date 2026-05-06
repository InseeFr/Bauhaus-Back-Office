package fr.insee.rmes.modules.users.domain.model;

public record Stamp(String stamp) {
    @Override
    public String toString() {
        return stamp;
    }
}
