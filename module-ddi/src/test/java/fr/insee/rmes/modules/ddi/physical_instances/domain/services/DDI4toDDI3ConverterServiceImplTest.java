package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeListReference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.DataRelationshipReference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4DataRelationship;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangStrings;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LogicalRecord;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.TopLevelReference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.VariableRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.VariablesInRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DDI4toDDI3ConverterServiceImplTest {

    private DDI4toDDI3ConverterServiceImpl converter;

    private static final Map<String, String> ITEM_TYPES = Map.of(
        "PhysicalInstance", "a51e85bb-6259-4488-8df2-f08cb43485f8",
        "DataRelationship", "f39ff278-8500-45fe-a850-3906da2d242b",
        "Variable", "683889c6-f74b-4d5e-92ed-908c0a42bb2d",
        "CodeList", "8b108ef8-b642-4484-9c49-f88e4bf7cf1d",
        "Category", "7e47c269-bcab-40f7-a778-af7bbc4e3d00"
    );

    @BeforeEach
    void setUp() {
        converter = new DDI4toDDI3ConverterServiceImpl(ITEM_TYPES);
    }

    @Test
    void shouldConvertPhysicalInstance() {
        Ddi4PhysicalInstance pi = new Ddi4PhysicalInstance(
                "true", "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:PhysicalInstance.saphir-rp99-sas:1",
                "fr.insee", "saphir-rp99-sas", "1",
                null,
                new Citation(LangStrings.of("fr-FR", "SAPHIR")),
                new DataRelationshipReference("fr.insee", "saphir-rp99-sas", "1", "DataRelationship")
        );
        Ddi4Response ddi4 = new Ddi4Response("file:/jsonSchema.json", null, List.of(pi), null, null, null, null);

        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        assertThat(result.items()).hasSize(1);
        Ddi3Response.Ddi3Item item = result.items().get(0);
        assertThat(item.itemType()).isEqualTo("a51e85bb-6259-4488-8df2-f08cb43485f8");
        assertThat(item.agencyId()).isEqualTo("fr.insee");
        assertThat(item.identifier()).isEqualTo("saphir-rp99-sas");
        assertThat(item.item())
                .contains("<ddi:PhysicalInstance")
                .contains(">SAPHIR<");
    }

    @Test
    void shouldConvertDataRelationship() {
        Ddi4DataRelationship dr = new Ddi4DataRelationship(
                "true", "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:DataRelationship.saphir-rp99-sas:1",
                "fr.insee", "saphir-rp99-sas", "1",
                null,
                LangStrings.of("fr-FR", "SAPHIR - RP99"),
                new LogicalRecord("true", "urn:ddi:fr.insee:lr:1", "fr.insee", "saphir-rp99-sas", "1",
                        LangStrings.of("fr-FR", "SAPHIR - RP99"),
                        new VariablesInRecord(List.of()))
        );
        Ddi4Response ddi4 = new Ddi4Response("file:/jsonSchema.json", null, null, List.of(dr), null, null, null);

        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        assertThat(result.items()).hasSize(1);
        Ddi3Response.Ddi3Item item = result.items().get(0);
        assertThat(item.itemType()).isEqualTo("f39ff278-8500-45fe-a850-3906da2d242b");
        assertThat(item.item())
                .contains("<ddi:DataRelationship")
                .contains(">SAPHIR - RP99<");
    }

    @Test
    void shouldConvertVariable() {
        Ddi4Variable var = new Ddi4Variable(
                "true", "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:Variable.AGEMEN8:1",
                "fr.insee", "AGEMEN8", "1",
                null,
                LangStrings.of("fr-FR", "AGEMEN8"),
                LangStrings.of("fr-FR", "Âge détaillé"),
                null,
                new VariableRepresentation(null,
                        new CodeRepresentation("false", new CodeListReference("fr.insee", "CL_AGEMEN8", "1", "CodeList")),
                        null, null, null),
                null
        );
        Ddi4Response ddi4 = new Ddi4Response("file:/jsonSchema.json", null, null, null, List.of(var), null, null);

        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        assertThat(result.items()).hasSize(1);
        Ddi3Response.Ddi3Item item = result.items().get(0);
        assertThat(item.itemType()).isEqualTo("683889c6-f74b-4d5e-92ed-908c0a42bb2d");
        assertThat(item.item())
                .contains("<ddi:Variable")
                .contains(">AGEMEN8<")
                .contains(">Âge détaillé<")
                .contains(">CL_AGEMEN8<");
    }

    @Test
    void shouldConvertCodeList() {
        Ddi4CodeList cl = new Ddi4CodeList(
                "true", "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:CodeList.CL_AGEMEN8:1",
                "fr.insee", "CL_AGEMEN8", "1",
                LangStrings.of("fr-FR", "Liste codes"),
                List.of()
        );
        Ddi4Response ddi4 = new Ddi4Response("file:/jsonSchema.json", null, null, null, null, List.of(cl), null);

        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        assertThat(result.items()).hasSize(1);
        Ddi3Response.Ddi3Item item = result.items().get(0);
        assertThat(item.itemType()).isEqualTo("8b108ef8-b642-4484-9c49-f88e4bf7cf1d");
        assertThat(item.item())
                .contains("<ddi:CodeList")
                .contains(">Liste codes<");
    }

    @Test
    void shouldConvertCategory() {
        Ddi4Category cat = new Ddi4Category(
                "true", "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:Category.CAT_0:1",
                "fr.insee", "CAT_0", "1",
                LangStrings.of("fr-FR", "0 an")
        );
        Ddi4Response ddi4 = new Ddi4Response("file:/jsonSchema.json", null, null, null, null, null, List.of(cat));

        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);

        assertThat(result.items()).hasSize(1);
        Ddi3Response.Ddi3Item item = result.items().get(0);
        assertThat(item.itemType()).isEqualTo("7e47c269-bcab-40f7-a778-af7bbc4e3d00");
        assertThat(item.item())
                .contains("<ddi:Category")
                .contains(">0 an<");
    }

    @Test
    void shouldHandleEmptyDdi4Response() {
        Ddi4Response ddi4 = new Ddi4Response("file:/jsonSchema.json", null, null, null, null, null, null);
        Ddi3Response result = converter.convertDdi4ToDdi3(ddi4);
        assertThat(result).isNotNull();
        assertThat(result.items()).isEmpty();
        assertThat(result.options().namedOptions()).containsExactly("RegisterOrReplace");
    }

    @Test
    void shouldBuildFragmentInstanceDocumentWithTopLevelReference() {
        Ddi4PhysicalInstance pi = new Ddi4PhysicalInstance(
                "true", "2025-01-21T13:48:46.363",
                "urn:ddi:fr.insee:PhysicalInstance.test:1",
                "fr.insee", "test-id", "1",
                null,
                new Citation(LangStrings.of("fr-FR", "Test")),
                new DataRelationshipReference("fr.insee", "test", "1", "DataRelationship")
        );
        TopLevelReference topLevelRef = new TopLevelReference("fr.insee", "test-id", "1", "PhysicalInstance");
        Ddi4Response ddi4 = new Ddi4Response("file:/jsonSchema.json", List.of(topLevelRef), List.of(pi), null, null, null, null);

        String result = converter.convertDdi4ToDdi3Xml(ddi4);

        assertThat(result)
                .startsWith("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                .contains("<ddi:FragmentInstance")
                .contains("<ddi:TopLevelReference")
                .contains(">test-id<")
                .contains(">PhysicalInstance<");
    }
}
