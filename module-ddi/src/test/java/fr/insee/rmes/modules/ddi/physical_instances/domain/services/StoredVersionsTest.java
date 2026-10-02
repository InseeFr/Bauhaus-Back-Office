package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.BasedOnObject;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4DataRelationship;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangString;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LogicalRecord;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.VariableRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.VariablesInRecord;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Le front réémet chaque item et chaque référence en {@code Version: "1"}. Colectica sert la
 * dernière version d'un item : écrire une v1 sur un item stocké en v2 crée une version fantôme
 * que personne ne relit, et la modification est perdue.
 */
class StoredVersionsTest {

    private static final String AGENCY = "fr.insee";

    @Test
    void writesAStoredItemAtItsStoredVersion() {
        Ddi4Response aligned = StoredVersions.align(stored(), incoming());

        Ddi4Variable variable = variable(aligned, "var-1");
        assertEquals("2", variable.version());
        assertEquals("urn:ddi:fr.insee:var-1:2", variable.urn());
    }

    @Test
    void pointsReferencesToTheStoredVersionOfTheirTarget() {
        Ddi4Response aligned = StoredVersions.align(stored(), incoming());

        Reference variableReference = aligned.dataRelationship()
                .getFirst()
                .logicalRecord()
                .getFirst()
                .variablesInRecord()
                .variableUsedReference()
                .getFirst();
        assertEquals("2", variableReference.version());
        assertEquals("urn:ddi:fr.insee:var-1:2", variableReference.urn());
        assertEquals("3", codeListReference(variable(aligned, "var-1")).version());
    }

    @Test
    void keepsTheIncomingVersionOfItemsAndReferencesUnknownToTheStore() {
        Ddi4Response aligned = StoredVersions.align(stored(), incoming());

        Ddi4Variable newVariable = variable(aligned, "var-new");
        assertEquals("1", newVariable.version());
        assertEquals("1", codeListReference(newVariable).version());
    }

    /** Le lignage désigne la version dont l'item est issu, pas la dernière : il n'est pas réaligné. */
    @Test
    void leavesLineageReferencesUntouched() {
        Ddi4Response aligned = StoredVersions.align(stored(), incoming());

        assertEquals(
                "1",
                variable(aligned, "var-new")
                        .basedOnObject()
                        .basedOnReferences()
                        .getFirst()
                        .version());
    }

    /**
     * Le PUT ne relit que les items du payload : la version stockée d'un item seulement référencé (une
     * liste mutualisée choisie pour une variable) arrive à part, sans son contenu.
     */
    @Test
    void pointsReferencesToItemsOutsideThePayloadToTheirSuppliedStoredVersion() {
        Ddi4Response aligned = StoredVersions.align(
                stored(), List.of(Reference.of(AGENCY, "cl-mutualisee", "5", Ddi4CodeList.TYPE)), incoming());

        Reference codeListReference = codeListReference(variable(aligned, "var-new"));
        assertEquals("5", codeListReference.version());
        assertEquals("urn:ddi:fr.insee:cl-mutualisee:5", codeListReference.urn());
    }

    @Test
    void listsEveryReferencedTargetButLineage() {
        Ddi4Response payload = response(
                dataRelationship("1"),
                List.of(variable(
                        "var-new",
                        "1",
                        "cl-1",
                        "1",
                        BasedOnObject.of(List.of(Reference.of(AGENCY, "var-source", "1", Ddi4Variable.TYPE))))),
                List.of());

        assertEquals(
                Set.of("var-1", "cl-1"),
                StoredVersions.references(payload).stream().map(Reference::id).collect(Collectors.toSet()));
    }

    // --- fixtures : DR -> {var-1 -> cl-1, var-new -> cl-mutualisee} ---

    /** État stocké : var-1 en v2, sa liste de codes en v3. */
    private static Ddi4Response stored() {
        return response(
                dataRelationship("2"),
                List.of(variable("var-1", "2", "cl-1", "3", null)),
                List.of(codeList("cl-1", "3")));
    }

    /** Ce qu'envoie le front : tout en v1, plus une variable nouvelle dérivée de var-1. */
    private static Ddi4Response incoming() {
        return response(
                dataRelationship("1"),
                List.of(
                        variable("var-1", "1", "cl-1", "1", null),
                        variable(
                                "var-new",
                                "1",
                                "cl-mutualisee",
                                "1",
                                BasedOnObject.of(List.of(Reference.of(AGENCY, "var-1", "1", Ddi4Variable.TYPE))))),
                List.of(codeList("cl-1", "1")));
    }

    private static Ddi4Response response(
            Ddi4DataRelationship dataRelationship, List<Ddi4Variable> variables, List<Ddi4CodeList> codeLists) {
        return new Ddi4Response(
                Ddi4Response.SCHEMA, null, null, List.of(dataRelationship), variables, codeLists, null, null);
    }

    private static Ddi4DataRelationship dataRelationship(String variableVersion) {
        return new Ddi4DataRelationship(
                Ddi4DataRelationship.TYPE,
                null,
                Reference.synthesizeUrn(AGENCY, "dr-1", "1"),
                AGENCY,
                "dr-1",
                "1",
                null,
                List.of(new LangString("fr", "Mon DR")),
                List.of(new LogicalRecord(
                        LogicalRecord.TYPE,
                        Reference.synthesizeUrn(AGENCY, "lr-1", "1"),
                        AGENCY,
                        "lr-1",
                        "1",
                        List.of(new LangString("fr", "Mon LR")),
                        new VariablesInRecord(
                                List.of(Reference.of(AGENCY, "var-1", variableVersion, Ddi4Variable.TYPE))))));
    }

    private static Ddi4Variable variable(
            String id, String version, String codeListId, String codeListVersion, BasedOnObject basedOnObject) {
        return new Ddi4Variable(
                Ddi4Variable.TYPE,
                null,
                Reference.synthesizeUrn(AGENCY, id, version),
                AGENCY,
                id,
                version,
                basedOnObject,
                List.of(new LangString("fr", id)),
                null,
                null,
                new VariableRepresentation(
                        null,
                        new CodeRepresentation(
                                CodeRepresentation.TYPE,
                                null,
                                Reference.of(AGENCY, codeListId, codeListVersion, Ddi4CodeList.TYPE)),
                        null,
                        null,
                        null,
                        null),
                null);
    }

    private static Ddi4CodeList codeList(String id, String version) {
        return new Ddi4CodeList(
                Ddi4CodeList.TYPE,
                null,
                Reference.synthesizeUrn(AGENCY, id, version),
                AGENCY,
                id,
                version,
                List.of(new LangString("fr", id)),
                null,
                List.of());
    }

    private static Ddi4Variable variable(Ddi4Response response, String id) {
        return response.variable().stream()
                .filter(v -> id.equals(v.id()))
                .findFirst()
                .orElseThrow();
    }

    private static Reference codeListReference(Ddi4Variable variable) {
        return variable.variableRepresentation().codeRepresentation().codeListReference();
    }
}
