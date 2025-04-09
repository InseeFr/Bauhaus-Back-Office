package fr.insee.rmes.rbac;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.config.additional-location=classpath:rbac.yml")
@EnableConfigurationProperties(RBACConfiguration.class)
class PropertiesRbacFetcherTest {
    @Autowired
    RbacFetcher rbacFetcher;

    @Test
    void shouldReturnSomething(){
        assertThat(
                rbacFetcher.getApplicationActionStrategyByRole("Gestionnaire_indicateur_RMESGNCS", RBAC.Module.SIMS, RBAC.Privilege.CREATE )
        ).isEqualTo(RBAC.Strategy.STAMP);
    }

}