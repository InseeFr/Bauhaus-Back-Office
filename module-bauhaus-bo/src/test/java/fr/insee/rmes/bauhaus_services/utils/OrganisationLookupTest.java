package fr.insee.rmes.bauhaus_services.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import fr.insee.rmes.bauhaus_services.OrganizationsService;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.organisations.domain.model.OrganisationOption;
import fr.insee.rmes.modules.organisations.domain.port.clientside.OrganisationService;
import fr.insee.rmes.modules.organisations.domain.port.serverside.OrganisationsRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganisationLookupTest {

    @Mock
    private OrganizationsService organizationsService;

    @Mock
    private OrganisationsRepository organisationsRepository;

    @Mock
    private OrganisationService organisationService;

    @Test
    void resolve_returnsIriUnchanged_whenInputIsAlreadyAnIri() throws RmesException {
        OrganisationLookup lookup =
                new OrganisationLookup(organizationsService, organisationsRepository, organisationService);

        Optional<String> result = lookup.resolve("http://bauhaus/organisations/DG75-A001");

        assertThat(result).contains("http://bauhaus/organisations/DG75-A001");
        verifyNoInteractions(organizationsService);
        verifyNoInteractions(organisationsRepository);
    }

    @Test
    void resolve_returnsIriResolvedFromGraph_whenInputIsAKnownLegacyLiteral() throws RmesException {
        when(organizationsService.getOrganizationUriById("DG75-A001"))
                .thenReturn("http://bauhaus/organisations/DG75-A001");
        OrganisationLookup lookup =
                new OrganisationLookup(organizationsService, organisationsRepository, organisationService);

        Optional<String> result = lookup.resolve("DG75-A001");

        assertThat(result).contains("http://bauhaus/organisations/DG75-A001");
    }

    @Test
    void resolve_returnsEmpty_whenInputIsAnUnknownLiteral() throws RmesException {
        when(organizationsService.getOrganizationUriById("UNKNOWN-STAMP")).thenReturn(null);
        OrganisationLookup lookup =
                new OrganisationLookup(organizationsService, organisationsRepository, organisationService);

        Optional<String> result = lookup.resolve("UNKNOWN-STAMP");

        assertThat(result).isEmpty();
    }

    @Test
    void resolve_returnsEmpty_whenInputIsNull() throws RmesException {
        OrganisationLookup lookup =
                new OrganisationLookup(organizationsService, organisationsRepository, organisationService);

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
        OrganisationLookup lookup =
                new OrganisationLookup(organizationsService, organisationsRepository, organisationService);

        List<String> unknown = lookup.findUnknown(List.of("http://bauhaus/organisations/DG75-A001", "DG75-B002"));

        assertThat(unknown).isEmpty();
    }

    @Test
    void findUnknown_returnsValuesThatDoNotResolve() throws Throwable {
        when(organisationsRepository.checkIfOrganisationExists("http://bauhaus/organisations/DG75-A001"))
                .thenReturn(true);
        when(organisationsRepository.checkIfOrganisationExists("http://bauhaus/organisations/MISSING"))
                .thenReturn(false);
        when(organizationsService.getOrganizationUriById("LEGACY-MISSING")).thenReturn(null);
        OrganisationLookup lookup =
                new OrganisationLookup(organizationsService, organisationsRepository, organisationService);

        List<String> unknown = lookup.findUnknown(List.of(
                "http://bauhaus/organisations/DG75-A001", "http://bauhaus/organisations/MISSING", "LEGACY-MISSING"));

        assertThat(unknown).containsExactlyInAnyOrder("http://bauhaus/organisations/MISSING", "LEGACY-MISSING");
    }

    @Test
    void canonicalize_returnsShortFormForAllResolvedValues_andDropsUnresolvableValues() throws RmesException {
        when(organisationService.getOrganisationsMap(anyList()))
                .thenReturn(Map.of(
                        "http://bauhaus/organisations/DG75-B002", new OrganisationOption("DG75-B002", "label B"),
                        "DG75-A001", new OrganisationOption("DG75-A001", "label A")));
        OrganisationLookup lookup =
                new OrganisationLookup(organizationsService, organisationsRepository, organisationService);

        JSONArray rows = new JSONArray();
        rows.put("http://bauhaus/organisations/DG75-B002");
        rows.put("DG75-A001");
        rows.put("UNKNOWN");

        JSONArray result = lookup.canonicalize(rows);

        assertThat(result.length()).isEqualTo(2);
        assertThat(result.getString(0)).isEqualTo("DG75-B002");
        assertThat(result.getString(1)).isEqualTo("DG75-A001");
    }

    @Test
    void canonicalize_returnsEmptyArray_whenInputIsNull() throws RmesException {
        OrganisationLookup lookup =
                new OrganisationLookup(organizationsService, organisationsRepository, organisationService);

        JSONArray result = lookup.canonicalize(null);

        assertThat(result.length()).isZero();
    }

    @Test
    void canonicalize_returns_short_form_for_organisation_iri() throws RmesException {
        assertCanonicalizedTo("http://bauhaus/organisations/insee/HIE2000069", "HIE2000069");
    }

    @Test
    void canonicalize_returns_admsIdentifier_from_organisationService_evenWhenItDiffersFromIriSegment()
            throws RmesException {
        // The canonical stamp comes from adms:identifier in the RDF graph, not from text-extraction
        // of the IRI's last segment. This test pins that contract by using an IRI whose segment
        // ("internal-id-42") differs from the real stamp ("PUBLIC-STAMP").
        assertCanonicalizedTo("http://example.org/organisations/internal-id-42", "PUBLIC-STAMP");
    }

    /** L'organisation d'IRI {@code iri} porte l'identifiant {@code stamp} : c'est lui qui ressort. */
    private void assertCanonicalizedTo(String iri, String stamp) throws RmesException {
        when(organisationService.getOrganisationsMap(anyList()))
                .thenReturn(Map.of(iri, new OrganisationOption(stamp, "label")));
        OrganisationLookup lookup =
                new OrganisationLookup(organizationsService, organisationsRepository, organisationService);

        JSONArray rows = new JSONArray();
        rows.put(iri);

        JSONArray result = lookup.canonicalize(rows);

        assertThat(result.length()).isEqualTo(1);
        assertThat(result.getString(0)).isEqualTo(stamp);
    }
}
