package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Citation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CogsDate;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4StudyUnit;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.LangStrings;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Reference;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.ddi.physical_instances.domain.services.Ddi4ToLifecycle33;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ColecticaStudyUnitRepositoryTest {

    private ColecticaStudyUnitRepository repository() {
        var instanceConfig = new ColecticaConfiguration.ColecticaInstanceConfiguration(
                "http://localhost:8082",
                "/api/v1/",
                null,
                "bauhaus",
                "DC337820-AF3A-4C0B-82F9-CF02535CDE83",
                "token",
                null,
                null,
                "fr.insee");
        return spy(new ColecticaStudyUnitRepository(
                mock(ColecticaClient.class), instanceConfig, new Ddi4ToLifecycle33(), mock(DDIRepository.class)));
    }

    @Test
    void addPhysicalInstance_preservesLogicalProductReferences() {
        // Given a study unit that files a LogicalProduct (holding its series' VariableScheme)
        Reference logicalProductReference = Reference.of("fr.insee", "lp-1", "1", "LogicalProduct");
        Ddi4StudyUnit studyUnit = new Ddi4StudyUnit(
                Ddi4StudyUnit.TYPE,
                CogsDate.ofDateTime("2026-01-01T00:00:00Z"),
                "urn:ddi:fr.insee:su-1:1",
                "fr.insee",
                "su-1",
                "1",
                new Citation(LangStrings.of("fr-FR", "Study Unit")),
                "http://id.insee.fr/operations/operation/op1",
                null,
                List.of(logicalProductReference));

        ColecticaStudyUnitRepository repository = repository();
        doNothing().when(repository).createOrUpdate(any());

        // When a physical instance is linked
        repository.addPhysicalInstance(studyUnit, Reference.of("fr.insee", "pi-1", "1", "PhysicalInstance"));

        // Then the rewritten study unit keeps both the new PhysicalInstanceReference and its LogicalProductReference
        ArgumentCaptor<Ddi4StudyUnit> captor = ArgumentCaptor.forClass(Ddi4StudyUnit.class);
        verify(repository).createOrUpdate(captor.capture());
        Ddi4StudyUnit updated = captor.getValue();
        assertThat(updated.physicalInstanceReferences())
                .extracting(Reference::id)
                .containsExactly("pi-1");
        assertThat(updated.logicalProductReferences()).containsExactly(logicalProductReference);
    }
}
