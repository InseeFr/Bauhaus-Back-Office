package fr.insee.rmes.utils;

import fr.insee.rmes.exceptions.RmesException;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.util.Map;

import static fr.insee.rmes.bauhaus_services.Constants.COLLECTION;
import static fr.insee.rmes.bauhaus_services.concepts.collections.CollectionExportBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;

class ExportUtilsTest {

    @Test
    void whenExportAsInputStream_ShouldNotWriteOnDisk() throws IOException, RmesException {
        ExportUtils exportUtils = new ExportUtils(10);
        var watchService = FileSystems.getDefault().newWatchService();
        var watchKey = Path.of(Files.temporaryFolderPath()).register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_CREATE);
        exportUtils.exportAsInputStream("toto", Map.of(), XSL_FILE, XML_PATTERN_ODS, ZIP_ODS, COLLECTION, FilesUtils.ODS_EXTENSION);
        assertThat(watchKey.pollEvents()).isEmpty();
        watchService.close();
    }
}