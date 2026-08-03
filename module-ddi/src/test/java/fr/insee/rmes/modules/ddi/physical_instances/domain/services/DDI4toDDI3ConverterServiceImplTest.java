package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CategoryScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4DataRelationship;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4LogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Code;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedRepresentationScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.ValueType;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4VariableScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangStrings;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LogicalRecord;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.VariableRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.VariablesInRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DDI4toDDI3ConverterServiceImplTest {

    private DDI4toDDI3ConverterServiceImpl converter;

    private static final Map<String, String> ITEM_TYPES = Map.ofEntries(
        Map.entry("PhysicalInstance", "a51e85bb-6259-4488-8df2-f08cb43485f8"),
        Map.entry("DataRelationship", "f39ff278-8500-45fe-a850-3906da2d242b"),
        Map.entry("Variable", "683889c6-f74b-4d5e-92ed-908c0a42bb2d"),
        Map.entry("CodeList", "8b108ef8-b642-4484-9c49-f88e4bf7cf1d"),
        Map.entry("CodeListScheme", "c5084949-3e3a-4b7f-9f5b-1a2b3c4d5e6f"),
        Map.entry("CategoryScheme", "1c11de94-a36d-4d80-95dc-950c6f37f624"),
        Map.entry("VariableScheme", "50907716-b67a-4dcd-8f9f-8a283cb5fee0"),
        Map.entry("LogicalProduct", "965c8d28-7d48-4950-bea7-04b27e52bb9b"),
        Map.entry("Category", "7e47c269-bcab-40f7-a778-af7bbc4e3d00"),
        Map.entry("ManagedRepresentationScheme", "16d4d829-41e1-4677-aa17-81190b6a0e66"),
        Map.entry("ManagedMissingValuesRepresentation", "c29c3125-2a53-4179-8fa6-aa3beb2bb5ed")
    );

    @BeforeEach
    void setUp() {
        converter = new DDI4toDDI3ConverterServiceImpl(ITEM_TYPES);
    }

    @Test
    void shouldConvertPhysicalInstance() {
        Ddi4PhysicalInstance pi = new Ddi4PhysicalInstance(Ddi4PhysicalInstance.TYPE,
                CogsDate.ofDateTime("2025-01-21T13:48:46.363"),
                "urn:ddi:fr.insee:PhysicalInstance.saphir-rp99-sas:1",
                "fr.insee", "saphir-rp99-sas", "1",
                null,
                new Citation(LangStrings.of("fr-FR", "SAPHIR")),
                List.of(Reference.of("fr.insee", "saphir-rp99-sas", "1", "DataRelationship"))
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
        Ddi4DataRelationship dr = new Ddi4DataRelationship(Ddi4DataRelationship.TYPE,
                CogsDate.ofDateTime("2025-01-21T13:48:46.363"),
                "urn:ddi:fr.insee:DataRelationship.saphir-rp99-sas:1",
                "fr.insee", "saphir-rp99-sas", "1",
                null,
                LangStrings.of("fr-FR", "SAPHIR - RP99"),
                List.of(new LogicalRecord(LogicalRecord.TYPE,"urn:ddi:fr.insee:lr:1", "fr.insee", "saphir-rp99-sas", "1",
                        LangStrings.of("fr-FR", "SAPHIR - RP99"),
                        new VariablesInRecord(List.of())))
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
        Ddi4Variable var = new Ddi4Variable(Ddi4Variable.TYPE,
                CogsDate.ofDateTime("2025-01-21T13:48:46.363"),
                "urn:ddi:fr.insee:Variable.AGEMEN8:1",
                "fr.insee", "AGEMEN8", "1",
                null,
                LangStrings.of("fr-FR", "AGEMEN8"),
                LangStrings.of("fr-FR", "Âge détaillé"),
                null,
                new VariableRepresentation(null,
                        new CodeRepresentation(CodeRepresentation.TYPE,false, Reference.of("fr.insee", "CL_AGEMEN8", "1", "CodeList")),
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
        Ddi4CodeList cl = new Ddi4CodeList(Ddi4CodeList.TYPE,
                CogsDate.ofDateTime("2025-01-21T13:48:46.363"),
                "urn:ddi:fr.insee:CodeList.CL_AGEMEN8:1",
                "fr.insee", "CL_AGEMEN8", "1",
                LangStrings.of("fr-FR", "Liste codes"),
                null,
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
        Ddi4Category cat = new Ddi4Category(Ddi4Category.TYPE,
                CogsDate.ofDateTime("2025-01-21T13:48:46.363"),
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
    void shouldConvertCodeListSchemeToDdi3Item() {
        Ddi4CodeListScheme scheme = new Ddi4CodeListScheme(Ddi4CodeListScheme.TYPE,
                CogsDate.ofDateTime("2025-01-21T13:48:46.363"),
                "urn:ddi:fr.insee:CLS_1:1", "fr.insee", "CLS_1", "1",
                LangStrings.of("fr-FR", "Schéma listes"),
                List.of(Reference.of("fr.insee", "CL_AGEMEN8", "1", "CodeList")));

        Ddi3Response.Ddi3Item item = converter.toCodeListSchemeItem(scheme);

        assertThat(item.itemType()).isEqualTo("c5084949-3e3a-4b7f-9f5b-1a2b3c4d5e6f");
        assertThat(item.agencyId()).isEqualTo("fr.insee");
        assertThat(item.identifier()).isEqualTo("CLS_1");
        assertThat(item.version()).isEqualTo("1");
        assertThat(item.item())
                .contains("<ddi:CodeListScheme")
                .contains(">Schéma listes<")
                .contains(">CL_AGEMEN8<");
    }

    @Test
    void shouldConvertCategorySchemeToDdi3Item() {
        Ddi4CategoryScheme scheme = new Ddi4CategoryScheme(Ddi4CategoryScheme.TYPE,
                CogsDate.ofDateTime("2025-01-21T13:48:46.363"),
                "urn:ddi:fr.insee:CATS_1:1", "fr.insee", "CATS_1", "1",
                LangStrings.of("fr-FR", "Schéma catégories"),
                List.of(Reference.of("fr.insee", "CAT_1", "1", "Category")));

        Ddi3Response.Ddi3Item item = converter.toCategorySchemeItem(scheme);

        assertThat(item.itemType()).isEqualTo("1c11de94-a36d-4d80-95dc-950c6f37f624");
        assertThat(item.agencyId()).isEqualTo("fr.insee");
        assertThat(item.identifier()).isEqualTo("CATS_1");
        assertThat(item.version()).isEqualTo("1");
        assertThat(item.item())
                .contains("<ddi:CategoryScheme")
                .contains(">Schéma catégories<")
                .contains(">CAT_1<");
    }

    @Test
    void shouldConvertManagedRepresentationSchemeToDdi3Item() {
        Ddi4ManagedRepresentationScheme scheme = new Ddi4ManagedRepresentationScheme(Ddi4ManagedRepresentationScheme.TYPE,
                CogsDate.ofDateTime("2025-01-21T13:48:46.363"),
                "urn:ddi:fr.insee:MRS_1:1", "fr.insee", "MRS_1", "1",
                LangStrings.of("fr-FR", "Schéma représentations gérées"),
                List.of());

        Ddi3Response.Ddi3Item item = converter.toManagedRepresentationSchemeItem(scheme);

        assertThat(item.itemType()).isEqualTo("16d4d829-41e1-4677-aa17-81190b6a0e66");
        assertThat(item.agencyId()).isEqualTo("fr.insee");
        assertThat(item.identifier()).isEqualTo("MRS_1");
        assertThat(item.version()).isEqualTo("1");
        assertThat(item.item())
                .contains("<r:ManagedRepresentationScheme")
                .contains(">Schéma représentations gérées<");
    }

    @Test
    void shouldConvertManagedMissingValuesRepresentationToDdi3Item() {
        Ddi4ManagedMissingValuesRepresentation mmvr = new Ddi4ManagedMissingValuesRepresentation(
                Ddi4ManagedMissingValuesRepresentation.TYPE,
                CogsDate.ofDateTime("2025-01-21T13:48:46.363"),
                "urn:ddi:fr.insee:MMVR_1:1", "fr.insee", "MMVR_1", "1",
                LangStrings.of("fr-FR", "Valeurs manquantes standard"),
                List.of(new CodeRepresentation(CodeRepresentation.TYPE, Boolean.FALSE,
                        Reference.of("fr.insee", "CL_SENTINEL", "1", "CodeList"))));

        Ddi3Response.Ddi3Item item = converter.toManagedMissingValuesRepresentationItem(mmvr);

        assertThat(item.itemType()).isEqualTo("c29c3125-2a53-4179-8fa6-aa3beb2bb5ed");
        assertThat(item.agencyId()).isEqualTo("fr.insee");
        assertThat(item.identifier()).isEqualTo("MMVR_1");
        assertThat(item.version()).isEqualTo("1");
        assertThat(item.item())
                .contains("<r:ManagedMissingValuesRepresentation")
                .contains(">Valeurs manquantes standard<")
                .contains(">CL_SENTINEL<");
    }

    @Test
    void shouldConvertCodeListToDdi3Item() {
        Ddi4CodeList codeList = new Ddi4CodeList(Ddi4CodeList.TYPE,
                CogsDate.ofDateTime("2025-01-21T13:48:46.363"),
                "urn:ddi:fr.insee:CL_1:1", "fr.insee", "CL_1", "1",
                LangStrings.of("fr-FR", "Liste sentinelle"),
                null,
                List.of(new Code(Code.TYPE, "urn:ddi:fr.insee:CODE_1:1", "fr.insee", "CODE_1", "1",
                        Reference.of("fr.insee", "CAT_1", "1", "Category"),
                        ValueType.of("NSP"), null)));

        Ddi3Response.Ddi3Item item = converter.toCodeListItem(codeList);

        assertThat(item.itemType()).isEqualTo("8b108ef8-b642-4484-9c49-f88e4bf7cf1d");
        assertThat(item.agencyId()).isEqualTo("fr.insee");
        assertThat(item.identifier()).isEqualTo("CL_1");
        assertThat(item.version()).isEqualTo("1");
        assertThat(item.item())
                .contains("CodeList")
                .contains(">Liste sentinelle<")
                .contains(">NSP<")
                .contains(">CAT_1<");
    }

    @Test
    void shouldConvertCategoryToDdi3Item() {
        Ddi4Category category = new Ddi4Category(Ddi4Category.TYPE,
                CogsDate.ofDateTime("2025-01-21T13:48:46.363"),
                "urn:ddi:fr.insee:CAT_1:1", "fr.insee", "CAT_1", "1",
                LangStrings.of("fr-FR", "Ne sait pas"));

        Ddi3Response.Ddi3Item item = converter.toCategoryItem(category);

        assertThat(item.itemType()).isEqualTo("7e47c269-bcab-40f7-a778-af7bbc4e3d00");
        assertThat(item.agencyId()).isEqualTo("fr.insee");
        assertThat(item.identifier()).isEqualTo("CAT_1");
        assertThat(item.version()).isEqualTo("1");
        assertThat(item.item())
                .contains("Category")
                .contains(">Ne sait pas<");
    }

    @Test
    void shouldConvertVariableSchemeToDdi3Item() {
        Ddi4VariableScheme scheme = new Ddi4VariableScheme(Ddi4VariableScheme.TYPE,
                CogsDate.ofDateTime("2025-01-21T13:48:46.363"),
                "urn:ddi:fr.insee:VARS_1:1", "fr.insee", "VARS_1", "1",
                LangStrings.of("fr-FR", "Schéma variables"),
                List.of(Reference.of("fr.insee", "VAR_1", "1", "Variable")));

        Ddi3Response.Ddi3Item item = converter.toVariableSchemeItem(scheme);

        assertThat(item.itemType()).isEqualTo("50907716-b67a-4dcd-8f9f-8a283cb5fee0");
        assertThat(item.agencyId()).isEqualTo("fr.insee");
        assertThat(item.identifier()).isEqualTo("VARS_1");
        assertThat(item.version()).isEqualTo("1");
        assertThat(item.item())
                .contains("<ddi:VariableScheme")
                .contains(">Schéma variables<")
                .contains(">VAR_1<");
    }

    @Test
    void shouldConvertLogicalProductToDdi3Item() {
        Ddi4LogicalProduct logicalProduct = new Ddi4LogicalProduct(Ddi4LogicalProduct.TYPE,
                CogsDate.ofDateTime("2025-01-21T13:48:46.363"),
                "urn:ddi:fr.insee:LP_1:1", "fr.insee", "LP_1", "1",
                LangStrings.of("fr-FR", "Produit logique"),
                List.of(Reference.of("fr.insee", "CLS_1", "1", "CodeListScheme")));

        Ddi3Response.Ddi3Item item = converter.toLogicalProductItem(logicalProduct);

        assertThat(item.itemType()).isEqualTo("965c8d28-7d48-4950-bea7-04b27e52bb9b");
        assertThat(item.agencyId()).isEqualTo("fr.insee");
        assertThat(item.identifier()).isEqualTo("LP_1");
        assertThat(item.version()).isEqualTo("1");
        assertThat(item.item())
                .contains("<ddi:LogicalProduct")
                .contains(">Produit logique<")
                .contains(">CLS_1<");
    }

    @Test
    void shouldConvertGroupToDdi3ItemWithLogicalProductReference() {
        Ddi4Group group = new Ddi4Group(Ddi4Group.TYPE,
                CogsDate.ofDateTime("2025-01-21T13:48:46.363"),
                "urn:ddi:fr.insee:GROUP_1:1", "fr.insee", "GROUP_1", "1", "resp",
                new Citation(LangStrings.of("fr-FR", "Groupe")),
                List.of(Reference.of("fr.insee", "SU_1", "1", "StudyUnit")),
                List.of("http://id.insee.fr/operations/serie/s1001"),
                "insee:StatisticalOperationSeries",
                List.of(Reference.of("fr.insee", "LP_1", "1", "LogicalProduct")));

        Ddi3Response.Ddi3Item item = converter.toGroupItem(group, "group-type-uuid");

        assertThat(item.itemType()).isEqualTo("group-type-uuid");
        assertThat(item.agencyId()).isEqualTo("fr.insee");
        assertThat(item.identifier()).isEqualTo("GROUP_1");
        assertThat(item.version()).isEqualTo("1");
        assertThat(item.item())
                .contains("<ddi:Group")
                .contains(">Groupe<")
                .contains("<r:LogicalProductReference")
                .contains(">LP_1<")
                .contains(">SU_1<");
    }

    @Test
    void shouldConvertStudyUnitToDdi3ItemWithLogicalProductReference() {
        Ddi4StudyUnit studyUnit = new Ddi4StudyUnit(Ddi4StudyUnit.TYPE,
                CogsDate.ofDateTime("2025-01-21T13:48:46.363"),
                "urn:ddi:fr.insee:SU_1:1", "fr.insee", "SU_1", "1",
                new Citation(LangStrings.of("fr-FR", "Study Unit")),
                "http://id.insee.fr/operations/operation/op1",
                List.of(Reference.of("fr.insee", "PI_1", "1", "PhysicalInstance")),
                List.of(Reference.of("fr.insee", "LP_1", "1", "LogicalProduct")));

        Ddi3Response.Ddi3Item item = converter.toStudyUnitItem(studyUnit, "study-unit-type-uuid");

        assertThat(item.itemType()).isEqualTo("study-unit-type-uuid");
        assertThat(item.agencyId()).isEqualTo("fr.insee");
        assertThat(item.identifier()).isEqualTo("SU_1");
        assertThat(item.item())
                .contains("<ddi:StudyUnit")
                .contains("<r:LogicalProductReference")
                .contains(">LP_1<")
                .contains("<r:PhysicalInstanceReference")
                .contains(">PI_1<");
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
        Ddi4PhysicalInstance pi = new Ddi4PhysicalInstance(Ddi4PhysicalInstance.TYPE,
                CogsDate.ofDateTime("2025-01-21T13:48:46.363"),
                "urn:ddi:fr.insee:PhysicalInstance.test:1",
                "fr.insee", "test-id", "1",
                null,
                new Citation(LangStrings.of("fr-FR", "Test")),
                List.of(Reference.of("fr.insee", "test", "1", "DataRelationship"))
        );
        Reference topLevelRef = Reference.of("fr.insee", "test-id", "1", "PhysicalInstance");
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
