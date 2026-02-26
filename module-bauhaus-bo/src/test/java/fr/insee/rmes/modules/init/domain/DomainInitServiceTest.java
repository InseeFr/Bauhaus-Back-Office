package fr.insee.rmes.modules.init.domain;

import fr.insee.rmes.BauhausConfiguration;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaConfiguration;
import fr.insee.rmes.modules.init.domain.model.InitProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class DomainInitServiceTest {

    private DomainInitService domainInitService;
    private ColecticaConfiguration colecticaConfiguration;
    private ColecticaConfiguration.ColecticaInstanceConfiguration instanceConfiguration;

    @BeforeEach
    void setUp() {
        colecticaConfiguration = Mockito.mock(ColecticaConfiguration.class);
        instanceConfiguration = Mockito.mock(ColecticaConfiguration.ColecticaInstanceConfiguration.class);

        when(colecticaConfiguration.server()).thenReturn(instanceConfiguration);
        when(instanceConfiguration.defaultAgencyId()).thenReturn("fr.insee");
        when(colecticaConfiguration.langs()).thenReturn(List.of("fr-FR", "en-GB"));

        domainInitService = new DomainInitService(
                new BauhausConfiguration("dev", "fr", "en", true, "http://localhost:3000",
                        List.of("concepts", "classifications"),
                        List.of("concepts", "classifications", "operations"),
                        "1.0.0"),
                "350",
                "DG75-L201",
                List.of("altLabel"),
                colecticaConfiguration
        );
    }

    @Test
    void should_return_init_properties_with_all_fields() {
        InitProperties properties = domainInitService.getInitProperties();

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
        domainInitService = new DomainInitService(
                new BauhausConfiguration("pre-prod", "fr", "en", true, "http://localhost:3000",
                        List.of(), List.of(), "1.0.0"),
                "350",
                "DG75-L201",
                List.of(),
                colecticaConfiguration
        );

        InitProperties properties = domainInitService.getInitProperties();

        assertThat(properties.authType()).isEqualTo("OpenIDConnectAuth");
    }

    @Test
    void should_return_openid_connect_auth_for_prod() {
        domainInitService = new DomainInitService(
                new BauhausConfiguration("prod", "fr", "en", true, "http://localhost:3000",
                        List.of(), List.of(), "1.0.0"),
                "350",
                "DG75-L201",
                List.of(),
                colecticaConfiguration
        );

        InitProperties properties = domainInitService.getInitProperties();

        assertThat(properties.authType()).isEqualTo("OpenIDConnectAuth");
    }

    @Test
    void should_return_openid_connect_auth_for_PROD() {
        domainInitService = new DomainInitService(
                new BauhausConfiguration("PROD", "fr", "en", true, "http://localhost:3000",
                        List.of(), List.of(), "1.0.0"),
                "350",
                "DG75-L201",
                List.of(),
                colecticaConfiguration
        );

        InitProperties properties = domainInitService.getInitProperties();

        assertThat(properties.authType()).isEqualTo("OpenIDConnectAuth");
    }

    @Test
    void should_return_no_auth_impl_for_dev_environment() {
        InitProperties properties = domainInitService.getInitProperties();

        assertThat(properties.authType()).isEqualTo("NoAuthImpl");
    }
}
