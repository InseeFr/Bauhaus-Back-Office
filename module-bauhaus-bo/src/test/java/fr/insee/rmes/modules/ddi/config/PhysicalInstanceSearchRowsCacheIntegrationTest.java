package fr.insee.rmes.modules.ddi.config;

import static fr.insee.rmes.colectica.client.dto.ColecticaItemBuilder.aColecticaItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.ItemReference;
import fr.insee.rmes.colectica.client.RelationshipDirection;
import fr.insee.rmes.colectica.client.dto.ColecticaAdvancedItem;
import fr.insee.rmes.colectica.client.dto.ColecticaAdvancedResponse;
import fr.insee.rmes.colectica.client.dto.ColecticaResponse;
import fr.insee.rmes.colectica.client.dto.LocalizedText;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangStrings;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceSearchRow;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI3toDDI4ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaCacheNames;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaConfiguration;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.DDIRepositoryImpl;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.MutualizedCodeListRefsProvider;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Vérifie, à travers le proxy Spring et le vrai cache Caffeine, qu'une sauvegarde de PhysicalInstance
 * met à jour sa ligne de recherche en place au lieu de vider toute la région
 * {@link ColecticaCacheNames#PHYSICAL_INSTANCE_SEARCH_ROWS} : la descente Group → StudyUnit → PI n'est
 * pas refaite.
 */
@SpringJUnitConfig
class PhysicalInstanceSearchRowsCacheIntegrationTest {

    private static final String AGENCY = "fr.insee";
    private static final String PI_TYPE = "a51e85bb-6259-4488-8df2-f08cb43485f8";
    private static final String GROUP_TYPE = "4bd6eef6-99df-40e6-9b11-5b8f64e5cb23";
    private static final String STUDY_UNIT_TYPE = "30ea0200-7121-4f01-8d21-a931a182b86d";

    @Configuration
    @Import(ColecticaCacheConfiguration.class)
    static class TestConfig {

        @Bean
        ColecticaClient colecticaClient() {
            return mock(ColecticaClient.class);
        }

        @Bean
        DDI4toDDI3ConverterService ddi4ToDdi3Converter() {
            return mock(DDI4toDDI3ConverterService.class);
        }

        @Bean
        ColecticaConfiguration colecticaConfiguration() {
            var server = new ColecticaConfiguration.ColecticaInstanceConfiguration(
                    "https://example.com",
                    "/api/v1/",
                    Map.of("PhysicalInstance", PI_TYPE, "StudyUnit", STUDY_UNIT_TYPE),
                    "resp",
                    "format",
                    "password",
                    "user",
                    "pass",
                    AGENCY);
            return new ColecticaConfiguration(
                    List.of("fr-FR"),
                    server,
                    new ColecticaConfiguration.PackageRef(AGENCY, "pkg-1", 1),
                    Duration.ofHours(1));
        }

        @Bean
        DDIRepository ddiRepository(
                ColecticaConfiguration config,
                ColecticaClient client,
                DDI4toDDI3ConverterService ddi4ToDdi3Converter,
                CacheManager cacheManager) {
            return new DDIRepositoryImpl(
                    config.server(),
                    mock(DDI3toDDI4ConverterService.class),
                    ddi4ToDdi3Converter,
                    config,
                    client,
                    new MutualizedCodeListRefsProvider(config.server(), config, client),
                    cacheManager.getCache(ColecticaCacheNames.PHYSICAL_INSTANCE_SEARCH_ROWS));
        }
    }

    @Autowired
    DDIRepository ddiRepository;

    @Autowired
    ColecticaClient client;

    @Autowired
    DDI4toDDI3ConverterService ddi4ToDdi3Converter;

    @Autowired
    CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        reset(client, ddi4ToDdi3Converter);
        cacheManager.getCache(ColecticaCacheNames.PHYSICAL_INSTANCE_SEARCH_ROWS).clear();
    }

    @Test
    void updateFullPhysicalInstance_refreshesItsSearchRowFromTheSavedInstanceWithoutCallingColectica() {
        when(client.queryAdvanced(anyList())).thenReturn(advancedResponse("Ancien libellé"));
        when(client.query(List.of(GROUP_TYPE)))
                .thenReturn(new ColecticaResponse(
                        List.of(aColecticaItem(GROUP_TYPE, "g1")
                                .itemName("Groupe BPE")
                                .build()),
                        1,
                        1,
                        null,
                        null,
                        null));
        when(client.findRelatedItems(
                        RelationshipDirection.BY_SUBJECT, new ItemReference(AGENCY, "g1"), List.of(STUDY_UNIT_TYPE)))
                .thenReturn(List.of(aColecticaItem(STUDY_UNIT_TYPE, "su-1")
                        .itemName("Recensement 2024")
                        .build()));
        when(client.findRelatedDescriptions(
                        RelationshipDirection.BY_SUBJECT, new ItemReference(AGENCY, "su-1"), List.of(PI_TYPE)))
                .thenReturn(List.of(new ItemReference(AGENCY, "pi-1")));
        // Sauvegarde minimale : la PI seule, sans StudyUnit résolue donc sans rangement sous un scheme.
        when(ddi4ToDdi3Converter.convertDdi4ToDdi3(any()))
                .thenReturn(new Ddi3Response(
                        new Ddi3Response.Ddi3Options(List.of("RegisterOrReplace")),
                        List.of(new Ddi3Response.Ddi3Item(
                                PI_TYPE,
                                AGENCY,
                                "2",
                                "pi-1",
                                "<pi/>",
                                "2026-10-02T10:00:00",
                                "resp",
                                false,
                                false,
                                false,
                                "fmt"))));

        assertThat(ddiRepository.getPhysicalInstanceSearchRows())
                .extracting(PhysicalInstanceSearchRow::label)
                .containsExactly("Ancien libellé");

        Ddi4PhysicalInstance saved = new Ddi4PhysicalInstance(
                Ddi4PhysicalInstance.TYPE,
                CogsDate.ofDateTime("2026-10-02T10:00:00Z"),
                "urn:ddi:fr.insee:pi-1:2",
                AGENCY,
                "pi-1",
                "2",
                null,
                new Citation(LangStrings.of("fr-FR", "Nouveau libellé")),
                List.of());
        ddiRepository.updateFullPhysicalInstance(
                AGENCY, "pi-1", new Ddi4Response("schema", null, List.of(saved), null, null, null, null, null));

        List<PhysicalInstanceSearchRow> rows = ddiRepository.getPhysicalInstanceSearchRows();
        assertThat(rows).extracting(PhysicalInstanceSearchRow::label).containsExactly("Nouveau libellé");
        assertThat(rows.getFirst().studyUnitLabel()).isEqualTo("Recensement 2024");
        assertThat(rows.getFirst().groupLabel()).isEqualTo("Groupe BPE");
        assertThat(rows.getFirst().versionDate()).isEqualTo(Date.from(Instant.parse("2026-10-02T10:00:00Z")));
        // Seule la construction initiale lit Colectica : la ligne est reconstituée depuis la PI sauvegardée.
        verify(client, times(1)).queryAdvanced(anyList());
        verify(client, times(1))
                .findRelatedItems(eq(RelationshipDirection.BY_SUBJECT), eq(new ItemReference(AGENCY, "g1")), anyList());
    }

    private static ColecticaAdvancedResponse advancedResponse(String label) {
        return new ColecticaAdvancedResponse(
                List.of(new ColecticaAdvancedItem(
                        AGENCY,
                        "pi-1",
                        1,
                        PI_TYPE,
                        false,
                        Map.of("label", List.of(new LocalizedText(label, "fr-FR"))),
                        Map.of(),
                        Map.of("isPublished", false))),
                1,
                null);
    }
}
