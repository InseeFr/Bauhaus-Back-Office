package fr.insee.rmes.bauhaus_services.structures.utils;

import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.structures.ComponentDefinition;
import fr.insee.rmes.model.structures.MutualizedComponent;
import fr.insee.rmes.model.structures.Structure;
import fr.insee.rmes.utils.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class StructureUtilsTest {

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
    public void shouldCallCreateRdfComponentSpecifications() throws RmesException {
        Resource graph = null;

        String currentDate = DateUtils.getCurrentDate();
        doNothing().when(structureUtils).createRdfComponentSpecifications(any(), anyList(), any(), any());
        doNothing().when(repoGestion).loadSimpleObject(any(), any(), any());
        doReturn(1).when(structureUtils).getNextComponentSpecificationID();

        URI structureIRI = ValueFactoryImpl.getInstance().createURI("http://structure");

        Structure structure = new Structure();
        structure.setId("id");
        structure.setLabelLg1("labelLg1");
        structure.setLabelLg2("labelLg2");
        structure.setUpdated(currentDate);
        structure.setCreated(currentDate);
        structure.setComponentDefinitions(new ArrayList<>());

        structureUtils.createRdfStructure(structure, "id", structureIRI, null);
        verify(structureUtils, times(1)).createRdfComponentSpecifications(any(), anyList(), any(), any());

    }

    @Test
    public void shouldCallCreateComponentSpecificationForEachComponents() throws RmesException {
        Resource graph = null;
        doReturn(1).when(structureUtils).getNextComponentSpecificationID();

        Model model = new LinkedHashModel();
        URI structureIRI = ValueFactoryImpl.getInstance().createURI("http://structure");

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
