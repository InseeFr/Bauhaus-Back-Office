package fr.insee.rmes.bauhaus_services.structures.utils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.structures.ComponentDefinition;
import fr.insee.rmes.model.structures.MutualizedComponent;

class StructureUtilsTest {

    @InjectMocks
    @Spy
    private StructureUtils structureUtils;

    @Mock
    private RepositoryGestion repoGestion;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void shouldCallCreateComponentSpecificationForEachComponents() throws RmesException {
        Resource graph = null;
        doReturn(1).when(structureUtils).getNextComponentSpecificationID();

        Model model = new LinkedHashModel();
        IRI structureIRI = SimpleValueFactory.getInstance().createIRI("http://structure");

        List<ComponentDefinition> components = new ArrayList<>();

        ComponentDefinition componentDefinition1 = new ComponentDefinition();
        MutualizedComponent component1 = new MutualizedComponent();
        component1.setId("d1001");
        componentDefinition1.setComponent(component1);

        ComponentDefinition componentDefinition2 = new ComponentDefinition();
        MutualizedComponent component2 = new MutualizedComponent();
        component2.setId("d1002");
        componentDefinition2.setComponent(component2);

        components.add(componentDefinition1);
        components.add(componentDefinition2);

        doNothing().when(structureUtils).createRdfComponentSpecification(any(), any(), any(), any());

        structureUtils.createRdfComponentSpecifications(structureIRI, components, model, graph);

        verify(structureUtils, times(2)).createRdfComponentSpecification(any(), any(), any(), any());
    }
}
