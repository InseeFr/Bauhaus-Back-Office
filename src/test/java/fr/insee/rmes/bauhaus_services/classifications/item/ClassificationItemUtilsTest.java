package fr.insee.rmes.bauhaus_services.classifications.item;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.Config;
import fr.insee.rmes.exceptions.RmesBadRequestException;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.classification.ClassificationItem;
import fr.insee.rmes.persistance.sparql_queries.classifications.ItemsQueries;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassificationItemUtilsTest {
    @Mock
    Config config;

    @Mock
    RepositoryGestion repositoryGestion;

    @InjectMocks
    ClassificationItemUtils classificationItemUtils;

    @Test
    void shouldThrowExceptionIfPrefLabelLg1Null() throws RmesException {
        when(config.getLg1()).thenReturn("fr");
        when(config.getLg2()).thenReturn("en");


        RdfUtils.setConfig(config);
        ItemsQueries.setConfig(config);
        ClassificationItem item = new ClassificationItem();
        item.setId("1");
        item.setPrefLabelLg2("label2");
        item.setDefinitionLg1("<p>Definition Lg1</p>");
        item.setDefinitionLg1Uri("http://definition-lg1");

        RmesException exception = assertThrows(RmesBadRequestException.class, () -> classificationItemUtils.updateClassificationItem(item, "http://uri", "1"));
        assertThat(exception.getDetails()).contains("The property prefLabelLg1 is required");

    }

    @Test
    void shouldThrowExceptionIfPrefLabelLg2Null() throws RmesException {
        when(config.getLg1()).thenReturn("fr");
        when(config.getLg2()).thenReturn("en");


        RdfUtils.setConfig(config);
        ItemsQueries.setConfig(config);
        ClassificationItem item = new ClassificationItem();
        item.setId("1");
        item.setPrefLabelLg1("label1");
        item.setDefinitionLg1("<p>Definition Lg1</p>");
        item.setDefinitionLg1Uri("http://definition-lg1");

        RmesException exception = assertThrows(RmesBadRequestException.class, () -> classificationItemUtils.updateClassificationItem(item, "http://uri", "1"));
        assertThat(exception.getDetails()).contains("The property prefLabelLg2 is required");

    }
}