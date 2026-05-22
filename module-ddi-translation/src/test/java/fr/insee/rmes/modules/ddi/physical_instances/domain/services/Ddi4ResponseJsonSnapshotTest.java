package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Snapshot doré du JSON produit pour le front à partir d'un {@link Ddi4Response} complet
 * (étape 4.1 du plan de migration vers les classes DDI générées).
 * <p>
 * Couvre tous les types DDI feuilles migrés (PhysicalInstance, DataRelationship, Variable,
 * CodeList, Category, LogicalRecord, Code, Citation) ainsi que les libellés {@code LangString}
 * et toutes les formes de référence ({@code DataRelationshipReference}, {@code CodeListReference},
 * {@code CategoryReference}, {@code VariableUsedReference}).
 * <p>
 * Le {@link Ddi4Response} est construit par le chemin de production (conversion DDI3 → DDI4),
 * puis sérialisé par Jackson. Les deux arbres sont <b>normalisés</b> (suppression récursive des
 * {@code null}, chaînes vides, tableaux et objets vides) avant comparaison par égalité d'arbres
 * {@link JsonNode} (indépendante de l'ordre des clés). Ainsi le golden survit au passage
 * records vers POJOs générés et à l'ajout de {@code @JsonInclude(NON_EMPTY)}, qui supprime les
 * champs vides aujourd'hui émis par les records (ex. {@code BasedOnObject: null},
 * {@code "@isGeographic": ""}). Seules la <b>présence</b> et la <b>valeur</b> des champs porteurs
 * d'information comptent (libellés, références, scalaires) : le contrat réel du front.
 * <p>
 * Si le golden n'existe pas encore, le test écrit la sortie normalisée courante dans
 * {@code target/ddi-snapshot/ddi4Response.json} et échoue avec la marche à suivre.
 */
class Ddi4ResponseJsonSnapshotTest {

    private static final String SCHEMA_URL = "http://localhost:8080/ddi/schema";
    private static final String GOLDEN_RESOURCE = "snapshot/ddi4Response.golden.json";

    private final ObjectMapper mapper = new ObjectMapper();

    private final DDI3toDDI4ConverterServiceImpl converter = new DDI3toDDI4ConverterServiceImpl(Map.of(
            "PhysicalInstance", "a51e85bb-6259-4488-8df2-f08cb43485f8",
            "DataRelationship", "f39ff278-8500-45fe-a850-3906da2d242b",
            "Variable", "683889c6-f74b-4d5e-92ed-908c0a42bb2d",
            "CodeList", "8b108ef8-b642-4484-9c49-f88e4bf7cf1d",
            "Category", "7e47c269-bcab-40f7-a778-af7bbc4e3d00"
    ));

    @Test
    void serializedDdi4Response_matchesGoldenSnapshot() throws Exception {
        Ddi4Response response = converter.convertDdi3ToDdi4(completeDdi3Response(), SCHEMA_URL);
        JsonNode actualTree = normalize(mapper.valueToTree(response));

        JsonNode goldenTree = readGoldenOrCapture(actualTree);

        assertEquals(goldenTree, actualTree,
                "Le JSON produit pour le front a changé par rapport au snapshot doré. "
                        + "Comparer target/ddi-snapshot/ddi4Response.json au golden "
                        + "src/test/resources/" + GOLDEN_RESOURCE + ".");
    }

    private JsonNode readGoldenOrCapture(JsonNode actualTree) throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(GOLDEN_RESOURCE)) {
            if (in != null) {
                return normalize(mapper.readTree(in));
            }
        }
        Path out = Path.of("target/ddi-snapshot/ddi4Response.json");
        Files.createDirectories(out.getParent());
        Files.writeString(out, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(actualTree),
                StandardCharsets.UTF_8);
        fail("Snapshot golden absent. Sortie normalisée capturée dans " + out.toAbsolutePath()
                + " — la copier vers src/test/resources/" + GOLDEN_RESOURCE + " puis relancer.");
        return null; // inatteignable
    }

    /**
     * Supprime récursivement les nœuds sans information : {@code null}, chaînes vides, tableaux
     * et objets vides. Rend la comparaison robuste au passage {@code @JsonInclude(NON_EMPTY)}.
     */
    private static JsonNode normalize(JsonNode node) {
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> it = obj.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                JsonNode cleaned = normalize(e.getValue());
                if (isEmpty(cleaned)) {
                    it.remove();
                } else {
                    e.setValue(cleaned);
                }
            }
            return obj;
        }
        if (node.isArray()) {
            ArrayNode arr = (ArrayNode) node;
            for (int i = arr.size() - 1; i >= 0; i--) {
                JsonNode cleaned = normalize(arr.get(i));
                if (isEmpty(cleaned)) {
                    arr.remove(i);
                } else {
                    arr.set(i, cleaned);
                }
            }
            return arr;
        }
        return node;
    }

    private static boolean isEmpty(JsonNode node) {
        return node.isNull()
                || (node.isTextual() && node.asText().isEmpty())
                || (node.isContainerNode() && node.isEmpty());
    }

    private Ddi3Response completeDdi3Response() {
        return new Ddi3Response(
                new Ddi3Response.Ddi3Options(List.of("RegisterOrReplace")),
                List.of(
                        item("a51e85bb-6259-4488-8df2-f08cb43485f8", "test-pi", PHYSICAL_INSTANCE_XML),
                        item("f39ff278-8500-45fe-a850-3906da2d242b", "test-dr", DATA_RELATIONSHIP_XML),
                        item("683889c6-f74b-4d5e-92ed-908c0a42bb2d", "AGEMEN8", VARIABLE_XML),
                        item("8b108ef8-b642-4484-9c49-f88e4bf7cf1d", "CL_AGEMEN8", CODE_LIST_XML),
                        item("7e47c269-bcab-40f7-a778-af7bbc4e3d00", "CAT_0", CATEGORY_XML)
                )
        );
    }

    private static Ddi3Response.Ddi3Item item(String typeGuid, String id, String xml) {
        return new Ddi3Response.Ddi3Item(
                typeGuid, "fr.insee", "1", id, xml,
                "2025-01-21T13:48:46.363", "abcde", false, false, false,
                "DC337820-AF3A-4C0B-82F9-CF02535CDE83");
    }

    private static final String PHYSICAL_INSTANCE_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <PhysicalInstance xmlns="ddi:physicalinstance:3_3" isUniversallyUnique="true" versionDate="2025-01-21T13:48:46.363">
                    <r:URN>urn:ddi:fr.insee:PhysicalInstance.saphir-rp99-sas:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>saphir-rp99-sas</r:ID>
                    <r:Version>1</r:Version>
                    <r:Citation>
                        <r:Title>
                            <r:String xml:lang="fr-FR">SAPHIR - Fichier Individus RP99 (.sas7bdat)</r:String>
                        </r:Title>
                    </r:Citation>
                    <DataRelationshipReference>
                        <r:Agency>fr.insee</r:Agency>
                        <r:ID>saphir-rp99-sas</r:ID>
                        <r:Version>1</r:Version>
                        <r:TypeOfObject>DataRelationship</r:TypeOfObject>
                    </DataRelationshipReference>
                </PhysicalInstance>
            </Fragment>
            """;

    private static final String DATA_RELATIONSHIP_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <DataRelationship xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-01-21T13:48:46.363">
                    <r:URN>urn:ddi:fr.insee:DataRelationship.saphir-rp99-sas:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>saphir-rp99-sas</r:ID>
                    <r:Version>1</r:Version>
                    <DataRelationshipName>
                        <r:String xml:lang="fr-FR">SAPHIR - RP99</r:String>
                    </DataRelationshipName>
                    <LogicalRecord isUniversallyUnique="true">
                        <r:URN>urn:ddi:fr.insee:LogicalRecord.saphir-rp99-sas:1</r:URN>
                        <r:Agency>fr.insee</r:Agency>
                        <r:ID>saphir-rp99-sas</r:ID>
                        <r:Version>1</r:Version>
                        <LogicalRecordName>
                            <r:String xml:lang="fr-FR">SAPHIR - RP99</r:String>
                        </LogicalRecordName>
                        <VariablesInRecord>
                            <VariableUsedReference>
                                <r:Agency>fr.insee</r:Agency>
                                <r:ID>AGEMEN8</r:ID>
                                <r:Version>1</r:Version>
                                <r:TypeOfObject>Variable</r:TypeOfObject>
                            </VariableUsedReference>
                        </VariablesInRecord>
                    </LogicalRecord>
                </DataRelationship>
            </Fragment>
            """;

    private static final String VARIABLE_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <Variable xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-01-21T13:48:46.363">
                    <r:URN>urn:ddi:fr.insee:Variable.AGEMEN8:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>AGEMEN8</r:ID>
                    <r:Version>1</r:Version>
                    <VariableName>
                        <r:String xml:lang="fr-FR">AGEMEN8</r:String>
                    </VariableName>
                    <r:Label>
                        <r:Content xml:lang="fr-FR">Âge détaillé</r:Content>
                    </r:Label>
                    <r:Description>
                        <r:Content xml:lang="fr-FR">Âge de l'individu en années révolues</r:Content>
                    </r:Description>
                    <VariableRepresentation>
                        <VariableRole>Demographic</VariableRole>
                        <r:CodeRepresentation blankIsMissingValue="false">
                            <r:CodeListReference>
                                <r:Agency>fr.insee</r:Agency>
                                <r:ID>CL_AGEMEN8</r:ID>
                                <r:Version>1</r:Version>
                                <r:TypeOfObject>CodeList</r:TypeOfObject>
                            </r:CodeListReference>
                        </r:CodeRepresentation>
                    </VariableRepresentation>
                </Variable>
            </Fragment>
            """;

    private static final String CODE_LIST_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <CodeList xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-01-21T13:48:46.363">
                    <r:URN>urn:ddi:fr.insee:CodeList.CL_AGEMEN8:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>CL_AGEMEN8</r:ID>
                    <r:Version>1</r:Version>
                    <r:Label>
                        <r:Content xml:lang="fr-FR">Liste de codes - Âge détaillé</r:Content>
                    </r:Label>
                    <Code isUniversallyUnique="true">
                        <r:URN>urn:ddi:fr.insee:Code.CL_AGEMEN8.0:1</r:URN>
                        <r:Agency>fr.insee</r:Agency>
                        <r:ID>0</r:ID>
                        <r:Version>1</r:Version>
                        <r:CategoryReference>
                            <r:Agency>fr.insee</r:Agency>
                            <r:ID>CAT_0</r:ID>
                            <r:Version>1</r:Version>
                            <r:TypeOfObject>Category</r:TypeOfObject>
                        </r:CategoryReference>
                        <r:Value>0</r:Value>
                    </Code>
                </CodeList>
            </Fragment>
            """;

    private static final String CATEGORY_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
                <Category xmlns="ddi:logicalproduct:3_3" isUniversallyUnique="true" versionDate="2025-01-21T13:48:46.363" isMissing="false">
                    <r:URN>urn:ddi:fr.insee:Category.CAT_0:1</r:URN>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>CAT_0</r:ID>
                    <r:Version>1</r:Version>
                    <r:Label>
                        <r:Content xml:lang="fr-FR">0 an</r:Content>
                    </r:Label>
                </Category>
            </Fragment>
            """;
}
