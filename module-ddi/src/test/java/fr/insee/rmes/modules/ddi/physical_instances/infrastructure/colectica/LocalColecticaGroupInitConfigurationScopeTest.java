package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.GroupService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.StudyUnitService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class LocalColecticaGroupInitConfigurationScopeTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(LocalColecticaGroupInitConfiguration.class)
            .withBean(GroupService.class, () -> mock(GroupService.class))
            .withBean(StudyUnitService.class, () -> mock(StudyUnitService.class))
            .withBean(DDIService.class, () -> mock(DDIService.class))
            .withBean(RepositoryPublicationReader.class, () -> mock(RepositoryPublicationReader.class))
            .withBean(ColecticaClient.class, () -> mock(ColecticaClient.class))
            .withBean(DDI4toDDI3ConverterService.class, () -> mock(DDI4toDDI3ConverterService.class))
            .withBean(ColecticaConfiguration.class, () -> mock(ColecticaConfiguration.class))
            .withBean(
                    MutualizedCodesProperties.class,
                    () -> new MutualizedCodesProperties(
                            MutualizedCodesProperties.Strategy.CONFIGURED_GROUPS,
                            List.of(new MutualizedCodesProperties.GroupRef("fr.insee", "group-id"))))
            .withPropertyValues(
                    "fr.insee.rmes.bauhaus.colectica.init=true",
                    "fr.insee.rmes.bauhaus.baseGraph=http://rdf.insee.fr/graphes/",
                    "fr.insee.rmes.bauhaus.operations.graph=operations");

    @Test
    void initRegistersEveryExampleByDefault() {
        contextRunner.run(context -> assertThat(context.getBeansOfType(CommandLineRunner.class))
                .hasSizeGreaterThan(1)
                .containsKey("initColecticaLargeMutualizedCodeListExample"));
    }

    @Test
    void initLimitedToTheLargeCodeListRegistersOnlyTheLargeMutualizedCodeListExample() {
        contextRunner
                .withPropertyValues("fr.insee.rmes.bauhaus.colectica.init-large-code-list-only=true")
                .run(context -> assertThat(context.getBeansOfType(CommandLineRunner.class))
                        .containsOnlyKeys("initColecticaLargeMutualizedCodeListExample"));
    }
}
