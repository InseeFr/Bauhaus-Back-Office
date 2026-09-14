package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangString;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Ce que le miroir des opérations écrit dans un Group doit pouvoir être relu : le Group est réécrit
 * à chaque enregistrement de sa série ou d'une de ses opérations, et tout ce que la relecture laisse
 * tomber est perdu à la réécriture suivante. L'IRI de la série ({@code r:UserID}) en particulier est
 * la clé par laquelle les stamps sont résolus.
 */
class ColecticaGroupSetReaderTest {

    private static final String GROUP_ID = "10a689ce-7006-429b-8e84-036b7787b422";

    private final ColecticaClient colecticaClient = mock(ColecticaClient.class);
    private final ColecticaGroupSetReader reader = new ColecticaGroupSetReader(colecticaClient, "fr-FR");

    @Test
    void readsBackTheSeriesIrisTheStampsAreResolvedFrom() {
        Ddi4Group group = findGroup();

        assertThat(group.seriesIris()).containsExactly("http://id.insee.fr/operations/serie/s1001");
    }

    @Test
    void readsBackTheLogicalProductThatFilesTheGroupSchemes() {
        Ddi4Group group = findGroup();

        assertThat(group.logicalProductReference())
                .containsExactly(Reference.of("fr.insee", "lp-1", "1", "LogicalProduct"));
    }

    @Test
    void readsBackTheTitleInEveryLanguageAndTheAlternateTitles() {
        Ddi4Group group = findGroup();

        assertThat(group.citation().title())
                .containsExactly(
                        new LangString("fr-FR", "Base permanente des équipements"),
                        new LangString("en-GB", "Permanent database of facilities"));
        assertThat(group.citation().alternateTitle())
                .containsExactly(new LangString("fr-FR", "BPE"), new LangString("en-GB", "PDF"));
    }

    private Ddi4Group findGroup() {
        when(colecticaClient.getDdiSet(anyString(), anyString()))
                .thenReturn(ddisetXml().getBytes(StandardCharsets.UTF_8));

        Optional<Ddi4Group> group = reader.findGroup("fr.insee", GROUP_ID);

        assertThat(group).isPresent();
        return group.get();
    }

    private static String ddisetXml() {
        return """
                <?xml version="1.0" encoding="utf-8"?>
                <ddi:FragmentInstance xmlns:r="ddi:reusable:3_3" xmlns:ddi="ddi:instance:3_3">
                    <Fragment xmlns:r="ddi:reusable:3_3" xmlns="ddi:instance:3_3">
                        <Group isUniversallyUnique="true" versionDate="2026-01-09T09:00:00Z" xmlns="ddi:group:3_3">
                            <r:URN>urn:ddi:fr.insee:%1$s:1</r:URN>
                            <r:Agency>fr.insee</r:Agency>
                            <r:ID>%1$s</r:ID>
                            <r:Version>1</r:Version>
                            <r:UserID typeOfUserID="URI">http://id.insee.fr/operations/serie/s1001</r:UserID>
                            <TypeOfGroup>insee:StatisticalOperationSeries</TypeOfGroup>
                            <r:Citation>
                                <r:Title>
                                    <r:String xml:lang="fr-FR">Base permanente des équipements</r:String>
                                    <r:String xml:lang="en-GB">Permanent database of facilities</r:String>
                                </r:Title>
                                <r:AlternateTitle>
                                    <r:String xml:lang="fr-FR">BPE</r:String>
                                </r:AlternateTitle>
                                <r:AlternateTitle>
                                    <r:String xml:lang="en-GB">PDF</r:String>
                                </r:AlternateTitle>
                            </r:Citation>
                            <r:StudyUnitReference>
                                <r:Agency>fr.insee</r:Agency>
                                <r:ID>su-1</r:ID>
                                <r:Version>1</r:Version>
                                <r:TypeOfObject>StudyUnit</r:TypeOfObject>
                            </r:StudyUnitReference>
                            <r:LogicalProductReference>
                                <r:Agency>fr.insee</r:Agency>
                                <r:ID>lp-1</r:ID>
                                <r:Version>1</r:Version>
                                <r:TypeOfObject>LogicalProduct</r:TypeOfObject>
                            </r:LogicalProductReference>
                        </Group>
                    </Fragment>
                </ddi:FragmentInstance>""".formatted(GROUP_ID);
    }
}
