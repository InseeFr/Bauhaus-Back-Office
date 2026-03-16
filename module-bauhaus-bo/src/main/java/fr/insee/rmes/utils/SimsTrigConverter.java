package fr.insee.rmes.utils;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimsTrigConverter {

    private static final Pattern VALUE_PATTERN = Pattern.compile(
            "(<http://www\\.w3\\.org/1999/02/22-rdf-syntax-ns#value>\\s+\")(<p>.*?)(\"@(\\w+)\\s*;)"
    );

    public static void main(String[] args) throws IOException {
        Path inputPath = Path.of("module-bauhaus-bo/src/test/resources/testcontainers/sims-all.trig");
        Path outputPath = Path.of("module-bauhaus-bo/src/test/resources/testcontainers/md-sims.trig");

        String content = Files.readString(inputPath);
        String converted = convertHtmlValuesToMarkdown(content);
        Files.writeString(outputPath, converted);

        System.out.println("Conversion terminée : " + outputPath);
    }

    static String convertHtmlValuesToMarkdown(String content) {
        MutableDataSet options = new MutableDataSet();
        options.set(FlexmarkHtmlConverter.SKIP_CHAR_ESCAPE, true);
        options.set(FlexmarkHtmlConverter.TYPOGRAPHIC_QUOTES, false);
        options.set(FlexmarkHtmlConverter.TYPOGRAPHIC_SMARTS, false);
        FlexmarkHtmlConverter converter = FlexmarkHtmlConverter.builder(options).build();

        Matcher matcher = VALUE_PATTERN.matcher(content);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String htmlValue = unescapeTurtle(matcher.group(2));
            String mdValue = converter.convert(htmlValue);
            if (mdValue.endsWith("\n")) {
                mdValue = mdValue.substring(0, mdValue.length() - 1);
            }
            String escapedMd = escapeTurtle(mdValue);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(
                    matcher.group(1) + escapedMd + matcher.group(3)
            ));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private static String unescapeTurtle(String value) {
        return value
                .replace("\\\\", "\u0000BACKSLASH\u0000")
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\u0000BACKSLASH\u0000", "\\");
    }

    private static String escapeTurtle(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private SimsTrigConverter() {
        throw new IllegalStateException("Utility class");
    }
}
