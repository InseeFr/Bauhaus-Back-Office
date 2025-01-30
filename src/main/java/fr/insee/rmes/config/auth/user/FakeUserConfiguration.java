package fr.insee.rmes.config.auth.user;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;

@ConfigurationProperties(prefix = "fr.insee.rmes.bauhaus.fakeuser")
public final class FakeUserConfiguration {

    private Optional<String> name= empty();
    private List<String> roles=List.of();
    private Optional<String> stamp=empty();

    public void setName(Optional<String> name) {
        this.name = name;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void setStamp(Optional<String> stamp) {
        this.stamp = stamp;
    }

    public Optional<String> name() {
        return name;
    }

    public List<String> roles() {
        return roles;
    }

    public Optional<String> stamp() {
        return stamp;
    }
}
