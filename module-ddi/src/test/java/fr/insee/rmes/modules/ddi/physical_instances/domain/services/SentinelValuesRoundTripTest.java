package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import fr.insee.ddi.lifecycle33.instance.FragmentDocument;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CodeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.DateTimeRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Variable;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangStrings;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.NumberRange;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.NumericRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.RangeValue;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.TextRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.VariableRepresentation;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valeurs sentinelles (#1566) — aller-retour DDI 4 → DDI 3 → DDI 4 : la
 * {@code MissingValuesReference} portée par le wrapper {@code VariableRepresentation} survit à la
 * conversion pour les quatre types de représentation, et le fragment MMVR lui-même est stable en
 * écriture → lecture.
 */
class SentinelValuesRoundTripTest {

    private static final Reference MMVR_REF =
            Reference.of("fr.insee", "mmvr-1", "1", "ManagedMissingValuesRepresentation");

    private final Ddi4ToLifecycle33 writer = new Ddi4ToLifecycle33();
    private final Lifecycle33ToDdi4 reader = new Lifecycle33ToDdi4();

    @Test
    void missingValuesReference_survivesRoundTrip_withCodeRepresentation() throws XmlException {
        VariableRepresentation rep = new VariableRepresentation(null,
                new CodeRepresentation(CodeRepresentation.TYPE, false,
                        Reference.of("fr.insee", "cl-1", "1", "CodeList")),
                null, null, null, MMVR_REF);

        VariableRepresentation roundTripped = roundTrip(rep);

        assertThat(roundTripped.missingValuesReference()).isEqualTo(MMVR_REF);
        assertThat(roundTripped.codeRepresentation().codeListReference().id()).isEqualTo("cl-1");
    }

    @Test
    void missingValuesReference_survivesRoundTrip_withNumericRepresentation() throws XmlException {
        VariableRepresentation rep = new VariableRepresentation(null, null,
                new NumericRepresentation(NumericRepresentation.TYPE, "Integer",
                        new NumberRange(new RangeValue(false, 0.0), new RangeValue(true, 100.0))),
                null, null, MMVR_REF);

        VariableRepresentation roundTripped = roundTrip(rep);

        assertThat(roundTripped.missingValuesReference()).isEqualTo(MMVR_REF);
        assertThat(roundTripped.numericRepresentation().numericTypeCode()).isEqualTo("Integer");
    }

    @Test
    void missingValuesReference_survivesRoundTrip_withDateTimeRepresentation() throws XmlException {
        VariableRepresentation rep = new VariableRepresentation(null, null, null,
                new DateTimeRepresentation(DateTimeRepresentation.TYPE, "Date", "yyyy-MM-dd"),
                null, MMVR_REF);

        VariableRepresentation roundTripped = roundTrip(rep);

        assertThat(roundTripped.missingValuesReference()).isEqualTo(MMVR_REF);
        assertThat(roundTripped.dateTimeRepresentation().dateTypeCode()).isEqualTo("Date");
    }

    @Test
    void missingValuesReference_survivesRoundTrip_withTextRepresentation() throws XmlException {
        VariableRepresentation rep = new VariableRepresentation(null, null, null, null,
                new TextRepresentation(TextRepresentation.TYPE, 255, 1, "[A-Z]+", true),
                MMVR_REF);

        VariableRepresentation roundTripped = roundTrip(rep);

        assertThat(roundTripped.missingValuesReference()).isEqualTo(MMVR_REF);
        assertThat(roundTripped.textRepresentation().maxLength()).isEqualTo(255);
    }

    @Test
    void managedMissingValuesRepresentation_survivesRoundTrip() throws XmlException {
        Ddi4ManagedMissingValuesRepresentation mmvr = new Ddi4ManagedMissingValuesRepresentation(
                Ddi4ManagedMissingValuesRepresentation.TYPE,
                CogsDate.ofDateTime("2026-04-03T12:00:00Z"),
                "urn:ddi:fr.insee:mmvr-1:1",
                "fr.insee", "mmvr-1", "1",
                LangStrings.of("fr-FR", "Valeurs sentinelles NSP/REF"),
                List.of(new CodeRepresentation(CodeRepresentation.TYPE, false,
                        Reference.of("fr.insee", "cl-sentinelles", "1", "CodeList"))));

        String xml = writer.toManagedMissingValuesRepresentation(mmvr).xmlText();
        Ddi4ManagedMissingValuesRepresentation roundTripped =
                reader.toManagedMissingValuesRepresentation(FragmentDocument.Factory.parse(xml));

        assertThat(roundTripped).isEqualTo(mmvr);
    }

    @Test
    void managedMissingValuesRepresentation_isWritable_withoutVersionDate() throws XmlException {
        // L'aperçu du front reconstruit une MMVR seulement réutilisée depuis la vue partielle du
        // groupe, qui ne porte pas la VersionDate : l'écriture doit l'accepter absente plutôt que
        // de forcer le front à en inventer une.
        Ddi4ManagedMissingValuesRepresentation mmvr = new Ddi4ManagedMissingValuesRepresentation(
                Ddi4ManagedMissingValuesRepresentation.TYPE,
                null,
                "urn:ddi:fr.insee:mmvr-1:1",
                "fr.insee", "mmvr-1", "1",
                LangStrings.of("fr-FR", "Valeurs sentinelles NSP/REF"),
                List.of(new CodeRepresentation(CodeRepresentation.TYPE, false,
                        Reference.of("fr.insee", "cl-sentinelles", "1", "CodeList"))));

        String xml = writer.toManagedMissingValuesRepresentation(mmvr).xmlText();
        Ddi4ManagedMissingValuesRepresentation roundTripped =
                reader.toManagedMissingValuesRepresentation(FragmentDocument.Factory.parse(xml));

        assertThat(roundTripped.versionDate()).isNull();
        assertThat(roundTripped.missingCodeRepresentation().getFirst().codeListReference().id())
                .isEqualTo("cl-sentinelles");
    }

    private VariableRepresentation roundTrip(VariableRepresentation rep) throws XmlException {
        Ddi4Variable variable = new Ddi4Variable(Ddi4Variable.TYPE,
                CogsDate.ofDateTime("2025-12-23T09:52:06.355Z"),
                "urn:ddi:fr.insee:var-rt:1", "fr.insee", "var-rt", "1", null,
                LangStrings.of("fr-FR", "VAR_RT"), null, null, rep, null);

        String xml = writer.toVariable(variable).xmlText();

        return reader.toVariable(FragmentDocument.Factory.parse(xml)).variableRepresentation();
    }
}
