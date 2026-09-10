package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Code;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4DataRelationship;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4PhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangString;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LogicalRecord;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.ValueType;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.VariableRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.VariablesInRecord;
import java.util.List;
import org.junit.jupiter.api.Test;

class VersionDateReconcilerTest {

    private static final String AGENCY = "fr.insee";

    private static final CogsDate NOW = date("2026-08-03T10:00:00+02:00");
    private static final CogsDate FRONT_DATE = date("2026-01-01T00:00:00Z");

    private static final CogsDate PI_DATE = date("2020-01-01T00:00:00Z");
    private static final CogsDate DR_DATE = date("2020-02-01T00:00:00Z");
    private static final CogsDate VAR1_DATE = date("2020-03-01T00:00:00Z");
    private static final CogsDate VAR2_DATE = date("2020-04-01T00:00:00Z");
    private static final CogsDate CL1_DATE = date("2020-05-01T00:00:00Z");
    private static final CogsDate CL2_DATE = date("2020-06-01T00:00:00Z");
    private static final CogsDate CAT1_DATE = date("2020-07-01T00:00:00Z");
    private static final CogsDate CAT2_DATE = date("2020-08-01T00:00:00Z");

    @Test
    void keepsStoredDatesWhenNothingChanged() {
        Ddi4Response stored = storedResponse();
        // le front renvoie le même contenu mais avec des dates quelconques
        Ddi4Response incoming = responseWithSameDateEverywhere(FRONT_DATE);

        Ddi4Response reconciled = VersionDateReconciler.reconcile(stored, incoming, NOW);

        assertEquals(PI_DATE, reconciled.physicalInstance().getFirst().versionDate());
        assertEquals(DR_DATE, reconciled.dataRelationship().getFirst().versionDate());
        assertEquals(VAR1_DATE, variableDate(reconciled, "var-1"));
        assertEquals(VAR2_DATE, variableDate(reconciled, "var-2"));
        assertEquals(CL1_DATE, codeListDate(reconciled, "cl-1"));
        assertEquals(CL2_DATE, codeListDate(reconciled, "cl-2"));
        assertEquals(CAT1_DATE, categoryDate(reconciled, "cat-1"));
        assertEquals(CAT2_DATE, categoryDate(reconciled, "cat-2"));
    }

    @Test
    void refreshesCodeListAndItsAncestorsWhenACodeChanges() {
        Ddi4Response stored = storedResponse();
        // même contenu sauf la valeur d'un code de cl-1
        Ddi4Response incoming = response(
                FRONT_DATE,
                FRONT_DATE,
                FRONT_DATE,
                FRONT_DATE,
                FRONT_DATE,
                FRONT_DATE,
                FRONT_DATE,
                FRONT_DATE,
                "code-value-1-modifie",
                "Catégorie 1");

        Ddi4Response reconciled = VersionDateReconciler.reconcile(stored, incoming, NOW);

        assertEquals(NOW, codeListDate(reconciled, "cl-1"));
        assertEquals(NOW, variableDate(reconciled, "var-1"));
        assertEquals(NOW, reconciled.dataRelationship().getFirst().versionDate());
        assertEquals(NOW, reconciled.physicalInstance().getFirst().versionDate());
        assertEquals(VAR2_DATE, variableDate(reconciled, "var-2"));
        assertEquals(CL2_DATE, codeListDate(reconciled, "cl-2"));
        assertEquals(CAT1_DATE, categoryDate(reconciled, "cat-1"));
        assertEquals(CAT2_DATE, categoryDate(reconciled, "cat-2"));
    }

    @Test
    void refreshesWholeChainUpToPhysicalInstanceWhenACategoryChanges() {
        Ddi4Response stored = storedResponse();
        // même contenu sauf le label de cat-1
        Ddi4Response incoming = response(
                FRONT_DATE,
                FRONT_DATE,
                FRONT_DATE,
                FRONT_DATE,
                FRONT_DATE,
                FRONT_DATE,
                FRONT_DATE,
                FRONT_DATE,
                "code-value-1",
                "Catégorie 1 modifiée");

        Ddi4Response reconciled = VersionDateReconciler.reconcile(stored, incoming, NOW);

        assertEquals(NOW, categoryDate(reconciled, "cat-1"));
        assertEquals(NOW, codeListDate(reconciled, "cl-1"));
        assertEquals(NOW, variableDate(reconciled, "var-1"));
        assertEquals(NOW, reconciled.dataRelationship().getFirst().versionDate());
        assertEquals(NOW, reconciled.physicalInstance().getFirst().versionDate());
        assertEquals(CAT2_DATE, categoryDate(reconciled, "cat-2"));
        assertEquals(CL2_DATE, codeListDate(reconciled, "cl-2"));
        assertEquals(VAR2_DATE, variableDate(reconciled, "var-2"));
    }

    @Test
    void propagatesThroughReferencesNestedInHierarchicalCodes() {
        // cl-1 porte un code hiérarchique dont un code enfant référence cat-3 :
        // la propagation doit traverser cette structure imbriquée quelconque.
        CogsDate cat3Date = date("2020-09-01T00:00:00Z");
        Ddi4Response stored = hierarchicalResponse(PI_DATE, DR_DATE, VAR1_DATE, CL1_DATE, cat3Date, "Catégorie 3");
        Ddi4Response incoming = hierarchicalResponse(
                FRONT_DATE, FRONT_DATE, FRONT_DATE, FRONT_DATE, FRONT_DATE, "Catégorie 3 modifiée");

        Ddi4Response reconciled = VersionDateReconciler.reconcile(stored, incoming, NOW);

        assertEquals(NOW, categoryDate(reconciled, "cat-3"));
        assertEquals(NOW, codeListDate(reconciled, "cl-1"));
        assertEquals(NOW, variableDate(reconciled, "var-1"));
        assertEquals(NOW, reconciled.dataRelationship().getFirst().versionDate());
        assertEquals(NOW, reconciled.physicalInstance().getFirst().versionDate());
    }

    @Test
    void newItemGetsNow() {
        Ddi4Response stored = storedResponse();
        Ddi4Response sameContent = responseWithSameDateEverywhere(FRONT_DATE);
        // var-3 n'existe pas dans l'état stocké
        Ddi4Response incoming = new Ddi4Response(
                sameContent.schema(),
                sameContent.topLevelReference(),
                sameContent.physicalInstance(),
                sameContent.dataRelationship(),
                List.of(
                        sameContent.variable().get(0),
                        sameContent.variable().get(1),
                        variable("var-3", FRONT_DATE, "cl-2")),
                sameContent.codeList(),
                sameContent.category(),
                null);

        Ddi4Response reconciled = VersionDateReconciler.reconcile(stored, incoming, NOW);

        assertEquals(NOW, variableDate(reconciled, "var-3"));
        assertEquals(PI_DATE, reconciled.physicalInstance().getFirst().versionDate());
        assertEquals(DR_DATE, reconciled.dataRelationship().getFirst().versionDate());
        assertEquals(VAR1_DATE, variableDate(reconciled, "var-1"));
        assertEquals(CL2_DATE, codeListDate(reconciled, "cl-2"));
    }

    @Test
    void everythingGetsNowWhenCurrentIsNull() {
        Ddi4Response incoming = responseWithSameDateEverywhere(FRONT_DATE);

        Ddi4Response reconciled = VersionDateReconciler.reconcile(null, incoming, NOW);

        assertEquals(NOW, reconciled.physicalInstance().getFirst().versionDate());
        assertEquals(NOW, reconciled.dataRelationship().getFirst().versionDate());
        assertEquals(NOW, variableDate(reconciled, "var-1"));
        assertEquals(NOW, variableDate(reconciled, "var-2"));
        assertEquals(NOW, codeListDate(reconciled, "cl-1"));
        assertEquals(NOW, codeListDate(reconciled, "cl-2"));
        assertEquals(NOW, categoryDate(reconciled, "cat-1"));
        assertEquals(NOW, categoryDate(reconciled, "cat-2"));
    }

    /** Valeurs sentinelles (#1566) : une MMVR nouvelle (absente de l'état stocké) est datée à now. */
    @Test
    void newManagedMissingValuesRepresentationGetsNow() {
        Ddi4Response stored = mmvrOnlyResponse(null);
        Ddi4Response incoming = mmvrOnlyResponse(mmvr(FRONT_DATE));

        Ddi4Response reconciled = VersionDateReconciler.reconcile(stored, incoming, NOW);

        assertEquals(
                NOW, reconciled.managedMissingValuesRepresentation().getFirst().versionDate());
    }

    /** Valeurs sentinelles (#1566) : une MMVR réutilisée telle quelle garde sa date stockée. */
    @Test
    void unchangedManagedMissingValuesRepresentationKeepsStoredDate() {
        CogsDate mmvrDate = date("2020-10-01T00:00:00Z");
        Ddi4Response stored = mmvrOnlyResponse(mmvr(mmvrDate));
        Ddi4Response incoming = mmvrOnlyResponse(mmvr(FRONT_DATE));

        Ddi4Response reconciled = VersionDateReconciler.reconcile(stored, incoming, NOW);

        assertEquals(
                mmvrDate,
                reconciled.managedMissingValuesRepresentation().getFirst().versionDate());
    }

    private static Ddi4Response mmvrOnlyResponse(Ddi4ManagedMissingValuesRepresentation mmvr) {
        return new Ddi4Response(
                Ddi4Response.SCHEMA, null, null, null, null, null, null, mmvr == null ? null : List.of(mmvr));
    }

    private static Ddi4ManagedMissingValuesRepresentation mmvr(CogsDate date) {
        return new Ddi4ManagedMissingValuesRepresentation(
                Ddi4ManagedMissingValuesRepresentation.TYPE,
                date,
                "urn:ddi:fr.insee:mmvr-1:1",
                AGENCY,
                "mmvr-1",
                "1",
                List.of(new LangString("fr", "Valeurs sentinelles NSP/REF")),
                List.of(new CodeRepresentation(
                        CodeRepresentation.TYPE, false, ref("cl-sentinelles", Ddi4CodeList.TYPE))));
    }

    // --- fixtures : PI -> DR -> {var-1 -> cl-1 -> cat-1, var-2 -> cl-2 -> cat-2} ---

    /** État stocké (résultat du GET), avec une date distincte par item. */
    private static Ddi4Response storedResponse() {
        return response(
                PI_DATE,
                DR_DATE,
                VAR1_DATE,
                VAR2_DATE,
                CL1_DATE,
                CL2_DATE,
                CAT1_DATE,
                CAT2_DATE,
                "code-value-1",
                "Catégorie 1");
    }

    /** Même contenu que l'état stocké, mais toutes les dates à {@code date}. */
    private static Ddi4Response responseWithSameDateEverywhere(CogsDate date) {
        return response(date, date, date, date, date, date, date, date, "code-value-1", "Catégorie 1");
    }

    private static Ddi4Response response(
            CogsDate piDate,
            CogsDate drDate,
            CogsDate var1Date,
            CogsDate var2Date,
            CogsDate cl1Date,
            CogsDate cl2Date,
            CogsDate cat1Date,
            CogsDate cat2Date,
            String cl1CodeValue,
            String cat1Label) {
        return new Ddi4Response(
                Ddi4Response.SCHEMA,
                List.of(ref("pi-1", Ddi4PhysicalInstance.TYPE)),
                List.of(physicalInstance(piDate)),
                List.of(dataRelationship(drDate)),
                List.of(variable("var-1", var1Date, "cl-1"), variable("var-2", var2Date, "cl-2")),
                List.of(
                        codeList("cl-1", cl1Date, "cat-1", cl1CodeValue),
                        codeList("cl-2", cl2Date, "cat-2", "code-value-2")),
                List.of(category("cat-1", cat1Date, cat1Label), category("cat-2", cat2Date, "Catégorie 2")),
                null);
    }

    /**
     * Variante hiérarchique : PI -> DR -> var-1 -> cl-1, dont le code racine
     * porte un code enfant référençant cat-3.
     */
    private static Ddi4Response hierarchicalResponse(
            CogsDate piDate,
            CogsDate drDate,
            CogsDate var1Date,
            CogsDate cl1Date,
            CogsDate cat3Date,
            String cat3Label) {
        Code childCode = new Code(
                Code.TYPE,
                urn("cl-1-c1-1"),
                AGENCY,
                "cl-1-c1-1",
                "1",
                ref("cat-3", Ddi4Category.TYPE),
                ValueType.of("child-value"),
                null);
        Code rootCode = new Code(
                Code.TYPE,
                urn("cl-1-c1"),
                AGENCY,
                "cl-1-c1",
                "1",
                null,
                ValueType.of("root-value"),
                List.of(childCode));
        Ddi4CodeList hierarchicalCodeList = new Ddi4CodeList(
                Ddi4CodeList.TYPE,
                cl1Date,
                urn("cl-1"),
                AGENCY,
                "cl-1",
                "1",
                List.of(new LangString("fr", "cl-1")),
                null,
                List.of(rootCode));
        return new Ddi4Response(
                Ddi4Response.SCHEMA,
                List.of(ref("pi-1", Ddi4PhysicalInstance.TYPE)),
                List.of(physicalInstance(piDate)),
                List.of(dataRelationship(drDate)),
                List.of(variable("var-1", var1Date, "cl-1")),
                List.of(hierarchicalCodeList),
                List.of(category("cat-3", cat3Date, cat3Label)),
                null);
    }

    private static Ddi4PhysicalInstance physicalInstance(CogsDate date) {
        return new Ddi4PhysicalInstance(
                Ddi4PhysicalInstance.TYPE,
                date,
                urn("pi-1"),
                AGENCY,
                "pi-1",
                "1",
                null,
                new Citation(List.of(new LangString("fr", "Ma PI"))),
                List.of(ref("dr-1", Ddi4DataRelationship.TYPE)));
    }

    private static Ddi4DataRelationship dataRelationship(CogsDate date) {
        return new Ddi4DataRelationship(
                Ddi4DataRelationship.TYPE,
                date,
                urn("dr-1"),
                AGENCY,
                "dr-1",
                "1",
                null,
                List.of(new LangString("fr", "Mon DR")),
                List.of(new LogicalRecord(
                        LogicalRecord.TYPE,
                        urn("lr-1"),
                        AGENCY,
                        "lr-1",
                        "1",
                        List.of(new LangString("fr", "Mon LR")),
                        new VariablesInRecord(
                                List.of(ref("var-1", Ddi4Variable.TYPE), ref("var-2", Ddi4Variable.TYPE))))));
    }

    private static Ddi4Variable variable(String id, CogsDate date, String codeListId) {
        return new Ddi4Variable(
                Ddi4Variable.TYPE,
                date,
                urn(id),
                AGENCY,
                id,
                "1",
                null,
                List.of(new LangString("fr", id)),
                null,
                null,
                new VariableRepresentation(
                        null,
                        new CodeRepresentation(CodeRepresentation.TYPE, null, ref(codeListId, Ddi4CodeList.TYPE)),
                        null,
                        null,
                        null,
                        null),
                null);
    }

    private static Ddi4CodeList codeList(String id, CogsDate date, String categoryId, String codeValue) {
        return new Ddi4CodeList(
                Ddi4CodeList.TYPE,
                date,
                urn(id),
                AGENCY,
                id,
                "1",
                List.of(new LangString("fr", id)),
                null,
                List.of(new Code(
                        Code.TYPE,
                        urn(id + "-c1"),
                        AGENCY,
                        id + "-c1",
                        "1",
                        ref(categoryId, Ddi4Category.TYPE),
                        ValueType.of(codeValue),
                        null)));
    }

    private static Ddi4Category category(String id, CogsDate date, String label) {
        return new Ddi4Category(
                Ddi4Category.TYPE, date, urn(id), AGENCY, id, "1", List.of(new LangString("fr", label)));
    }

    private static Reference ref(String id, String type) {
        return Reference.of(AGENCY, id, "1", type);
    }

    private static String urn(String id) {
        return Reference.synthesizeUrn(AGENCY, id, "1");
    }

    private static CogsDate date(String iso) {
        return CogsDate.ofDateTime(iso);
    }

    private static CogsDate variableDate(Ddi4Response response, String id) {
        return response.variable().stream()
                .filter(v -> id.equals(v.id()))
                .findFirst()
                .orElseThrow()
                .versionDate();
    }

    private static CogsDate codeListDate(Ddi4Response response, String id) {
        return response.codeList().stream()
                .filter(cl -> id.equals(cl.id()))
                .findFirst()
                .orElseThrow()
                .versionDate();
    }

    private static CogsDate categoryDate(Ddi4Response response, String id) {
        return response.category().stream()
                .filter(c -> id.equals(c.id()))
                .findFirst()
                .orElseThrow()
                .versionDate();
    }
}
