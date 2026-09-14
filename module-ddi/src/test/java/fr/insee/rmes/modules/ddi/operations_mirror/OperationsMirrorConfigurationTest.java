package fr.insee.rmes.modules.ddi.operations_mirror;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.modules.ddi.operations_mirror.domain.port.clientside.OperationsMirrorService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.GroupService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.StudyUnitService;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaConfiguration;
import fr.insee.rmes.modules.operation.domain.event.BilingualLabel;
import fr.insee.rmes.modules.operation.domain.event.OperationSaved;
import fr.insee.rmes.modules.operation.domain.event.SeriesSaved;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class OperationsMirrorConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(OperationsMirrorConfiguration.class)
            .withBean(GroupService.class, () -> mock(GroupService.class))
            .withBean(StudyUnitService.class, () -> mock(StudyUnitService.class))
            .withBean(DDIService.class, () -> mock(DDIService.class))
            .withBean(ColecticaConfiguration.class, OperationsMirrorConfigurationTest::colecticaConfiguration);

    @Test
    void mirrorsNothingWhenTheFeatureIsNotEnabled() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(OperationsMirrorService.class));
    }

    @Test
    void mirrorsNothingWhenTheFeatureIsExplicitlyDisabled() {
        contextRunner
                .withPropertyValues("fr.insee.rmes.bauhaus.colectica.operations-mirror.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(OperationsMirrorService.class));
    }

    @Test
    void registersTheMirrorWhenTheFeatureIsEnabled() {
        contextRunner
                .withPropertyValues("fr.insee.rmes.bauhaus.colectica.operations-mirror.enabled=true")
                .run(context -> assertThat(context).hasSingleBean(OperationsMirrorService.class));
    }

    @Test
    void forwardsEachSavedSeriesAndOperationToTheMirror() {
        OperationsMirrorService mirror = mock(OperationsMirrorService.class);
        SeriesSaved series = new SeriesSaved(
                "http://id.insee.fr/operations/serie/s1001",
                "s1001",
                new BilingualLabel("Recensement", null),
                new BilingualLabel("RP", null));
        OperationSaved operation = new OperationSaved(
                "http://id.insee.fr/operations/operation/o1500",
                "o1500",
                "http://id.insee.fr/operations/serie/s1001",
                new BilingualLabel("Enquête emploi", null),
                new BilingualLabel("EEC", null));

        OperationsMirrorEventListener listener = new OperationsMirrorEventListener(mirror);
        listener.on(series);
        listener.on(operation);

        verify(mirror).mirror(series);
        verify(mirror).mirror(operation);
    }

    @Test
    void letsAFailingMirrorFailTheSaveThatTriggeredIt() {
        OperationsMirrorService mirror = mock(OperationsMirrorService.class);
        SeriesSaved series = new SeriesSaved(
                "http://id.insee.fr/operations/serie/s1001", "s1001", new BilingualLabel("RP", null), null);
        RuntimeException colecticaIsDown = new IllegalStateException("Colectica unreachable");
        doThrow(colecticaIsDown).when(mirror).mirror(series);

        OperationsMirrorEventListener listener = new OperationsMirrorEventListener(mirror);

        assertThatThrownBy(() -> listener.on(series)).isSameAs(colecticaIsDown);
        verify(mirror, never()).mirror(any(OperationSaved.class));
    }

    private static ColecticaConfiguration colecticaConfiguration() {
        ColecticaConfiguration.ColecticaInstanceConfiguration server =
                mock(ColecticaConfiguration.ColecticaInstanceConfiguration.class);
        when(server.defaultAgencyId()).thenReturn("fr.insee");
        ColecticaConfiguration configuration = mock(ColecticaConfiguration.class);
        when(configuration.server()).thenReturn(server);
        when(configuration.langs()).thenReturn(List.of("fr-FR", "en-GB"));
        return configuration;
    }
}
