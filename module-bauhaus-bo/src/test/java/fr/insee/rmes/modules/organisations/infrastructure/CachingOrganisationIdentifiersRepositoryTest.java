package fr.insee.rmes.modules.organisations.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.modules.organisations.domain.exceptions.OrganisationFetchException;
import fr.insee.rmes.modules.organisations.domain.port.serverside.OrganisationsRepository;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CachingOrganisationIdentifiersRepositoryTest {

    private final OrganisationsRepository delegate = mock(OrganisationsRepository.class);
    private final CachingOrganisationIdentifiersRepository repository =
            new CachingOrganisationIdentifiersRepository(delegate, Duration.ofMinutes(1));

    @Test
    void should_read_the_adms_identifier_of_a_stamp_once() throws OrganisationFetchException {
        when(delegate.getAdmsIdentifier("DG75-A001")).thenReturn(Optional.of("HIE2000331"));

        repository.getAdmsIdentifier("DG75-A001");

        assertThat(repository.getAdmsIdentifier("DG75-A001")).contains("HIE2000331");
        verify(delegate, times(1)).getAdmsIdentifier("DG75-A001");
    }

    @Test
    void should_read_the_dcterms_identifier_of_a_hie_once() throws OrganisationFetchException {
        when(delegate.getDctermsIdentifier("HIE2000331")).thenReturn(Optional.of("DG75-A001"));

        repository.getDctermsIdentifier("HIE2000331");

        assertThat(repository.getDctermsIdentifier("HIE2000331")).contains("DG75-A001");
        verify(delegate, times(1)).getDctermsIdentifier("HIE2000331");
    }

    @Test
    void should_remember_that_a_stamp_has_no_alternate_identifier() throws OrganisationFetchException {
        when(delegate.getAdmsIdentifier("DG75-Z999")).thenReturn(Optional.empty());

        repository.getAdmsIdentifier("DG75-Z999");

        assertThat(repository.getAdmsIdentifier("DG75-Z999")).isEmpty();
        verify(delegate, times(1)).getAdmsIdentifier("DG75-Z999");
    }

    @Test
    void should_not_cache_a_failed_lookup() throws OrganisationFetchException {
        when(delegate.getAdmsIdentifier("DG75-A001"))
                .thenThrow(new OrganisationFetchException())
                .thenReturn(Optional.of("HIE2000331"));

        assertThatThrownBy(() -> repository.getAdmsIdentifier("DG75-A001"))
                .isInstanceOf(OrganisationFetchException.class);

        assertThat(repository.getAdmsIdentifier("DG75-A001")).contains("HIE2000331");
    }

    @Test
    void should_delegate_the_other_reads() throws OrganisationFetchException {
        repository.getCompactOrganisations(List.of("HIE1"));
        repository.checkIfOrganisationExists("http://bauhaus/organisations/insee/HIE1");

        verify(delegate).getCompactOrganisations(List.of("HIE1"));
        verify(delegate).checkIfOrganisationExists("http://bauhaus/organisations/insee/HIE1");
    }
}
