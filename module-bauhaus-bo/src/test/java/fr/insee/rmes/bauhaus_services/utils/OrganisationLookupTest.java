package fr.insee.rmes.bauhaus_services.utils;

import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.organisations.domain.port.serverside.OrganisationsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganisationLookupTest {

    @Mock
    private OrganizationsService organizationsService;

    @Mock
    private OrganisationsRepository organisationsRepository;

    @Test
    void resolve_returnsIriUnchanged_whenInputIsAlreadyAnIri() throws RmesException {
        OrganisationLookup lookup = new OrganisationLookup(organizationsService, organisationsRepository);

        Optional<String> result = lookup.resolve("http://bauhaus/organisations/DG75-A001");

        assertThat(result).contains("http://bauhaus/organisations/DG75-A001");
        verifyNoInteractions(organizationsService);
        verifyNoInteractions(organisationsRepository);
    }

    @Test
    void resolve_returnsIriResolvedFromGraph_whenInputIsAKnownLegacyLiteral() throws RmesException {
        when(organizationsService.getOrganizationUriById("DG75-A001"))
                .thenReturn("http://bauhaus/organisations/DG75-A001");
        OrganisationLookup lookup = new OrganisationLookup(organizationsService, organisationsRepository);

        Optional<String> result = lookup.resolve("DG75-A001");

        assertThat(result).contains("http://bauhaus/organisations/DG75-A001");
    }

    @Test
    void resolve_returnsEmpty_whenInputIsAnUnknownLiteral() throws RmesException {
        when(organizationsService.getOrganizationUriById("UNKNOWN-STAMP"))
                .thenReturn(null);
        OrganisationLookup lookup = new OrganisationLookup(organizationsService, organisationsRepository);

        Optional<String> result = lookup.resolve("UNKNOWN-STAMP");

        assertThat(result).isEmpty();
    }

    @Test
    void resolve_returnsEmpty_whenInputIsNull() throws RmesException {
        OrganisationLookup lookup = new OrganisationLookup(organizationsService, organisationsRepository);

        Optional<String> result = lookup.resolve(null);

        assertThat(result).isEmpty();
        verifyNoInteractions(organizationsService);
        verifyNoInteractions(organisationsRepository);
    }

    @Test
    void findUnknown_returnsEmpty_whenAllValuesResolve() throws Throwable {
        when(organisationsRepository.checkIfOrganisationExists("http://bauhaus/organisations/DG75-A001"))
                .thenReturn(true);
        when(organizationsService.getOrganizationUriById("DG75-B002"))
                .thenReturn("http://bauhaus/organisations/DG75-B002");
        OrganisationLookup lookup = new OrganisationLookup(organizationsService, organisationsRepository);

        List<String> unknown = lookup.findUnknown(List.of(
                "http://bauhaus/organisations/DG75-A001",
                "DG75-B002"));

        assertThat(unknown).isEmpty();
    }

    @Test
    void findUnknown_returnsValuesThatDoNotResolve() throws Throwable {
        when(organisationsRepository.checkIfOrganisationExists("http://bauhaus/organisations/DG75-A001"))
                .thenReturn(true);
        when(organisationsRepository.checkIfOrganisationExists("http://bauhaus/organisations/MISSING"))
                .thenReturn(false);
        when(organizationsService.getOrganizationUriById("LEGACY-MISSING"))
                .thenReturn(null);
        OrganisationLookup lookup = new OrganisationLookup(organizationsService, organisationsRepository);

        List<String> unknown = lookup.findUnknown(List.of(
                "http://bauhaus/organisations/DG75-A001",
                "http://bauhaus/organisations/MISSING",
                "LEGACY-MISSING"));

        assertThat(unknown).containsExactlyInAnyOrder(
                "http://bauhaus/organisations/MISSING",
                "LEGACY-MISSING");
    }

    @Test
    void canonicalize_replacesLegacyStampsWithIris_andDropsUnresolvableValues() throws RmesException {
        when(organizationsService.getOrganizationUriById("DG75-A001"))
                .thenReturn("http://bauhaus/organisations/DG75-A001");
        when(organizationsService.getOrganizationUriById("UNKNOWN"))
                .thenReturn(null);
        OrganisationLookup lookup = new OrganisationLookup(organizationsService, organisationsRepository);

        org.json.JSONArray rows = new org.json.JSONArray();
        rows.put("http://bauhaus/organisations/DG75-B002");
        rows.put("DG75-A001");
        rows.put("UNKNOWN");

        org.json.JSONArray result = lookup.canonicalize(rows);

        assertThat(result.length()).isEqualTo(2);
        assertThat(result.getString(0)).isEqualTo("http://bauhaus/organisations/DG75-B002");
        assertThat(result.getString(1)).isEqualTo("http://bauhaus/organisations/DG75-A001");
    }

    @Test
    void canonicalize_returnsEmptyArray_whenInputIsNull() throws RmesException {
        OrganisationLookup lookup = new OrganisationLookup(organizationsService, organisationsRepository);

        org.json.JSONArray result = lookup.canonicalize(null);

        assertThat(result.length()).isZero();
    }
}
