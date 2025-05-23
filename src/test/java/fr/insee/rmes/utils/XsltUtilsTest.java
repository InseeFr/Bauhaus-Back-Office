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
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void shouldBuildParamsWithCorrectDisplay() {
        String target= "An example of target";
        String trueTrueTrue=  XsltUtils.buildParams(true,true,true,target);
        String trueTrueFalse=  XsltUtils.buildParams(true,true,false,target);
        String trueFalseTrue=  XsltUtils.buildParams(true,false,true,target);
        String trueFalseFalse=  XsltUtils.buildParams(true,false,false,target);
        String falseTrueTrue=  XsltUtils.buildParams(false,true,true,target);
        String falseTrueFalse=  XsltUtils.buildParams(false,true,false,target);
        String falseFalseTrue=  XsltUtils.buildParams(false,false,true,target);
        String falseFalseFalse=  XsltUtils.buildParams(false,false,false,target);

        List<String> results = List.of(trueTrueTrue,trueTrueFalse,trueFalseTrue,trueFalseFalse,falseTrueTrue,falseTrueFalse,falseFalseTrue,falseFalseFalse);
        List<Boolean> actualIncludeEmptyFields = new ArrayList<>();
        List<Boolean> actualLanguage1 = new ArrayList<>();
        List<Boolean> actualLanguage2 = new ArrayList<>();

        for (String element : results){
            actualIncludeEmptyFields.add(element.contains("</includeEmptyFields>"));
            actualLanguage1.add(element.contains("<language id=\"Fr\">1</language>"));
            actualLanguage2.add(element.contains("<language id=\"En\">2</language>"));
        }

        boolean actualCorrespondToExpectedIncludeEmptyFields = List.of(true,true,true,true,true,true,true,true).equals(actualIncludeEmptyFields);
        boolean actualCorrespondToExpectedLanguage1= List.of(true,true,true,true,false,false,false,false).equals(actualLanguage1);
        boolean actualCorrespondToExpectedLanguage2= List.of(true,true,false,false,true,true,false,false).equals(actualLanguage2);

        assertTrue(actualCorrespondToExpectedIncludeEmptyFields && actualCorrespondToExpectedLanguage1 && actualCorrespondToExpectedLanguage2);

    }

}