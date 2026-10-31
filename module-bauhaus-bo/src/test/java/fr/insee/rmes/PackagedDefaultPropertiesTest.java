package fr.insee.rmes;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.Properties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * config/bauhaus-local-dev.properties n'est pas dans le jar : toute clé lue sans valeur par défaut
 * et indépendante de l'environnement doit donc avoir sa valeur dans bauhaus-core.properties, sinon
 * le contexte ne démarre pas sur un environnement qui ne la redéfinit pas (régression 4.22.0-rc1).
 */
class PackagedDefaultPropertiesTest {

    private static Properties core;

    @BeforeAll
    static void loadPackagedProperties() throws IOException {
        core = PropertiesLoaderUtils.loadProperties(new ClassPathResource("bauhaus-core.properties"));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "fr.insee.rmes.bauhaus.storage.document.gestion",
                "fr.insee.rmes.bauhaus.storage.document.publication",
                "fr.insee.rmes.bauhaus.storage.document.publication.interne",
                "fr.insee.web4g.baseURL",
                "fr.insee.rmes.rdf.backend",
                "storage.directoryGestion",
                "storage.directoryPublication",
                "jwt.stampClaim",
                "jwt.roleClaim",
                "jwt.idClaim",
                "jwt.sourceClaim",
                "jwt.inseeGroupClaim",
                "jwt.roleClaimConfig.roles",
                "jwt.hieApplicationPrefix"
            })
    void environmentIndependentPropertyHasAPackagedDefault(String key) {
        assertNotNull(core.getProperty(key), key + " n'a pas de valeur par défaut dans le jar");
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "fr.insee.rmes.bauhaus.cors.allowedOrigin",
                "fr.insee.rmes.bauhaus.rdf.auth",
                "minio.accessName",
                "minio.secretKey",
                "fr.insee.rmes.bauhaus.keycloak.defaultrealm.clientsecret",
                "fr.insee.rmes.bauhaus.keycloak.colecticarealm.clientsecret"
            })
    void localDevOnlyPropertyStaysOutOfTheJar(String key) {
        assertNull(core.getProperty(key), key + " ne doit pas avoir de valeur par défaut dans le jar");
    }
}
