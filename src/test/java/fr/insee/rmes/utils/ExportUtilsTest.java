package fr.insee.rmes.utils;

import fr.insee.rmes.exceptions.RmesException;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingStream;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static fr.insee.rmes.bauhaus_services.Constants.COLLECTION;
import static fr.insee.rmes.bauhaus_services.concepts.collections.CollectionExportBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;

class ExportUtilsTest {

    @Test
    void whenExportAsInputStream_ShouldNotWriteOnDisk() throws RmesException {

        ExportUtils exportUtils = new ExportUtils(10);
        List<RecordedEvent> recordedEvents = new ArrayList<>();
        InputStream zipStream;
        try (var rs = new RecordingStream()) {
            rs.enable("jdk.FileWrite").withoutThreshold().withStackTrace();
            rs.onEvent("jdk.FileWrite", recordedEvents::add);
            rs.startAsync();
            zipStream = exportUtils.exportAsInputStream("toto", Map.of(), XSL_FILE, XML_PATTERN_ODS, ZIP_ODS, COLLECTION, FilesUtils.ODS_EXTENSION);
            rs.stop();
        }
        assertThat(recordedEvents).isEmpty();
        assertThat(zipStream).isNotEmpty();
    }
}