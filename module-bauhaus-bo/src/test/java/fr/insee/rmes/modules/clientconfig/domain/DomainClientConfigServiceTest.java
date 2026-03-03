package fr.insee.rmes.modules.clientconfig.domain;

import fr.insee.rmes.modules.clientconfig.domain.model.ClientConfigProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DomainClientConfigServiceTest {

    private DomainClientConfigService domainClientConfigService;

    @BeforeEach
    void setUp() {
        domainClientConfigService = new DomainClientConfigService(
                "http://localhost:3000",
                "350",
                "DG75-L201",
                "fr",
                "en",
                "dev",
                List.of("concepts", "classifications"),
                List.of("concepts", "classifications", "operations"),
                "1.0.0",
                List.of("altLabel"),
                "fr.insee",
                List.of("fr-FR", "en-GB"),
                true
        );
    }

    @Test
    void should_return_init_properties_with_all_fields() {
        ClientConfigProperties properties = domainClientConfigService.getClientConfigProperties();

        assertThat(properties.appHost()).isEqualTo("http://localhost:3000");
        assertThat(properties.defaultContributor()).isEqualTo("DG75-L201");
        assertThat(properties.maxLengthScopeNote()).isEqualTo("350");
        assertThat(properties.lg1()).isEqualTo("fr");
        assertThat(properties.lg2()).isEqualTo("en");
        assertThat(properties.authType()).isEqualTo("NoAuthImpl");
        assertThat(properties.version()).isEqualTo("1.0.0");
        assertThat(properties.defaultAgencyId()).isEqualTo("fr.insee");
        assertThat(properties.activeModules()).containsExactly("concepts", "classifications");
        assertThat(properties.modules()).containsExactly("concepts", "classifications", "operations");
        assertThat(properties.extraMandatoryFields()).containsExactly("altLabel");
        assertThat(properties.colecticaLangs()).containsExactly("fr-FR", "en-GB");
        assertThat(properties.enableDevTools()).isTrue();
    }

    @Test
    void should_return_openid_connect_auth_for_pre_prod() {
        domainClientConfigService = new DomainClientConfigService(
                "http://localhost:3000", "350", "DG75-L201",
                "fr", "en", "pre-prod",
                List.of(), List.of(), "1.0.0",
                List.of(), "fr.insee", List.of(), true
        );

        ClientConfigProperties properties = domainClientConfigService.getClientConfigProperties();

        assertThat(properties.authType()).isEqualTo("OpenIDConnectAuth");
    }

    @Test
    void should_return_openid_connect_auth_for_prod() {
        domainClientConfigService = new DomainClientConfigService(
                "http://localhost:3000", "350", "DG75-L201",
                "fr", "en", "prod",
                List.of(), List.of(), "1.0.0",
                List.of(), "fr.insee", List.of(), true
        );

        ClientConfigProperties properties = domainClientConfigService.getClientConfigProperties();

        assertThat(properties.authType()).isEqualTo("OpenIDConnectAuth");
    }

    @Test
    void should_return_openid_connect_auth_for_PROD() {
        domainClientConfigService = new DomainClientConfigService(
                "http://localhost:3000", "350", "DG75-L201",
                "fr", "en", "PROD",
                List.of(), List.of(), "1.0.0",
                List.of(), "fr.insee", List.of(), true
        );

        ClientConfigProperties properties = domainClientConfigService.getClientConfigProperties();

        assertThat(properties.authType()).isEqualTo("OpenIDConnectAuth");
    }

    @Test
    void should_return_no_auth_impl_for_dev_environment() {
        ClientConfigProperties properties = domainClientConfigService.getClientConfigProperties();

        assertThat(properties.authType()).isEqualTo("NoAuthImpl");
    }
}