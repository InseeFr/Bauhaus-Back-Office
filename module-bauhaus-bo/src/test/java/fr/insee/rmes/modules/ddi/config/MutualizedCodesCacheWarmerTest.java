package fr.insee.rmes.modules.ddi.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodesList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceSearchRow;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskExecutor;

class MutualizedCodesCacheWarmerTest {

    /** Runs the submitted task synchronously, so the test can assert on its effects directly. */
    private static final TaskExecutor SYNCHRONOUS = Runnable::run;

    @Test
    void warmUpOnStartup_loadsTheMutualizedCodesListsOnce() {
        DDIRepository ddiRepository = mock(DDIRepository.class);
        when(ddiRepository.getMutualizedCodesLists()).thenReturn(List.<PartialCodesList>of());
        MutualizedCodesCacheWarmer warmer = new MutualizedCodesCacheWarmer(ddiRepository, SYNCHRONOUS);

        warmer.warmUpOnStartup();

        verify(ddiRepository, times(1)).getMutualizedCodesLists();
    }

    @Test
    void warmUpOnStartup_loadsThePhysicalInstanceSearchRowsOnce() {
        DDIRepository ddiRepository = mock(DDIRepository.class);
        when(ddiRepository.getPhysicalInstanceSearchRows()).thenReturn(List.<PhysicalInstanceSearchRow>of());
        MutualizedCodesCacheWarmer warmer = new MutualizedCodesCacheWarmer(ddiRepository, SYNCHRONOUS);

        warmer.warmUpOnStartup();

        verify(ddiRepository, times(1)).getPhysicalInstanceSearchRows();
    }

    @Test
    void warmUpOnStartup_doesNotPropagateFailures() {
        DDIRepository ddiRepository = mock(DDIRepository.class);
        when(ddiRepository.getMutualizedCodesLists()).thenThrow(new RuntimeException("Colectica down"));
        when(ddiRepository.getPhysicalInstanceSearchRows()).thenThrow(new RuntimeException("Colectica down"));
        MutualizedCodesCacheWarmer warmer = new MutualizedCodesCacheWarmer(ddiRepository, SYNCHRONOUS);

        assertThatCode(warmer::warmUpOnStartup).doesNotThrowAnyException();
    }
}
