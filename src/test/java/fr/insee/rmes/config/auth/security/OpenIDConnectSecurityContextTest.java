package fr.insee.rmes.config.auth.security;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.OutputStreamAppender;
import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONObject;
import fr.insee.rmes.config.auth.user.User;
import fr.insee.rmes.exceptions.RmesException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext.LOG_INFO_DEFAULT_STAMP;
import static fr.insee.rmes.config.auth.security.OpenIDConnectSecurityContext.TIMBRE_ANONYME;
import static org.assertj.core.api.Assertions.assertThat;

class OpenIDConnectSecurityContextTest {

    private static final ByteArrayOutputStream logOutputStream=new ByteArrayOutputStream();
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
        LoggerContext context = ((LoggerContext) LoggerFactory.getILoggerFactory());
        addAppenderToOidcSecurityContextLogger(createOutputStreamAppender(context), context);
    }

    private static void addAppenderToOidcSecurityContextLogger(Appender<ILoggingEvent> appender, LoggerContext context) {
        var oidcSecurityContextLogger = context.getLogger(OpenIDConnectSecurityContext.class);
        oidcSecurityContextLogger.setLevel(Level.INFO);
        oidcSecurityContextLogger.addAppender(appender);
    }

    @NotNull
    private static Appender<ILoggingEvent> createOutputStreamAppender(LoggerContext context) {
        PatternLayoutEncoder ple=new PatternLayoutEncoder();
        ple.setPattern("%msg");
        ple.setContext(context);
        ple.start();
        var appender = new OutputStreamAppender<ILoggingEvent>();
        appender.setContext(context);
        appender.setEncoder(ple);
        appender.setOutputStream(logOutputStream);
        appender.start();

        return appender;
    }

    @Test
    void testJwt() throws RmesException {
        var oidcContext = new OpenIDConnectSecurityContext("timbre", "realm_access", "preferred_username", false, "roles");

        User user = oidcContext.buildUserFromToken(jwtDecoded);
        assertThat(user.id()).isEqualTo(idep);
        assertThat(user.getStamp()).isEqualTo(timbre);
        assertThat(user.roles()).isEqualTo(roles);
    }

    @Test
    void testJwt_sansTimbre() throws RmesException {
        logOutputStream.reset();

        var oidcContext = new OpenIDConnectSecurityContext( "timbr", "realm_access", "preferred_username", false, "roles");

        User user = oidcContext.buildUserFromToken(jwtDecoded);
        assertThat(user.id()).isEqualTo(idep);
        assertThat(user.getStamp()).isEqualTo(TIMBRE_ANONYME);
        assertThat(user.roles()).isEqualTo(roles);
        assertThat(logOutputStream.toString().trim()).hasToString(String.format(LOG_INFO_DEFAULT_STAMP.replace("{}", "%s"), idep));
        logOutputStream.reset();
    }
}