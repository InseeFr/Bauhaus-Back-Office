package fr.insee.rmes.modules.users.domain.model;

import fr.insee.rmes.domain.auth.Source;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void shouldCreateUserWithAllParameters() {
        var user = new User("user123", List.of("ADMIN", "USER"), "STAMP-01", "insee");

        assertThat(user.id()).isEqualTo("user123");
        assertThat(user.roles()).containsExactly("ADMIN", "USER");
        assertThat(user.getStamp()).isEqualTo("STAMP-01");
        assertThat(user.source()).isEqualTo(Source.INSEE);
    }

    @Test
    void shouldCreateUserWithoutSource() {
        var user = new User("user123", List.of("ADMIN"), "STAMP-01");

        assertThat(user.id()).isEqualTo("user123");
        assertThat(user.roles()).containsExactly("ADMIN");
        assertThat(user.getStamp()).isEqualTo("STAMP-01");
        assertThat(user.source()).isNull();
    }

    @Test
    void shouldCreateUserWithStampObject() {
        var stamp = new Stamp("STAMP-02");
        var user = new User("user456", List.of("USER"), stamp, Source.SSM);

        assertThat(user.id()).isEqualTo("user456");
        assertThat(user.roles()).containsExactly("USER");
        assertThat(user.stamp()).isEqualTo(stamp);
        assertThat(user.getStamp()).isEqualTo("STAMP-02");
        assertThat(user.source()).isEqualTo(Source.SSM);
    }

    @Test
    void shouldCreateEmptyUser() {
        var emptyUser = User.EMPTY_USER;

        assertThat(emptyUser.id()).isNull();
        assertThat(emptyUser.roles()).isEmpty();
        assertThat(emptyUser.getStamp()).isEmpty();
        assertThat(emptyUser.source()).isNull();
    }

    @Test
    void shouldCheckIfUserHasRole_whenRoleExists() {
        var user = new User("user123", List.of("ADMIN", "USER", "MODERATOR"), "STAMP-01");

        assertThat(user.hasRole("ADMIN")).isTrue();
        assertThat(user.hasRole("USER")).isTrue();
        assertThat(user.hasRole("MODERATOR")).isTrue();
    }

    @Test
    void shouldCheckIfUserHasRole_whenRoleDoesNotExist() {
        var user = new User("user123", List.of("USER"), "STAMP-01");

        assertThat(user.hasRole("ADMIN")).isFalse();
        assertThat(user.hasRole("MODERATOR")).isFalse();
    }

    @Test
    void shouldCheckIfUserHasRole_whenNoRoles() {
        var user = new User("user123", List.of(), "STAMP-01");

        assertThat(user.hasRole("ADMIN")).isFalse();
    }

    @Test
    void shouldGenerateToString() {
        var user = new User("user123", List.of("ADMIN"), "STAMP-01", "insee");

        String result = user.toString();

        assertThat(result).contains("user123");
        assertThat(result).contains("ADMIN");
        assertThat(result).contains("STAMP-01");
        assertThat(result).contains("INSEE");
    }

    @Test
    void shouldHandleNullSource() {
        var user = new User("user123", List.of("ADMIN"), "STAMP-01", null);

        assertThat(user.source()).isNull();
    }

    @Test
    void shouldParseSourceFromString() {
        var userInsee = new User("user1", List.of("ADMIN"), "STAMP-01", "insee");
        var userSsm = new User("user2", List.of("USER"), "STAMP-02", "ssm");
        var userProconnect = new User("user3", List.of("USER"), "STAMP-03", "proconnect");

        assertThat(userInsee.source()).isEqualTo(Source.INSEE);
        assertThat(userSsm.source()).isEqualTo(Source.SSM);
        assertThat(userProconnect.source()).isEqualTo(Source.PROCONNECT);
    }
}
