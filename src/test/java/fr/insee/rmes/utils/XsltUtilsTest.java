package fr.insee.rmes.utils;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.*;

class XsltUtilsTest {

    @InjectMocks
    Path finalPath;
    InputStream zipToCompleteIS;

    @Test
    void shouldThrowNoSuchFileExceptionWhenBuildParams()  {
        String file="parent";
        String subfolder = "child";
        File output = new File(file,subfolder);

        Path tempDir = new Path() {
            @Override
            public @NotNull FileSystem getFileSystem() {return null;}

            @Override
            public boolean isAbsolute() {return false;}

            @Override
            public Path getRoot() {return null;}

            @Override
            public Path getFileName() {return null;}

            @Override
            public Path getParent() {return null;}

            @Override
            public int getNameCount() {return 0;}

            @Override
            public @NotNull Path getName(int index) {return null;}

            @Override
            public @NotNull Path subpath(int beginIndex, int endIndex) {return null;}

            @Override
            public boolean startsWith(@NotNull Path other) {return false;}

            @Override
            public boolean endsWith(@NotNull Path other) {return false;}

            @Override
            public @NotNull Path normalize() {return null;}

            @Override
            public @NotNull Path resolve(@NotNull Path other) {return null;}

            @Override
            public @NotNull Path relativize(@NotNull Path other) {return null;}

            @Override
            public @NotNull URI toUri() {return null;}

            @Override
            public @NotNull Path toAbsolutePath() {return null;}

            @Override
            public @NotNull Path toRealPath(@NotNull LinkOption... options) throws IOException {return null;}

            @Override
            public @NotNull WatchKey register(@NotNull WatchService watcher, WatchEvent.@NotNull Kind<?>[] events, @NotNull WatchEvent.Modifier... modifiers) throws IOException {return null;}

            @Override
            public int compareTo(@NotNull Path other) {return 0;}
        };

        NoSuchFileException exception = assertThrows(NoSuchFileException.class, () ->XsltUtils.createOdtFromXml(output,finalPath ,zipToCompleteIS,tempDir));
        assertTrue(!Objects.equals(exception.getMessage(), "") && exception.getMessage()!=null);

    }


}