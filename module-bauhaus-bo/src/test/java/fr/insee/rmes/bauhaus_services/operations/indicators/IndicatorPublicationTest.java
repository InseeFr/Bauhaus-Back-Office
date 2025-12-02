package fr.insee.rmes.bauhaus_services.operations.indicators;

import fr.insee.rmes.bauhaus_services.operations.ParentUtils;
import fr.insee.rmes.graphdb.ObjectType;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.modules.shared_kernel.domain.model.ValidationStatus;
import fr.insee.rmes.model.links.OperationsLink;
import fr.insee.rmes.model.operations.Indicator;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndicatorPublicationTest {

    @Mock
    private ParentUtils ownersUtils;

    @InjectMocks
    private IndicatorPublication indicatorPublication;

    private Indicator indicator;

    @BeforeEach
    void setUp() throws RmesException {
        indicator = new Indicator();
        indicator.setId("123");
    }



    @Test
    void validate_ShouldThrowBadRequestException_WhenParentSeriesIsNotValidated() throws RmesException {
        try (MockedStatic<RdfUtils> mockedFactory = Mockito.mockStatic(RdfUtils.class)) {
            OperationsLink link = new OperationsLink();
            link.id = "series-1";
            when(ownersUtils.getValidationStatus("series-1")).thenReturn(ValidationStatus.UNPUBLISHED.toString());
            indicator.wasGeneratedBy = List.of(link);

            mockedFactory.when(() -> RdfUtils.objectIRI(eq(ObjectType.INDICATOR), eq("123"))).thenReturn(SimpleValueFactory.getInstance().createIRI("http://indicator/1"));

            RmesBadRequestException exception = assertThrows(RmesBadRequestException.class, () -> indicatorPublication.validate(indicator));
            assertThat(exception.getDetails()).contains("An indicator can be published if and only if all parent series have been published.");
        }
    }

    @Test
    void validate_ShouldPass_WhenUserHasPermissionAndParentSeriesAreValidated() throws RmesException {
        try (MockedStatic<RdfUtils> mockedFactory = Mockito.mockStatic(RdfUtils.class)) {
            OperationsLink link = new OperationsLink();
            link.id = "series-1";
            when(ownersUtils.getValidationStatus("series-1")).thenReturn(ValidationStatus.VALIDATED.toString());
            indicator.wasGeneratedBy = List.of(link);

            mockedFactory.when(() -> RdfUtils.objectIRI(eq(ObjectType.INDICATOR), eq("123"))).thenReturn(SimpleValueFactory.getInstance().createIRI("http://indicator/1"));

            assertDoesNotThrow(() -> indicatorPublication.validate(indicator));
        }
    }
}
