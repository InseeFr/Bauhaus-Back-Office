package fr.insee.rmes.modules.ddi.physical_instances.domain.services;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIItemConverter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DDIItemConvertServiceImplTest {

    private static final String GROUP_FRAGMENT = """
            <Fragment xmlns="ddi:instance:3_3">
                <ddi:Group xmlns:ddi="ddi:group:3_3">
                    <r:URN xmlns:r="ddi:reusable:3_3">urn:ddi:fr.insee:abc:1</r:URN>
                    <r:Agency xmlns:r="ddi:reusable:3_3">fr.insee</r:Agency>
                    <r:ID xmlns:r="ddi:reusable:3_3">abc</r:ID>
                    <r:Version xmlns:r="ddi:reusable:3_3">1</r:Version>
                </ddi:Group>
            </Fragment>
            """;

    @Test
    void convert_dispatchesToMatchingConverter() {
        DDIItemConverter groupConverter = mock(DDIItemConverter.class);
        when(groupConverter.supports("Group")).thenReturn(true);
        JsonNode expected = mock(JsonNode.class);
        when(groupConverter.convert(GROUP_FRAGMENT)).thenReturn(expected);

        var service = new DDIItemConvertServiceImpl(List.of(groupConverter));
        JsonNode result = service.convert(GROUP_FRAGMENT);

        assertSame(expected, result);
        verify(groupConverter).convert(GROUP_FRAGMENT);
    }

    @Test
    void convert_throwsWhenNoConverterFound() {
        DDIItemConverter converter = mock(DDIItemConverter.class);
        when(converter.supports(anyString())).thenReturn(false);

        var service = new DDIItemConvertServiceImpl(List.of(converter));
        assertThrows(IllegalArgumentException.class, () -> service.convert(GROUP_FRAGMENT));
    }

    @Test
    void convert_worksWithDirectItemXml() {
        String directXml = """
                <ddi:Group xmlns:ddi="ddi:group:3_3">
                    <r:URN xmlns:r="ddi:reusable:3_3">urn:ddi:fr.insee:abc:1</r:URN>
                    <r:Agency xmlns:r="ddi:reusable:3_3">fr.insee</r:Agency>
                    <r:ID xmlns:r="ddi:reusable:3_3">abc</r:ID>
                    <r:Version xmlns:r="ddi:reusable:3_3">1</r:Version>
                </ddi:Group>
                """;

        DDIItemConverter groupConverter = mock(DDIItemConverter.class);
        when(groupConverter.supports("Group")).thenReturn(true);
        when(groupConverter.convert(directXml)).thenReturn(mock(JsonNode.class));

        var service = new DDIItemConvertServiceImpl(List.of(groupConverter));
        service.convert(directXml);

        verify(groupConverter).supports("Group");
    }
}
