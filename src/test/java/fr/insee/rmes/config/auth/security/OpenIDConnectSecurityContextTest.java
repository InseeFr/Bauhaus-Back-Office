package fr.insee.rmes.config.auth.security;

import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONObject;
import com.nimbusds.jwt.JWTParser;
import fr.insee.rmes.config.auth.user.User;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext.LOG_INFO_DEFAULT_STAMP;
import static fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext.TIMBRE_ANONYME;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;

class OpenIDConnectSecurityContextTest {

    private static ByteArrayOutputStream logOutputStream=new ByteArrayOutputStream();
    private static final String idep = "zzzzzz";
    private static final String timbre="DR59-SNDI";
    private static final List<String> roles = List.of("role1", "role2", "role3", "role4", "role5");

    private static final JSONArray rolesOfAccount=initJsonArray("manage-account",
                "manage-account-links",
                "view-profile");

    private static final JSONArray allowedOrigins = initJsonArray("https://gestion-metadonnees.insee.fr",
            "https://preprod.gestion-metadonnees.insee.fr",
            "https://test-gestion-metadonnees.insee.fr");

    private static final JSONArray rolesOfJwt = initJsonArray(roles);

    private static final JSONArray rolesOfRealmAccess = rolesOfJwt;

    private static final Map<String, Object> jwtDecoded=initJwtDecoded();


    private static JSONArray initJsonArray(List<String> elements) {
        var retour=new JSONArray();
        retour.addAll(elements);
        return retour;
    }

    private static JSONArray initJsonArray(String... elements) {
        return initJsonArray(List.of(elements));
    }

    private static Map<String,Object> initJwtDecoded() {
        Map<String, Object> retour =new HashMap<>();
        retour.put("sub", "f:fblabbalbalba:ZZZZZZ");
        retour.put("resource_access", new JSONObject(Map.of(
                "account", new JSONObject(Map.of(
                        "roles", rolesOfAccount)
                )
        )));
        retour.put("matricule", "0000000000054");
        retour.put("allowed-origins", allowedOrigins);
        retour.put("roles", rolesOfJwt);
        retour.put("iss", "https://blabla.fr/a/r/a");
        retour.put("typ", "Bearer");
        retour.put("preferred_username", idep);
        retour.put("given_name", "Fabrice");
        retour.put("sid", "c3000000-0000-0000-0000-000000000");
        retour.put("aud", List.of("account"));
        retour.put("timbre", timbre);
        retour.put("realm_access", new JSONObject(Map.of("roles", rolesOfRealmAccess)));
        retour.put("idminefi", "1010101010101");
        retour.put("azp", "blblblblbl-frontend");
        retour.put("auth_time", 1693372846);
        retour.put("scope", "insee-timbre-matricule");
        retour.put("name", "Fabrice Vivonne");
        retour.put("exp", "Wed Aug 30 15,15,33 CEST 2023");
        retour.put("session_state", "c000000-0000-0000-0000-00000000000");
        retour.put("iat", "Wed Aug 30 15,10,33 CEST 2023");
        retour.put("family_name", "Vivonne");
        retour.put("jti", "000000-0000-0000-0000-000000000");
        retour.put("email", "fabrice.vivonne@insee.fr");
        return retour;
    }

    @BeforeAll
    static void configureLog4j(){

        final Configuration config = ((LoggerContext) LogManager.getContext(false)).getConfiguration();
        final Appender appender = createOutputStreamAppender(config);
        addAppenderEverywhere(config, appender);
        forceInfoForRootLogger(config);
    }

    private static void addAppenderEverywhere(Configuration config, Appender appender) {
        config.addAppender(appender);
        config.getRootLogger().addAppender(appender, Level.INFO, null);
        for (final LoggerConfig loggerConfig : config.getLoggers().values()) {
            loggerConfig.addAppender(appender, Level.INFO, null);
        }
    }

    @NotNull
    private static Appender createOutputStreamAppender(Configuration config) {
        final PatternLayout layout = PatternLayout.createDefaultLayout(config);
        final Appender appender = OutputStreamAppender.createAppender(layout, null, logOutputStream, "StdoutTest", false, true);
        appender.start();
        return appender;
    }

    private static void forceInfoForRootLogger(Configuration config) {
        config.getRootLogger().setLevel(Level.INFO);
        config.getRootLogger().setAdditive(true);
    }

    @Test
    void testJwt() throws ParseException, JSONException {
        var oidcContext = new OpenIDConnectSecurityContext(empty(), "timbre", "realm_access", "preferred_username", false, "roles");

        User user = oidcContext.buildUserFromToken(Optional.of(jwtDecoded));
        assertThat(user.getId()).isEqualTo(idep);
        assertThat(user.getStamp()).isEqualTo(timbre);
        assertThat(user.getRoles()).isEqualTo(new org.json.JSONArray(roles));
    }

    @Test
    void testJwt_sansTimbre() throws ParseException, JSONException {
        logOutputStream.reset();

        var oidcContext = new OpenIDConnectSecurityContext(empty(), "timbr", "realm_access", "preferred_username", false, "roles");

        User user = oidcContext.buildUserFromToken(Optional.of(jwtDecoded));
        assertThat(user.getId()).isEqualTo(idep);
        assertThat(user.getStamp()).isEqualTo(TIMBRE_ANONYME);
        assertThat(user.getRoles()).isEqualTo(new org.json.JSONArray(roles));
        assertThat(logOutputStream.toString().trim()).hasToString(String.format(LOG_INFO_DEFAULT_STAMP.replace("{}", "%s"), idep));
        logOutputStream.reset();
    }
}