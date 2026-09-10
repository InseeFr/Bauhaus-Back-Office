package fr.insee.rmes.modules.ddi.physical_instances;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangStrings;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica.ColecticaConfiguration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PhysicalInstanceConfigurationTest {

    private static final String PHYSICAL_INSTANCE_UUID = "a51e85bb-6259-4488-8df2-f08cb43485f8";

    @Test
    void shouldStampConfiguredVersionResponsibilityOnConvertedItems() {
        ColecticaConfiguration.ColecticaInstanceConfiguration server =
                mock(ColecticaConfiguration.ColecticaInstanceConfiguration.class);
        when(server.itemTypes()).thenReturn(Map.of("PhysicalInstance", PHYSICAL_INSTANCE_UUID));
        when(server.versionResponsibility()).thenReturn("responsable-configure");
        ColecticaConfiguration colecticaConfiguration = mock(ColecticaConfiguration.class);
        when(colecticaConfiguration.server()).thenReturn(server);

        DDI4toDDI3ConverterService converter =
                new PhysicalInstanceConfiguration().ddi4toDdi3ConverterService(colecticaConfiguration);

        Ddi3Response ddi3 = converter.convertDdi4ToDdi3(physicalInstanceOnly());

        assertThat(ddi3.items())
                .singleElement()
                .extracting(Ddi3Response.Ddi3Item::versionResponsibility)
                .isEqualTo("responsable-configure");
    }

    private static Ddi4Response physicalInstanceOnly() {
        Ddi4PhysicalInstance physicalInstance = new Ddi4PhysicalInstance(
                Ddi4PhysicalInstance.TYPE,
                CogsDate.ofDateTime("2026-01-21T13:48:46.363"),
                "urn:ddi:fr.insee:PhysicalInstance.saphir-rp99-sas:1",
                "fr.insee",
                "saphir-rp99-sas",
                "1",
                null,
                new Citation(LangStrings.of("fr-FR", "SAPHIR")),
                null);
        return new Ddi4Response("file:/jsonSchema.json", null, List.of(physicalInstance), null, null, null, null, null);
    }
}
